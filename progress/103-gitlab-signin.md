# Ticket 103 — GitLab sign-in on Android and signed-in account backfill

- Statut : en cours (id GitLab à renseigner)
- Propriétaire humain : @pepito2t (H3)
- Piste : C android + finitions
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Escalations : aucune
- Fichiers autorisés : `shared/src/commonMain/kotlin/dev/nitpick/app/ui/App.kt`,
  `shared/src/commonMain/kotlin/dev/nitpick/app/settings/ForgeClient.kt` (constante `GITLAB`)

## Fait

- Au démarrage, une connexion complète sans `login` (enregistrée avant #96) demande son nom à la
  forge une fois (`ForgeClient.fetchLogin`) et le sauve ; un échec laisse le nom vide sans
  réessayer tant que la connexion ne change pas.

## À faire

- `ForgeOAuthClientIds.GITLAB` : application GitLab « Nitpick », non confidentielle, scopes
  `read_repository` + `write_repository`, à créer par H3.

## Preuves

- `check.sh` vert. Pas de test ajouté : l'effet ne fait qu'enchaîner `fetchLogin` (testé dans
  `ForgeClientTest`) et `SettingsViewModel.update` (testé dans `SettingsViewModelTest`).

## Journal

- 00:10 — worktree créé, rattrapage du login au démarrage.
