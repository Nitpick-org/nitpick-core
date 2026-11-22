# Ticket 119 — Pushed confirmation, review-aware commit message, suggestions that fix stale comments

- Status: done, to merge
- Human owner: @pepito2t (H3)
- Track: A core (prompt) + B/C UI desktop and phone
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Allowed files: `ui/repository/{RepositoryViewModel,SyncStatus,PushedBanner,CommitMessage,ReviewPanel}.kt`,
  `ui/phone/PhoneReviewScreen.kt`, `review/{ReviewPrompt,ReviewResponseParser}.kt`, `domain/Review.kt`
  (optional `title` field), EN/FR strings, `docs/demo/android-09*`, `android-10*`, `docs/DEMO.md`

## Done

- `RepositoryUiState.lastPushedBranch`: set on a successful push or publish, cleared on
  commit, fetch, pull, checkout and at the start of the next push. `PushedBanner` (check mark that "pops"
  in a spring, line that fades in 400 ms) at the bottom of the phone Review screen and the desktop
  Review panel; `syncStatusMessage` says "Pushed to origin/branch" in the desktop status bar
  and on the phone Graph.
- `commitMessageFor(applied)`: `fix:`/`refactor:` + titles of the applied nits (`title` field
  requested from the model, fallback to the first sentence of the message), 72 characters max. Recalculated on
  each Apply as long as the user has not touched the message.
- Prompt: "widen startLine..endLine to include a comment the fix makes wrong". On
  the emulator the model preferred separate nits for stale comments: equivalent
  result, the wrong comments are fixed.

## Evidence

- `check.sh` green; `CommitMessageTest` 3, `ReviewPromptTest` 5, `ReviewResponseParserTest` 3,
  `RepositoryViewModelTest` (lastPushedBranch after push, `fix: wrong lane` message).
- Emulator: second pass on `pepito2t/OpenTab` (faulty `perf(mru)`) → 3 nits including 2 on
  the stale comments → subject "fix: correct eviction comment, restore mru.count base for
  unseen ranks" → push → banner, commit `f91a419` on GitHub.
- Not verified on screen: desktop (same composable, same `syncStatusMessage`).

## Journal

- 00:50 — ticket, worktree, reading of push/commit/prompt.
- 01:00 — implemented, check green, replayed on the emulator with screenshots.
