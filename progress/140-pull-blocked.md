# Ticket 140 — Pull is a silent no-op while suggestions are applied or a review runs

- Status: done
- Human owner: @pepito2t (H3)
- Track: B git+desktop, C android
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Allowed files: `ui/repository/{RepositoryViewModel,SyncStatus,RepositoryScreen}.kt`,
  `ui/phone/PhoneGraphScreen.kt`, EN/FR strings, `RepositoryViewModelTest.kt`

## Done

- `RepositoryUiState.isPullBlocked` (applied suggestions not committed, or a review running);
  `pullBranch` uses it instead of the inline condition.
- Desktop: Pull action at 50 % opacity, click ignored, tooltip "Commit the applied suggestions
  before pulling or switching branch". Phone: Pull button disabled.
- Sync status line (desktop and phone Graph): same sentence while suggestions are
  applied, to say why instead of doing nothing.
- Test: `pullIsBlockedAndSaysSoWhileSuggestionsAreApplied`.

## Log

- 02:05 — worktree, fix, check green.
