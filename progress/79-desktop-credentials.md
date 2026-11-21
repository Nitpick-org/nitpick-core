# Ticket 79-desktop-credentials

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : B desktop
- Agent : lead
- Fichiers : `ui/settings/{SettingsDialog,SettingsFields}.kt`, `ui/App.kt`,
  `composeResources/values{,-fr}/strings.xml`

## Journal

- 23:15 — section **Git account** dans Settings : host GitHub/GitLab + token masqué
  (Show/Hide), aide lecture/écriture. Écrit `Settings.gitConnection`, déjà sérialisé.
- 23:18 — desktop Fetch/Pull/Push n'envoient les credentials que si `gitConnection` est
  complet ; un token vide ne remplace pas un remote public.
- Tests `SettingsTest` déjà présents pour `GitConnection.toCredentials()` et la persistance.

## Décisions

- Un seul compte forge mémorisé dans les settings (comme Android), pas un token par repo.
- Le token reste masqué et n'est jamais loggé ; il part uniquement dans l'en-tête JGit.

## Vérifications

- `:shared:jvmTest` + `:desktopApp:compileKotlin` verts.
- Pas de test UI Compose ; le branchement credentials est couvert par `SettingsTest` et le
  passage direct de `Settings` à `App`.

## Incertain

- Clone desktop garde son propre champ token (#45) ; la section Settings sert à
  fetch/pull/push des repos déjà ouverts, pas à préremplir le clone.
