# Ticket 140 — Pull is a silent no-op while suggestions are applied or a review runs

- Statut : fait
- Propriétaire humain : @pepito2t (H3)
- Piste : B git+desktop, C android
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Fichiers autorisés : `ui/repository/{RepositoryViewModel,SyncStatus,RepositoryScreen}.kt`,
  `ui/phone/PhoneGraphScreen.kt`, strings EN/FR, `RepositoryViewModelTest.kt`

## Fait

- `RepositoryUiState.isPullBlocked` (suggestions appliquées non commitées, ou review en cours) ;
  `pullBranch` l'utilise à la place de la condition inline.
- Desktop : action Pull à 50 % d'opacité, clic ignoré, tooltip « Commit the applied suggestions
  before pulling or switching branch ». Téléphone : bouton Pull désactivé.
- Ligne de statut sync (desktop et Graph téléphone) : même phrase tant que des suggestions sont
  appliquées, pour dire pourquoi au lieu de ne rien faire.
- Test : `pullIsBlockedAndSaysSoWhileSuggestionsAreApplied`.

## Journal

- 02:05 — worktree, fix, check vert.
