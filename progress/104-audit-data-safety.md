# Ticket 104-audit-data-safety

- Status: ready for PR (#113, rebased)
- Human owner: @dovatgabriel
- Track: B git+desktop, C android+polish
- Agent: Claude Code, main session (quality audit of 22/11)
- Model: Fable, picked by hand by H1
- Escalations: none
- Files: `jvmSharedMain/.../git/JGitRepository.kt`,
  `commonMain/.../ui/settings/SettingsViewModel.kt`,
  `commonMain/.../ui/repository/{RepositoryViewModel,ReviewPanel}.kt`,
  `commonMain/.../ui/phone/PhoneReviewScreen.kt`, tests `FileSettingsStoreTest` and
  `RepositoryViewModelTest`

## Context

This branch carried the four fixes of #104. H2 merged them first in #109, and #116 completed.
Rebased on `main` at H1's request, it keeps only what `main` does not have.

## Journal

- 00:23 — worktree created, four fixes written (pull, minSdk 33, `saveDraft`, `Files.move`).
- 00:35 — #109 merged by H2 with the same fixes: PR #113 closed as a duplicate.
- Rebase on `main` (after #109 and #116):
  - pull commit: dropped, `main` already has the fix and its messages (#116);
  - minSdk commit: dropped, already in #109 and #116;
  - `saveDraft`: reduced to the "all the draft except the recent repos" form, so that a
    field added to the dialog is no longer forgotten a third time;
  - `Files.move`: reduced to the "two saves in a row" test, which #109 did not have;
  - `requireAccepted`: kept as is, `push` and `publishBranch` share the check.
- Two follow-ups from the `reviewer` agent's review of #116:
  - Apply and Commit are disabled during a pull or a checkout, instead of ignoring the tap;
  - `actionError` is reset when a pull or a shared review replaces the review.

## Decisions

- The `SettingsViewModelTest` tests stay those of `main`: they already cover both
  behaviours, the rewrite of `saveDraft` changes nothing observable.
- The two follow-ups of #116 are in this PR rather than in a third one: they are a few-line
  touch-ups to code I just had merged.

## Checks

- Red then green test: `aPullThatDropsTheReviewAlsoDropsItsActionError` (the error "Snippet
  not found for n1" stayed in the state after the pull).
- `./scripts/check.sh` green on the rebased branch.

## Uncertain

- Buttons disabled on phone and desktop: no Compose test, to be checked by hand.
- Windows: the two-saves test cannot fail on macOS or Linux.

## Next

- Human review. The rest of the audit is in #107.
