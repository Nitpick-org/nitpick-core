package dev.nitpick.app.graph

import dev.nitpick.app.domain.Commit

/**
 * One rendered line of the commit graph.
 *
 * [lanesBefore] and [lanesAfter] are snapshots of the active lanes around this commit; a
 * non-null value is the commit id that lane is waiting for. The UI uses them to draw the
 * vertical lines that pass through the row. [incomingLanes] are the lanes that converge on
 * this commit (several children, or simply the lane that expected it), and [parentLanes]
 * are the lanes this commit flows into, in [Commit.parentIds] order.
 */
data class CommitGraphRow(
    val commit: Commit,
    val lane: Int,
    val colorIndex: Int,
    val incomingLanes: List<Int>,
    val parentLanes: List<Int>,
    val lanesBefore: List<String?>,
    val lanesAfter: List<String?>,
)

private const val MaxCommits = 500
private const val LaneColorCount = 8

/**
 * Assigns lanes to a newest-first commit list.
 *
 * The invariant is simple: `active[i]` is the commit that lane `i` is currently waiting to
 * render. When a commit appears, every lane waiting for it converges on one lane; its first
 * parent continues there, and the other parents take an existing lane or the first free one.
 * A lane is released as soon as the commit it was waiting for has no known parent, so a
 * later branch can reuse it.
 */
fun buildCommitGraph(commits: List<Commit>): List<CommitGraphRow> {
    val limited = commits.take(MaxCommits)
    val knownIds = limited.mapTo(HashSet(limited.size)) { it.id }
    val active = ArrayList<String?>()
    val rows = ArrayList<CommitGraphRow>(limited.size)

    for (commit in limited) {
        val lanesBefore = active.toList()
        val expectedBy = active.indices.filter { active[it] == commit.id }
        val lane = if (expectedBy.isNotEmpty()) {
            expectedBy.first()
        } else {
            firstFreeLane(active)
        }

        // The commit consumes every lane that was waiting for it; the chosen lane becomes
        // the dot, the others converge into it.
        expectedBy.forEach { active[it] = null }
        active[lane] = null

        val parentLanes = ArrayList<Int>(commit.parentIds.size)
        val handledParents = HashSet<String>(commit.parentIds.size)
        for ((parentIndex, parentId) in commit.parentIds.withIndex()) {
            if (parentId == commit.id) continue
            if (parentId !in knownIds) continue
            if (!handledParents.add(parentId)) continue

            val existingLane = active.indexOf(parentId)
            val parentLane = if (existingLane >= 0) {
                existingLane
            } else if (parentIndex == 0 && active[lane] == null) {
                lane
            } else {
                firstFreeLane(active)
            }
            active[parentLane] = parentId
            parentLanes += parentLane
        }

        rows += CommitGraphRow(
            commit = commit,
            lane = lane,
            colorIndex = lane % LaneColorCount,
            incomingLanes = expectedBy,
            parentLanes = parentLanes,
            lanesBefore = lanesBefore,
            lanesAfter = active.toList(),
        )
    }
    return rows
}

private fun firstFreeLane(active: MutableList<String?>): Int {
    val free = active.indexOf(null)
    if (free >= 0) return free
    active += null
    return active.lastIndex
}
