package dev.nitpick.app.domain

data class WorkingTreeStatus(
    val staged: List<String>,
    val unstaged: List<String>,
    val untracked: List<String>,
    val conflicted: List<String>,
) {
    val isClean: Boolean
        get() = staged.isEmpty() && unstaged.isEmpty() && untracked.isEmpty() && conflicted.isEmpty()
}
