# Ticket 35-settings-android

- Statut : prêt pour review (PR)
- Propriétaire humain : @pepito2t (H3)
- Piste : C android+finitions
- Agent : Claude Fable 5.1 (session interactive)
- Model : fable
- Escalations : aucune
- Fichiers autorisés : shared/src/commonMain/kotlin/dev/nitpick/app/ui/settings/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/components/**,
  shared/src/commonMain/composeResources/**

## Fait

- `SettingsScreen` (téléphone) refait selon le handoff §4.4 : `PhoneTopBar`, fond `canvas`,
  cartes `surface` ; Thème en `NitpickSegmentedControl` 48 dp ; fournisseur en
  `NitpickRadioGroup` vertical 52 dp avec sous-titre ; modèle en select pleine largeur ; clé
  API 48 dp avec Show/Hide et aide ; la clé est masquée quand Ollama est choisi (pas de clé).
  Chaque changement est sauvegardé immédiatement, pas de Save sur téléphone.
- `ModelSelector` et `ApiKeyField` extraits de `SettingsDialog` dans `SettingsFields.kt`,
  partagés desktop / téléphone.
- `NitpickSegmentedControl(height)`, `NitpickRadioGroup(vertical, subtitle, rowHeight)`,
  `NitpickTextField(minHeight)`.

## Décisions

- Aide de la clé : même texte que le desktop (« fichier de réglages de l'app »), pas
  « Android Keystore » : c'est la vérité du stockage.
- Sous-titre Ollama : « auto-hébergé sur cette machine, sans clé », le handoff disait
  « demande l'URL d'un serveur » mais aucun champ d'URL n'existe.
- États Android (vide, erreurs) : déjà livrés par #23 (Repos) et #60 (review) ; rien ici.
- Pas de champ « Review server URL » : son comportement arrive avec #48.

## Incertain

- Comparaison visuelle avec `Phone.dc.html` faite sur capture d'émulateur, pas sur appareil.

## Journal

- 19:19 — worktree créé.
- 19:35 — lecture du ticket et du flux téléphone.
- 19:50 — écran, composants, strings ; compile et `check.sh` verts.
