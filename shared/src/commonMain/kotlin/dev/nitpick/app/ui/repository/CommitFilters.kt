package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.graph.CommitGraphRow

enum class CommitDateFilter { ALL, TODAY, WEEK, MONTH }

data class CommitFilters(
    val query: String = "",
    val branch: String? = null,
    val author: String? = null,
    val date: CommitDateFilter = CommitDateFilter.ALL,
) {
    val isActive: Boolean
        get() = query.isNotBlank() || branch != null || author != null || date != CommitDateFilter.ALL
}

/**
 * Filters commits without touching the lane layout. When a filter is active the UI hides the
 * graph; showing filtered rows with the original lanes would draw edges to rows that are gone.
 */
fun filterCommits(
    rows: List<CommitGraphRow>,
    refs: List<Ref>,
    filters: CommitFilters,
    nowEpochSeconds: Long,
): List<CommitGraphRow> {
    val reachable = filters.branch?.let { branch -> reachableFrom(branch, rows, refs) }
    val maxAge = when (filters.date) {
        CommitDateFilter.ALL -> null
        CommitDateFilter.TODAY -> 86_400L
        CommitDateFilter.WEEK -> 7 * 86_400L
        CommitDateFilter.MONTH -> 30 * 86_400L
    }
    return rows.filter { row ->
        val commit = row.commit
        val query = filters.query.trim()
        (query.isEmpty() || commit.id.startsWith(query, ignoreCase = true) ||
            commit.shortMessage.contains(query, ignoreCase = true) ||
            commit.fullMessage.contains(query, ignoreCase = true) ||
            commit.authorName.contains(query, ignoreCase = true)) &&
            (reachable == null || commit.id in reachable) &&
            (filters.author == null || commit.authorName == filters.author) &&
            (maxAge == null || nowEpochSeconds - commit.timestampEpochSeconds in 0..maxAge)
    }
}

private fun reachableFrom(branch: String, rows: List<CommitGraphRow>, refs: List<Ref>): Set<String> {
    val tip = refs.firstOrNull { it.type == RefType.LOCAL_BRANCH && it.name == branch }
        ?: refs.firstOrNull { it.type == RefType.REMOTE_BRANCH && it.name == branch }
        ?: return emptySet()
    val byId = rows.associateBy { it.commit.id }
    val seen = mutableSetOf<String>()
    val stack = ArrayDeque<String>().apply { add(tip.commitId) }
    while (stack.isNotEmpty()) {
        val id = stack.removeLast()
        if (!seen.add(id)) continue
        byId[id]?.commit?.parentIds?.forEach { parent -> if (parent in byId) stack.addLast(parent) }
    }
    return seen
}
