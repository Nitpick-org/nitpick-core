package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.graph.buildCommitGraph
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommitFiltersTest {

    private val base = commit("base", "Initial commit", "Ada", 86_400)
    private val feature = commit("feature", "Add filters", "Bob", 2 * 86_400, listOf(base.id))
    private val docs = commit("docs", "Improve docs", "Ada", 3 * 86_400, listOf(base.id))
    private val tip = commit("tip", "Final polish", "Bob", 4 * 86_400, listOf(feature.id, docs.id))
    private val rows = buildCommitGraph(listOf(tip, docs, feature, base))
    private val refs = listOf(
        Ref("main", "refs/heads/main", tip.id, RefType.LOCAL_BRANCH),
        Ref("base-only", "refs/heads/base-only", base.id, RefType.LOCAL_BRANCH),
    )
    private val now = 4 * 86_400L + 3_600L

    @Test
    fun textMatchesSubjectAndShortHash() {
        val bySubject = filterCommits(rows, refs, CommitFilters(query = "polish"), now)
        val byHash = filterCommits(rows, refs, CommitFilters(query = "tip"), now)

        assertEquals(listOf("tip"), bySubject.map { it.commit.id })
        assertEquals(listOf("tip"), byHash.map { it.commit.id })
    }

    @Test
    fun authorMatchesExactly() {
        val filtered = filterCommits(rows, refs, CommitFilters(author = "Ada"), now)

        assertEquals(listOf("docs", "base"), filtered.map { it.commit.id })
    }

    @Test
    fun branchFilterKeepsOnlyAncestorsOfTheBranchTip() {
        val filtered = filterCommits(rows, refs, CommitFilters(branch = "base-only"), now)

        assertEquals(listOf("base"), filtered.map { it.commit.id })
    }

    @Test
    fun dateFilterUsesTheProvidedNow() {
        val today = filterCommits(rows, refs, CommitFilters(date = CommitDateFilter.TODAY), now)
        val week = filterCommits(rows, refs, CommitFilters(date = CommitDateFilter.WEEK), now)

        assertEquals(listOf("tip"), today.map { it.commit.id })
        assertEquals(rows.map { it.commit.id }, week.map { it.commit.id })
    }

    @Test
    fun noFilterKeepsTheGraphOrder() {
        val filtered = filterCommits(rows, refs, CommitFilters(), now)

        assertEquals(rows.map { it.commit.id }, filtered.map { it.commit.id })
        assertTrue(filtered.isNotEmpty())
    }

    private fun commit(id: String, message: String, author: String, seconds: Long, parents: List<String> = emptyList()) = Commit(
        id = id,
        shortMessage = message,
        fullMessage = message,
        authorName = author,
        authorEmail = "$author@example.com",
        timestampEpochSeconds = seconds,
        parentIds = parents,
    )
}
