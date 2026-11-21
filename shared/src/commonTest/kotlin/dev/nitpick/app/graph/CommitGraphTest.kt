package dev.nitpick.app.graph

import dev.nitpick.app.domain.Commit
import kotlin.test.Test
import kotlin.test.assertEquals

class CommitGraphTest {

    @Test
    fun emptyInputProducesNoRows() {
        assertEquals(emptyList(), buildCommitGraph(emptyList()))
    }

    @Test
    fun linearHistoryStaysOnOneLane() {
        val rows = buildCommitGraph(
            listOf(
                commit("c", "b"),
                commit("b", "a"),
                commit("a"),
            ),
        )

        assertEquals(listOf(0, 0, 0), rows.map { it.lane })
        assertEquals(listOf(listOf(0), listOf(0), emptyList()), rows.map { it.parentLanes })
        assertEquals(listOf(emptyList(), listOf(0), listOf(0)), rows.map { it.incomingLanes })
        assertEquals(emptyList<String>(), rows.last().lanesAfter.filterNotNull())
    }

    @Test
    fun branchAndMergeUseTwoLanesThenConverge() {
        val rows = buildCommitGraph(
            listOf(
                commit("m", "a", "b"),
                commit("a", "c"),
                commit("b", "c"),
                commit("c", "d"),
                commit("d"),
            ),
        )

        assertEquals(listOf(0, 0, 1, 0, 0), rows.map { it.lane })
        assertEquals(listOf(listOf(0, 1), listOf(0), listOf(0), listOf(0), emptyList()), rows.map { it.parentLanes })
        assertEquals(listOf(emptyList(), listOf(0), listOf(1), listOf(0), listOf(0)), rows.map { it.incomingLanes })
        assertEquals(listOf<String?>("a", "b"), rows[0].lanesAfter)
        assertEquals(listOf<String?>("c", "b"), rows[2].lanesBefore)
    }

    @Test
    fun orphanCommitEndsItsLane() {
        val rows = buildCommitGraph(
            listOf(
                commit("orphan", "not-in-log"),
            ),
        )

        assertEquals(0, rows.single().lane)
        assertEquals(emptyList(), rows.single().parentLanes)
        assertEquals(emptyList(), rows.single().incomingLanes)
        assertEquals(emptyList<String>(), rows.single().lanesAfter.filterNotNull())
    }

    @Test
    fun octopusMergeGetsOneLanePerParent() {
        val rows = buildCommitGraph(
            listOf(
                commit("m", "a", "b", "c"),
                commit("a", "d"),
                commit("b", "d"),
                commit("c", "d"),
                commit("d"),
            ),
        )

        assertEquals(listOf(0, 1, 2), rows.first().parentLanes)
        assertEquals(listOf(0, 0, 1, 2, 0), rows.map { it.lane })
        assertEquals(emptyList(), rows.last().parentLanes)
    }

    @Test
    fun releasedLaneIsReusedByALaterBranch() {
        val rows = buildCommitGraph(
            listOf(
                commit("m", "a", "b"),
                commit("a", "c"),
                commit("b", "d"),
                commit("c", "e"),
                commit("d"),
                commit("x", "y"),
                commit("y"),
                commit("e"),
            ),
        )

        assertEquals(1, rows.first { it.commit.id == "x" }.lane)
        assertEquals(1, rows.first { it.commit.id == "y" }.lane)
        assertEquals(0, rows.first { it.commit.id == "e" }.lane)
    }

    @Test
    fun secondChildConvergesOnTheExistingParentLane() {
        val rows = buildCommitGraph(
            listOf(
                commit("m1", "a"),
                commit("m2", "a"),
                commit("a"),
            ),
        )

        val secondChild = rows.first { it.commit.id == "m2" }
        assertEquals(1, secondChild.lane)
        assertEquals(listOf(0), secondChild.parentLanes)
        assertEquals(0, rows.last().lane)
    }

    @Test
    fun inputIsCappedAtFiveHundredCommits() {
        val commits = (0 until 600).map { index ->
            if (index == 599) commit("c$index") else commit("c$index", "c${index + 1}")
        }

        val rows = buildCommitGraph(commits)

        assertEquals(500, rows.size)
    }

    private fun commit(id: String, vararg parents: String): Commit = Commit(
        id = id,
        shortMessage = id,
        fullMessage = id,
        authorName = "Tester",
        authorEmail = "tester@nitpick.dev",
        timestampEpochSeconds = 0,
        parentIds = parents.toList(),
    )
}
