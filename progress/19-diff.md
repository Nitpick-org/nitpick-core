# Ticket 19-diff

- Status: done, to push and merge
- Human owner: @dovatgabriel
- Track: B git + desktop
- Agent: Claude Code, interactive session
- Model: opus
- Escalations: none
- Allowed files: shared/src/jvmSharedMain/.../git/JGitRepository.kt, shared/src/jvmTest/.../git/,
  shared/src/commonMain/.../ui/repository/{DiffPanel,RepositoryScreen,RepositoryViewModel}.kt,
  shared/src/commonMain/.../ui/App.kt, ui/components/NitpickReview.kt (DiffLineRow),
  composeResources/values{,-fr}/strings.xml, shared/src/commonTest/.../ui/repository/

## Log

- 17:35: worktree created. PR #50 (H2) already shipped a first `diffAgainst` and a
  minimal Diff tab, mixed with #22. Human decision: extract and complete #19
  rather than redo it.
- 17:40: red JGit tests, then `diffAgainst`: merge-base, renames, working tree,
  ignored files excluded.
- 17:45: ViewModel: `baseRef`, `isDiffLoading`, `diffError`, `selectedDiffPath`,
  `selectBase`, `selectDiffFile`, `refreshDiff`. 7 green tests.
- 17:50: `DiffPanel` per UI-HANDOFF §3.3. `check.sh` green.
- 18:20: ponytail review: untracked excluded, remote branches in the selector, no more
  flicker on reload, `shortcut:` on the hash.
- 17:55: #50 merged in the meantime. Rebased on `main`, conflicts resolved: #50's review,
  apply and commit stay, and reload the diff via `refreshDiff`.

## Decisions

- The left side is the **merge-base** between HEAD and the base, not the tip of the base. Without
  that, a commit that landed on `main` after the fork appears as removed by the
  branch. Without a common ancestor, we compare against the base itself.
- The right side is the **working tree** (#51 review request): an applied suggestion
  appears immediately. **Untracked** files are excluded (`TrackedFilter`):
  the review sends each file of the diff to the provider, and a secrets file missing from
  `.gitignore` must not leave. A new file counts as soon as it is staged (`git add`).
  Ignored files are also excluded by the `FileTreeIterator`.
- Lockfiles, binaries and generated files: already filtered for the review by `isReviewable`
  (`review/ReviewContext.kt`, #20). The diff shows them; a binary displays "Binary file,
  not shown".
- The diff reloads when the repo opens, on base change, when opening the Diff tab
  (the working tree may have changed) and after apply or commit.
- The base selector lists local (except the current one) and remote branches: a clone
  `-b feature` has no local `main`, and the user stayed stuck on the error.
- A reload keeps the displayed diff; the spinner only appears when there is nothing to show.
- `DiffLineRow` displayed "null" for the missing number of an added or removed line:
  fixed.

## Verification

- `:shared:jvmTest`: JGitRepositoryTest 17 tests and RepositoryViewModelTest 7 tests, 0 failure.
- `./scripts/check.sh` green.

## Uncertain

- Long lines cut off on the right (no horizontal scroll in `DiffLineRow`).
- `FileTreeIterator` hashes every tracked file on every load: fast on a demo repo,
  to measure on a big repo.
- Android not wired here (track C, #21/#23); `diffAgainst` lives in `jvmSharedMain`, but no
  test on ART covers it yet.

## Next

- #22: the review loop uses this diff.
