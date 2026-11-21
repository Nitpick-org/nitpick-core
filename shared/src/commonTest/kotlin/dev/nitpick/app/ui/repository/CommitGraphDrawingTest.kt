package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.Commit
import dev.nitpick.app.graph.buildCommitGraph
import kotlin.test.Test
import kotlin.test.assertEquals

class CommitGraphDrawingTest {

    // m merges b into a; a and b both come from c.
    private val rows = buildCommitGraph(
        listOf(
            commit("m", "a", "b"),
            commit("a", "c"),
            commit("b", "c"),
            commit("c"),
        ),
    )

    @Test
    fun aLaneAlreadyCarryingTheParentStaysDrawnThroughTheRow() {
        // Row b sits on lane 1 and joins lane 0, which already carries the line from a down to c.
        assertEquals(listOf(0), passThroughLanes(rows[2]))
    }

    @Test
    fun aParentLaneThatOpensOnThisRowIsLeftToTheCurve() {
        // Row m opens lane 1 for b: the curve from m draws it, not a straight line.
        assertEquals(emptyList(), passThroughLanes(rows[0]))
    }

    @Test
    fun anUnrelatedLaneCrossesTheRow() {
        // Row a sits on lane 0 while lane 1 waits for b.
        assertEquals(listOf(1), passThroughLanes(rows[1]))
    }

    private fun commit(id: String, vararg parents: String) = Commit(
        id = id,
        shortMessage = id,
        fullMessage = "$id\n",
        authorName = "Ada Lovelace",
        authorEmail = "ada@nitpick.dev",
        timestampEpochSeconds = 0,
        parentIds = parents.toList(),
    )
}
