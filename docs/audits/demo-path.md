# Audit lane: demo-path correctness (#128)

- Pin: `origin/main` = `54c37a2` (22/11 01:10). Read in a detached worktree, then with `git show 54c37a2:<path>` after that worktree was removed mid-audit.
- Method: one independent read-only reviewer over `review/`, `ui/repository/`, `ui/phone/`, `git/`, `server/` and their tests; the three P1 re-read by H3 at the cited lines. No Gradle run in this lane.
- Result: 0 P0, 3 P1, 4 P2. Each P1 is a 15-minute fix with a missing test.

## P1

| # | Finding | Evidence | What goes wrong | Fix | Cost | Conf. | Impact |
| --- | --- | --- | --- | --- | --- | --- | --- |
| P1-1 | The tool loop throws away the model's final answer after the last tool round | `review/KoogLlmClient.kt:78-118`: `repeat(MAX_TOOL_ROUNDS)` checks answer N, runs its tools, fetches answer N+1; after the 4th iteration the loop exits and throws "exceeded 4 tool rounds" without looking at that answer. `ReviewToolbox.kt:14` allows 6 calls, so only 3 rounds really work. | The model lists files, reads three files one call per round, then returns valid JSON: the JSON is dropped. `RepositoryViewModel.kt:63` maps the `ReviewProviderException` to `ReviewFailure.Network`, so the user sees a network error with Retry. The repair retry in `LlmReviewAgent` does not run (it only catches `ReviewResponseException`). | After the loop, return `textOf(answer)` when the last answer has no tool call, throw only otherwise. | 15m | high on the logic, medium on frequency (the nudge after each round makes it rarer) | demo, pitch ("the agent reads neighbouring files") |
| P1-2 | Checking out a branch, or opening a shared review on another branch, with applied uncommitted nits strands those edits | `RepositoryViewModel.kt:551` makes `pullBranch` refuse when `appliedFiles` is not empty; `checkoutBranch` (`:593-597`) and `openSharedReview` (`:432-440`) have no such guard, and `switchTo` clears `appliedIds`, `appliedFiles` and `commitMessage` (`:621-623`). `RepositoryScreen.kt:147` and `PhoneGraphScreen.kt:224` pass the checkout straight through. | Apply a nit, click another branch in the sidebar (or open a shared review of another branch): JGit carries the non-conflicting edit across, the commit bar disappears because `appliedFiles` is empty, and the app has no other commit path. Nothing is lost on disk, but it cannot be committed from the app and the next review on that branch reviews it. | Add `|| _state.value.appliedFiles.isNotEmpty()` to the early return of `checkoutBranch` and `openSharedReview`, as `pullBranch` does. | 15m | high | demo (the phone shared-review flow switches branches) |
| P1-3 | A new review keeps the old `appliedIds`, and nit ids are positional | `ReviewResponseParser.kt:49` builds ids as `"$filePath:$startLine:$index"`; `reviewBranch` (`:268-276`) replaces `review` without resetting `appliedIds`; the desktop "Nitpick branch" button is always enabled (`RepositoryScreen.kt:117-119`). | Apply nit `Foo.kt:12:0`, press Nitpick branch again before committing: a new nit at index 0 on `Foo.kt:12` shows "Applied" with no Apply button (`ReviewPanel.kt:143-155`), `applySuggestion` no-ops it (`:296`) and commit drops it (`:350`); the proposed commit message uses the wrong set (`:302-308`). | Set `appliedIds = emptySet()` when a new review lands; keep `appliedFiles` so the commit still works. | 15m | medium (needs an id collision) | demo (re-review is a natural demo move) |

## P2

