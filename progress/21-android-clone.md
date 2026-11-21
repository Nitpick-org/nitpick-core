# Ticket 21-android-clone

- Statut : fini, à relire puis merger (issue #21)
- Propriétaire humain : @pepito2t (H3)
- Piste : C android+finitions
- Agent : Claude Code, session principale
- Model : fable
- Escalations : aucune
- Fichiers autorisés : shared/src/jvmSharedMain/git, shared/src/commonMain/kotlin/dev/nitpick/app/{ui,settings/Settings.kt}, shared/src/commonMain/composeResources, shared/src/{jvmTest,commonTest}, androidApp
- Hors limites (worktree #20 en cours) : `AppContainer.kt`, `domain/Review.kt`, `FakeGitRepository.kt`, `shared/build.gradle.kts`, `gradle/libs.versions.toml`, `docs/RESEARCH.md`

## Journal

- 17:26 — worktree créé. #21 choisie : seule P0 non bloquée dont rien ne dépend ; #23 attend #20 et #21.
- 17:30 — `JGitRepositoryFactory.clone` (`Git.cloneRepository` + `UsernamePasswordCredentialsProvider`)
  et `JGitRepository.fetch`. 3 tests `jvmTest` sur un dépôt local en `file://` : clone (log,
  branche, `origin/main`), clone raté sans dossier résiduel, fetch qui avance `origin/main` sans
  bouger la branche locale. 16 tests JGit verts.
- 17:33 — `RepositoryViewModel.cloneRepository(url, path, credentials)` partage `load()` avec
  `openRepository`. 2 tests avec une factory locale au test (pas de modification du fake partagé).
  `Settings.withRecentRepository` (tête de liste, sans doublon, 10 max) + 2 tests.
- 17:38 — flux téléphone dans `ui/phone/` : `PhoneFlow` (Repos → Clone → Graph), `ReposScreen`
  (dépôts récents + bouton Clone), `CloneScreen` (URL, token masqué avec Afficher/Masquer, nom du
  dossier déduit de l'URL, erreur affichée sur place), `PhoneGraphScreen` (onglets Commits /
  Branches, réutilise `CommitGraphList` et `RefsSidebar`). `App` choisit ce flux sous 600 dp de
  large et pousse chaque dépôt ouvert dans `recentRepositories`. Strings EN + FR.
  `MainActivity` passe `filesDir/repos` comme racine des clones ; permission `INTERNET` ajoutée.
- 17:40 — `check.sh` vert (57 tests JVM exécutés). Test ART `JGitOnDeviceTest` (ouvre un `.git`
  zippé en asset, le clone en `file://`, lit le log).
- 17:50 — `connectedDebugAndroidTest` sur l'émulateur `nitpick` (API 36) : **2 tests exécutés, 0 échec**
  (`opensARepositoryAndReadsItsLog`, `clonesALocalRepositoryAndReadsItsLog`). JGit ouvre et clone sur ART.

- 17:55 — preuve manuelle sur l'émulateur : Repos (état vide) → Clone → clone HTTPS public
  `octocat/Hello-World` sans token → Graph « on master · 3 commits » avec lanes ; après
  `force-stop` et relance, Hello-World est listé dans Repos (captures dans le scratchpad de la
  session). Centrage du libellé dans `NitpickButton` (le bouton pleine largeur l'affichait à gauche).

- 18:00 — review ponytail, 5 points corrigés dans le même commit : clavier `Password` pour le token
  et `Uri` pour l'URL (`NitpickTextField` gagne `keyboardOptions`) ; flux téléphone sur `cloneRoot`
  au lieu de la largeur (paysage vérifié sur l'émulateur) ; `SettingsViewModel.update` attend la
  lecture du fichier (test `SettingsViewModelTest`, sinon un update précoce écrasait la clé API) ;
  retour depuis Réglages sur Graph quand un dépôt est ouvert (vérifié) ; nom de dossier `trim()`.
  `check.sh` vert, 58 tests JVM.
- 18:10 — rebase sur `origin/main` (#20 et #22 mergées) : 5 conflits « garder les deux » (strings,
  `App.kt`, `RepositoryViewModel.load` qui garde maintenant le dépôt ouvert, tests JGit).
  `check.sh` vert, 82 tests JVM, 2 tests ART. Piège : `connectedDebugAndroidTest` désinstalle l'app
  après les tests et efface ses données (réglages, clones) ; recloner avant une démo.
- 18:20 — seconde review ponytail, 3 points corrigés : clavier `Password` aussi sur la clé API de
  l'écran Réglages, string `repository_loading` inutilisée supprimée, `PhoneScreen` privé.
- 18:30 — troisième review : l'activité est verrouillée en portrait (le token, volontairement hors
  du saved state, disparaissait à la rotation).

## Décisions

- Le flux téléphone se déclenche quand `App` reçoit un `cloneRoot` (Android), pas sur la largeur :
  la première version (< 600 dp) affichait le layout desktop, sans clone possible, dès que le téléphone
  passait en paysage. Pas d'`expect`/`actual`.
- La racine des clones arrive par un paramètre de `App`, comme `pickRepositoryPath`, pour ne pas
  toucher `AppContainer` pendant que #20 l'édite.
- Le token : jamais dans l'état du ViewModel, jamais dans `rememberSaveable`, jamais persisté ; il
  vit dans `Credentials` le temps du clone. Nom d'utilisateur fixe `x-access-token` (GitHub accepte
  n'importe quel nom avec un PAT ; `shortcut:` noté pour GitLab).
- L'écran Clone reste affiché pendant le clone : un token faux se corrige sur place, sans ressaisir
  l'URL. La navigation vers Graph se fait quand l'état est chargé sans erreur.
- Le nom de dossier refuse `/`, `\`, `.` et `..` : le clone reste sous `filesDir/repos`.
- Fixture ART = zip d'un `.git` (pas de git CLI sur l'appareil, pas d'import JGit dans `androidApp`).
  Dépendance ajoutée à `androidApp` seulement : `kotlinx-coroutines-core` en `androidTestImplementation`
  (pour `runBlocking`), alias déjà dans le catalogue.
- Pas de section JGit dans `docs/RESEARCH.md` (fichier ouvert par #20) : API vérifiée dans la javadoc
  JGit 6.10 (`CloneCommand.setURI/setDirectory/setCredentialsProvider`, nettoyage du dossier créé en
  cas d'échec ; `FetchCommand.setCredentialsProvider`). Le test « clone raté sans dossier résiduel »
  le prouve.

## Incertain

- Bouton système Retour sur Android : non géré (les écrans ont leur propre bouton Retour) ; le
  `BackHandler` multiplateforme est expérimental, à ajouter si la démo le demande.
- Barre du haut à 40 dp (composant maison `NitpickToolbar`) au lieu des 56 dp du handoff §4.
- Pas de FAB « Nitpick branch » sur l'écran Graph : c'est #23.
- Les lignes de `CommitGraphList` (#16, layout desktop) sont serrées sur 393 dp : la date chevauche
  le sujet. Polish à faire dans le composant (ligne compacte 60 dp du handoff §4), pas dans ce ticket.
- Message d'erreur JGit affiché brut (ex. `https://…: not authorized`) ; il ne contient pas le token.

## Blocages

- Aucun.

## À faire par un humain (critère d'acceptation)

- Cloner un dépôt GitHub privé avec un token sur un téléphone ou l'émulateur, vérifier commits et
  branches, relancer l'app et retrouver le dépôt dans Repos.

## Suivant

- #23 : boucle review sur Android (push avec `Credentials`, bottom sheet), sur ces écrans.
