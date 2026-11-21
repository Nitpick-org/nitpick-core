# Ticket 81-android-branch-card

- Statut : en cours
- Propriétaire humain : @pepito2t (H3)
- Piste : C android+finitions
- Agent : Claude Code, session humaine
- Model : Fable 5.1
- Escalations : aucune
- Fichiers autorisés : shared/src/commonMain/.../ui/phone/PhoneGraphScreen.kt, composeResources/values{,-fr}/strings.xml

## Journal

- 23:37 — worktree créé sur `fb13976`.
- 00:05 — `BranchCard` sous la barre du Graph : nom de branche (mono), upstream ou « No upstream branch », `↑n ↓m`, nombre de commits, puis trois `NitpickButton` Secondary de 44 dp à largeur égale : Fetch / Pull n / Push n (Publish sans upstream, rien sans `origin`). Les trois actions quittent la barre du haut, qui garde Shared et Settings. La ligne d'état de sync (« Pulled new commits », erreurs) reste sous la liste.

## Décisions

- Le compteur de commits reste dans la carte (il était dans l'en-tête remplacé) ; le handoff ne le demande pas mais le retirer serait une perte d'information.
- Pas de sélecteur `branche ▾` dans la carte : l'onglet Branches fait déjà le checkout (#42).

- 00:20 — mesuré sur l'émulateur (420 dpi, uiautomator) : boutons 48 dp de haut (minimum tactile Material autour des 44 dp demandés), 91 dp de large chacun. Captures light/dark dans `docs/demo/81-branch-card-{light,dark}.png` (un dossier `captures/` est ignoré par `.gitignore`, première tentative perdue).
- 00:35 — review ponytail : un push raté depuis le Graph n'affichait rien (`syncStatusMessage` ignorait `pushError`, seul l'écran Review le montrait) ; la ligne d'état le montre maintenant, sur le téléphone comme dans la barre d'état desktop.

## Incertain

- Rien.
