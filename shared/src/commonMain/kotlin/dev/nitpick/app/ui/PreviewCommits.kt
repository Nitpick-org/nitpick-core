package dev.nitpick.app.ui

import dev.nitpick.app.domain.Commit

/**
 * Preview data for the commit graph, used until the desktop repo picker (#15) feeds real
 * commits. The shape deliberately includes a merge, a side branch, a lane reuse and an
 * orphan commit so the renderer can be checked by eye.
 */
val previewCommits: List<Commit> = listOf(
    commit(
        id = "9f2c1a7b4e8d6c5a3b1f0e9d8c7b6a5f4e3d2c1b",
        message = "Merge branch 'review/nits'",
        author = "Elliot",
        seconds = 1_795_267_200,
        parents = listOf(
            "6b4d2c1a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c",
            "1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b",
        ),
    ),
    commit(
        id = "6b4d2c1a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c",
        message = "Render inline nits in the diff",
        author = "Elliot",
        seconds = 1_795_266_600,
        parents = listOf("c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2"),
    ),
    commit(
        id = "1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b",
        message = "Ask Koog for anchored review comments",
        author = "Gabriel",
        seconds = 1_795_266_000,
        parents = listOf("c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2"),
    ),
    commit(
        id = "c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2",
        message = "Load the real commit log with JGit",
        author = "Gabriel",
        seconds = 1_795_264_800,
        parents = listOf("d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3"),
    ),
    commit(
        id = "d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3",
        message = "Add the section 6 contracts",
        author = "Thomas",
        seconds = 1_795_263_600,
        parents = listOf("e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4"),
    ),
    commit(
        id = "f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5",
        message = "Try a local-only review experiment",
        author = "Elliot",
        seconds = 1_795_262_400,
        parents = listOf("missing-parent"),
    ),
    commit(
        id = "e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4",
        message = "Bootstrap the Nitpick KMP project",
        author = "Thomas",
        seconds = 1_795_261_200,
        parents = emptyList(),
    ),
)

private fun commit(
    id: String,
    message: String,
    author: String,
    seconds: Long,
    parents: List<String>,
) = Commit(
    id = id,
    shortMessage = message,
    fullMessage = "$message\n\nPreview commit for the graph renderer.",
    authorName = author,
    authorEmail = "${author.lowercase()}@nitpick.dev",
    timestampEpochSeconds = seconds,
    parentIds = parents,
)
