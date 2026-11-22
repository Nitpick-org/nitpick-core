# Ticket 103 — GitLab sign-in on Android and signed-in account backfill

- Status: ready to merge (GitLab deferred)
- Human owner: @pepito2t (H3)
- Track: C android + polish
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Escalations: none
- Allowed files: `shared/src/commonMain/kotlin/dev/nitpick/app/ui/App.kt`,
  `shared/src/commonMain/kotlin/dev/nitpick/app/settings/ForgeClient.kt` (constant `GITLAB`)

## Done

- At startup, a complete connection without `login` (saved before #96) asks the
  forge for its name once (`ForgeClient.fetchLogin`) and saves it; a failure leaves the name empty without
  retrying as long as the connection does not change.

## Deferred

- GitLab: H3 decision of 22/11, GitHub only for now. When the time comes: a GitLab
  application "Nitpick", non-confidential, scopes `read_repository` + `write_repository`, paste
  the Application ID into `ForgeOAuthClientIds.GITLAB`.

## Evidence

- `check.sh` green. No test added: the effect only chains `fetchLogin` (tested in
  `ForgeClientTest`) and `SettingsViewModel.update` (tested in `SettingsViewModelTest`).

## Log

- 00:10 — worktree created, login backfill at startup.
- 00:20 — PR #108; GitLab deferred.
