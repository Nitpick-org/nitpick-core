# Ticket 34-settings-desktop

- Statut : prêt pour review (PR)
- Propriétaire humain : @pepito2t (H3)
- Piste : C android+finitions (desktop ici)
- Agent : Claude Fable 5.1 (session interactive)
- Model : fable
- Escalations : aucune
- Fichiers autorisés : shared/src/commonMain/kotlin/dev/nitpick/app/ui/settings/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/components/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/designsystem/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/App.kt, shared/src/commonMain/composeResources/**,
  shared/src/commonTest/kotlin/dev/nitpick/app/ui/settings/**

## Fait

- `SettingsDialog` desktop (handoff §3.5) : nav 220 dp avec recherche qui filtre les sections,
  General seul rempli ; Thème en `NitpickSegmentedControl`, fournisseur en `NitpickRadioGroup`,
  modèle en select 300 dp mono, clé API 380 dp avec Show/Hide et aide nommant le fournisseur ;
  Cancel / Save ; Esc et clic dehors = Cancel.
- Brouillon dans `SettingsViewModel` : rien n'est écrit avant Save ; Save ne copie que les quatre
  champs du dialogue sur les réglages courants.
- Deux composants dans `ui/components/` et le catalogue.
- Labels `LlmProvider.label` / `ThemeMode.label` partagés.

## Décisions

- Le téléphone garde `SettingsScreen` (restylé par #35).
- Aide de la clé : « fichier de réglages de l'app », pas « trousseau » : c'est la vérité du
  stockage actuel.
- Pas de champ « Review server URL » : son comportement arrive avec #48, un champ mort serait pire.

## Incertain

- Rendu visuel clair/sombre non comparé à la maquette (pas de clic possible depuis la session).

## Journal

- 19:05 — worktree, lecture du handoff §3.5 et de l'existant.
- 19:25 — dialogue, composants, brouillon, tests ; compile verte.
