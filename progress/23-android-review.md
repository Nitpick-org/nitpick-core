# Ticket 23-android-review

- Status: in progress (issue #23), full loop to be proven with an API key and a private repository
- Human owner: @pepito2t (H3)
- Track: C android+polish
- Agent: Claude Code, main session
- Model: fable
- Escalations: none
- Allowed files: shared/src/jvmSharedMain/git, shared/src/commonMain/kotlin/dev/nitpick/app/ui/{phone,components/NitBottomSheet.kt,App.kt,designsystem}, shared/src/commonMain/kotlin/dev/nitpick/app/ui/repository/{RepositoryViewModel.kt (new functions), RepositoryScreen.kt (RefsSidebar), DiffPanel.kt (FileHunks internal)}, composeResources, shared/src/{jvmTest,commonTest}
- Off-limits (worktree #22 in progress): `ReviewPanel.kt`, `NitpickReview.kt`, `ReviewAnchoring.kt`, `FakeGitRepository.kt`, the bodies of `reviewBranch`/`applySuggestion`/`commitAppliedSuggestions`, `JGitRepository.commit`

## Log

- 18:15 — worktree created on `0f11c21` (#21 merged).
- 18:25 — `JGitRepository.push` (`PushCommand` + credentials; a rejected ref does not throw an exception
  in JGit: we read `RemoteRefUpdate.status` and throw with the status) and `checkout` (local branch,
  or remote branch → local tracking branch, like `git checkout`). 3 `jvmTest` tests: push to
  a local bare repo (HEAD of the bare = HEAD of the clone, `origin/main` up to date), out-of-date push
  rejected (`REJECTED_NONFASTFORWARD`), local/remote checkout. 23 JGit tests green.
- 18:35 — `RepositoryViewModel`: `pushBranch(credentials?)`, `checkoutBranch`, `hasUnpushedCommits`
  (derived from the refs: local branch ≠ `origin/<branch>`), `hasCredentials`, `isPushing`, `pushError`,
  `isCheckingOut`. The clone token stays in session memory for the push (never in the state,
  never saved). 4 tests with a `GitRepository by inner` wrapper local to the test. 13 VM tests green.
- 18:50 — phone UI: `PhoneReviewScreen` (states, summary card, nits grouped by file in
  56 dp rows, file header → `PhoneDiffScreen` that reuses `FileHunks`, commit then push
  bar with token entry if missing), `NitBottomSheet` (`ModalBottomSheet` + `NitSheetContent`
  reusing `ReviewCommentCard`, Dismiss/Apply 56 dp buttons; `NitSheetContent` in the
  catalog), "Nitpick branch" button on Graph, Branches tab tappable → checkout. EN/FR strings.
- 18:55 — `check.sh` green (96 JVM tests). Emulator: clone Hello-World → Branches → tap `origin/test`
  → "on test · 4 commits" (checkout on ART OK) → Nitpick branch → "Review failed: Unknown base:
  main". Fixed: the base is chosen on load among main / origin/main / master /
  origin/master (`defaultBaseRef`, tested). New attempt: "Review · test vs master", error "No API
  key for ANTHROPIC" (expected, no key on the emulator).
- 19:05 — **incident**: the first `git commit` ran `check.sh` through the `pre-commit` hook, with
  `GIT_DIR` and `GIT_INDEX_FILE` exported by git. In-memory JGit (`CloneCommand`) reads these variables:
  the test clones initialized in the worktree's gitdir and **replaced the index** with
  the test repo's (`a.txt`, `b.txt`), hence "invalid object … for 'a.txt'". Branches, `main`
  and source files intact; a few dangling objects in `.git/objects`. Fixed by
  `git read-tree HEAD && git add -A`. Causes fixed: the hook runs `check.sh` with
  `env -u GIT_DIR -u GIT_INDEX_FILE -u GIT_WORK_TREE -u GIT_PREFIX`, and the factory sets `setGitDir`.
  The case of #15 (`git` subprocess) was covered by `TestGitRepo`; this one is the in-memory JGit
  case, which nothing can filter on the test side.
- 19:20 — advisor: two bugs on the ticket's path fixed before the review. (1) After a commit,
  `review` goes back to null and the commit/push bar lived in the "result" branch: Push was
  unreachable. The bar is out of the states and shows itself (`appliedFiles`, `isPushing`,
  `hasUnpushedCommits`). (2) After a rejected push without a token, the token field never appeared:
  the error is merged into the "to push" branch, token field optional. Plus `checkoutError`
  displayed on Graph. Hook: issue #58 + PR #59 (separate hotfix for the whole team, to merge before
  H2's next commit). `check.sh --demo` green.

- 19:30 — ponytail review: the Diff screen no longer shows the first file by default when the nit's
  path is not in the diff (empty state); the manual check "Push visible after commit"
  is in the checklist of `docs/DEMO.md`.

- 19:40 — ponytail review: no checkout during an in-progress review (the old branch's nits
  would land on the new one).
- 19:55 — human decision: **the mobile app has a GitHub or GitLab connection** as its base.
  `Settings.gitConnection` (`GitConnection(host, token)`, serialized with the settings), Connect
  screen (GitHub/GitLab choice, masked token) shown as long as no account is connected, "Account"
  action on Repos to change it. The clone no longer has a token field, push no longer asks for one:
  the `Credentials` come from the settings (`x-access-token` for GitHub, `oauth2` for GitLab). Removed:
  the ViewModel's session credentials, `hasCredentials`, strings `clone_token_*` and `push_token_*`.
  3 settings tests (user name per forge, serialization), VM tests adjusted. `check.sh --demo` green
  (99 JVM tests, 67 Android host).

- 20:05 — ponytail review: `SettingsViewModel.isLoaded` and `App` shows the phone flow only once
  the settings are read (no more Connect screen flash at startup); editing the account no longer
  prefills the token (empty field = we keep the old one).

## Decisions

- `checkout` implemented here (it is in the `GitRepository` contract): after a clone, the phone
  is on the default branch and has nothing to re-read. #42 keeps `pull`. Assumption noted without a
  human answer at commit time.
- Git connection in the settings (H3 decision): one token per device, used by clone, fetch and
  push. Stored like the API key, in the app's private settings file (Keystore: later).
- "Something to push" is derived from the refs (local ≠ `origin/<branch>`), not from a flag to
  reset in the #22 functions.
- Review base chosen on load: a GitHub clone often has no local `main`
  (Hello-World only has `master`). The desktop selector stays in charge afterwards.
- No floating FAB: full-width 56 dp button at the bottom, like the other phone screens.

## Uncertain

- Full loop (nits → sheet → apply → commit → push) not replayed on the emulator: the API
  key is needed in the emulated app's Settings and a private repository with a branch to review.
- Commit author identity on Android: JGit without `~/.gitconfig` can produce an empty author or
  `u0_aNNN@localhost`. To be checked on the first real commit; possible fix in
  `JGitRepositoryFactory.clone` (local config `user.name/email`).
- `ModalBottomSheet` compiles on desktop and Android; not visually verified on desktop (not used).
- The handoff "suggestion applied" snackbar replaced by a green line in the commit bar.

## Blockers

- None.

## Lesson to add to the team rules (H2)

- In-memory JGit reads `GIT_DIR`/`GIT_INDEX_FILE` like the CLI: a hook that runs the tests must
  remove them from the environment (`env -u`), otherwise a test clone overwrites the repo's index.

## Next

- Replay the loop with a key + a private repo, verify the commit on GitHub; rebase after #22 merges.
