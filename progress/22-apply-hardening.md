# Ticket 22-apply-hardening

- Status: ready for review (PR)
- Human owner: @zooldyyk (H2)
- Track: A core + B desktop
- Agent: Claude Fable 5.1 (interactive session)
- Model: fable
- Escalations: none
- Allowed files: shared/src/commonMain/kotlin/dev/nitpick/app/review/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/repository/RepositoryViewModel.kt,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/repository/ReviewPanel.kt,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/components/NitpickReview.kt,
  shared/src/jvmSharedMain/kotlin/dev/nitpick/app/git/JGitRepository.kt,
  shared/src/commonMain/composeResources/**, shared/src/{commonTest,jvmTest}**

## Done (rest of #22 after the 21/11 audit)

- A single anchoring: `ReviewAnchoring` (lines compared after `trim()`, closest occurrence
  to the announced line) is used by Apply, by re-anchoring and by the agent's "whole file"
  fallback; `Anchoring.kt` deleted.
- Apply and commit serialised by a `Mutex` in `RepositoryViewModel`.
- `FakeGitRepository` written in memory; ViewModel test review → apply → commit.
- `JGitRepository.commit` refuses an empty commit (`setAllowEmpty(false)`), test on a temporary repository.
- `LocalReviewAgent` sorts BUG first, like the Koog agent.
- UI: "whole file" / "fichier entier" for a whole-file nit; "Dismiss" and
  "N nits on M changed files" go through the EN/FR resources.

## Uncertain

- Apply and commit with a real Koog nit remain to be replayed by hand (`check.sh --demo`).

## Log

- 18:50 — worktree created; #19 closed (delivered by #53), took over the rest of #22.
- 19:10 — six audit points fixed, tests green (anchoring 7, VM 8, JGit 18).
