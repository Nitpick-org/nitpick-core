# Ticket 45-desktop-clone

- Statut : fait, à relire puis merger (issue #45). Preuve manuelle avec un dépôt privé à faire.
- Piste : B
- Agent : Claude Code, session principale
- Model : opus 5.5
- Escalations : aucune
- Fichiers : `ui/repository/CloneDialog.kt` (nouveau), `ui/repository/RepositoryScreen.kt`,
  `ui/App.kt`, `composeResources/values{,-fr}/strings.xml`,
  `commonTest/.../ui/repository/CloneCredentialsTest.kt`

## Journal

- Branche `ticket/45-desktop-clone` depuis `main`. #21 est fermée : `GitRepositoryFactory.clone`
  et `RepositoryViewModel.cloneRepository` existent, rien à ajouter côté git.
- `CloneDialog` : URL, token masqué (Show/Hide), dossier de destination (bouton Choose… qui
  réutilise le sélecteur natif `pickDirectory` déjà passé à `App`), nom du dossier déduit de
  l'URL. Le dialogue reste ouvert pendant le clone (bouton « Cloning… ») et affiche l'erreur
  JGit sur place ; il se ferme quand le dépôt est chargé.
- `cloneCredentials(url, token)` : token vide → clone anonyme ; sinon `x-access-token`, ou
  `oauth2` si l'URL contient `gitlab`. 3 tests JVM exécutés, 0 échec.
- Toolbar : « Clone repository… » à côté de « Open repository… », seulement si la plateforme a
  un sélecteur de dossier (desktop). `recentRepositories` : effet existant de `App`.
- `check.sh` vert.

## Décisions

- Le token vit dans un `remember` du dialogue : ni `settings.json`, ni saved state, ni l'état
  du ViewModel (déjà garanti par #21).
- Pas de choix GitHub/GitLab dans le dialogue : on le déduit de l'URL.
- Aide du token : texte exact de l'issue, nouvelle string `clone_token_help`.

## Incertain

- Les erreurs « token invalide » / « réseau » sont le message brut de JGit, comme sur Android.
- Push/pull après un clone privé utilisent `Settings.gitConnection`, pas le token du dialogue :
  sans compte configuré, le push d'un dépôt privé échouera.
- Pas vérifié à l'écran : un humain doit cloner un dépôt privé avec un token (critère 1).

## Suivant

- Preuve manuelle : `./gradlew :desktopApp:run`, Clone repository…, dépôt privé + token.
