# Dogfooding: Nitpick re-reviews our own branches

Proof for the criterion "the review re-reviews the agents' branches" (`AGENTS.md`, way of
working). One entry per pass, with what the review found and what we did about it.

## 22/11 00:50 — `ticket/104-audit-data-safety` (Gabriel, 6 commits, 11 files)

Desktop, Anthropic `claude-sonnet-5-5`, diff against `main`. Screenshot: `dogfooding-104-review.png`.

| Nit | File | Check | Follow-up |
| --- | --- | --- | --- |
| With local changes and a diverged branch, JGit answers `ABORTED` without throwing an exception: the `catch` that writes "Uncommitted changes…" never runs, the user sees "Pull failed: Aborted". | `JGitRepository.kt:243-270` | Reproduced on `main`: the `aDirtyTreeIsNotResetWhenThePullDiverged` test passes because it only checks the exception type; the real message is `Pull failed: Aborted`. | Issue #114 |
| `Files.move(…, ATOMIC_MOVE)` throws `AtomicMoveNotSupportedException` on some file systems, with no fallback. | `FileSettingsStore.kt` | True on the reviewed branch; the merged version (#109) already has the `REPLACE_EXISTING` fallback. | Nothing to do |

Assessment: 2 nits, 1 real bug still present on `main`, 1 real problem already fixed before the merge. Zero false positives.