| # | Finding | Evidence | Fix | Cost | Conf. |
| --- | --- | --- | --- | --- | --- |
| P2-1 | Applying a nit does not re-anchor the other nits in the same file | `applySuggestion` (`:297-309`) writes without re-anchoring, only commit does (`:350`); `ReviewAnchoring.findSnippet` (`:55`) takes the exact match at the stale `preferredStart` first. Nit A grows the file by 2 lines; nit B's one-line snippet (a `}` or `return null`) also appears at B's old line: B edits the wrong occurrence. Usually only the "line N" label is stale. | Re-anchor the file's comments with the private `reanchor()` after the write | 15m | medium |
| P2-2 | The desktop "Pushed" banner never shows once every nit is applied | `ReviewPanel.kt:168` is in the `else` branch (review with comments); commit sets `review = null` when nothing remains (`:357`). Phone is fine (`PhoneReviewScreen.kt:227`). `DEMO.md:93` claims the banner on Android only. Same as UX lane P2-4. | Render the banner in the `review == null` branch too | 15m | high |
| P2-3 | One unreadable file fails the whole review | `ReviewToolbox.readFile` (`:94`) calls `repository.readFile`, which throws `IllegalArgumentException` from `resolveInWorkTree` (`JGitRepository.kt:333-337`) for a tracked symlink pointing outside the repo; not caught, so it ends as `ReviewFailure.Other`. `buildReviewContext` (`ReviewContext.kt:78`) has the same hole for a changed symlink. The escape itself is blocked. | Catch it in `readFile` and return an error `ToolOutput` | 15m | medium |
| P2-4 | The 90 s timeout covers up to 6 provider calls | `REVIEW_TIMEOUT` (`:45`) wraps 1 initial, 4 tool-round and 1 repair call, each allowed 32k output tokens (`KoogLlmClient.kt:31`). The Android replay in `DEMO.md` at 00:45 passed. `RealReviewCallTest` is opt-in, so nothing measures it. | Measure once on the demo repo; raise or make per-call | 1h | low |

## Checked, no finding

- `ReviewToolbox` path checks: absolute, drive letter, `..`, `.`, empty segments, `.git` ignoring case, tracked-only, reviewable filter, call and byte budgets.
- `resolveInWorkTree` canonicalises, so symlinks cannot escape; `.GIT/config` canonicalises to `.git` on macOS (probe run by the reviewer).
- Apply and commit share a mutex and re-check state inside it: `applyingTheSameNitTwiceDoesNotPatchTwice`, `committingTwiceCreatesOneCommit`.
- Apply and commit are ignored while a pull or checkout rewrites the tree: `anApplyWhileAPullRunsIsIgnored`.
- `TimeoutCancellationException` caught before `CancellationException` (`:277`): `aReviewThatNeverAnswersEndsAsATimeout`.
- Provider errors are re-wrapped with the class name only (`KoogLlmClient.kt:131-133`); the API key never enters UI state.
- `withoutCredentials` strips userinfo before share and list; clone and push credentials go through a `CredentialsProvider`, not the URL.
- Pull on a dirty tree is fast-forward only; a conflict on a clean tree hard-resets; `ABORTED` and `CheckoutConflictException` carry a clear message.
- JGit commit rejects a blank message and sets `allowEmpty = false`.
- Model comments are filtered to the diffed paths (`LlmReviewAgent.kt:44`); a mis-anchored nit falls back to the whole file.
- Server: body capped at 1 MB with or without Content-Length, UUID id regex, repoUrl validation, write-then-rename persistence, URL normalisation for scp and https.

## Coverage gaps

- Not read: `JGitRepositoryTest` and `TestGitRepo` bodies, `ReviewToolboxTest`, `LlmReviewAgentTest`, server tests, `DiffPanel`, `NitBottomSheet` internals, `CommitMessage.kt`, `PhoneFlow` beyond the review hook, `androidTest`.
- Not run: no Gradle in this lane (the pinned worktree vanished mid-audit).
- Unverified: whether Koog 1.3 wraps every HTTP failure in `LLMClientException`; if not, a raw message reaches `ReviewFailure.Other`.
- Unverified: whether raw JGit `TransportException` text (which embeds the remote URI) reaches `pushError`, `fetchError` or clone errors (`:189, :498, :524, :542`) when a remote URL carries a token.
- Not tested: concurrent push and commit (`pushBranch` does not check a commit in progress).
