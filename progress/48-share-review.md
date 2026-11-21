# Ticket 48-share-review

- Statut : prêt pour PR (démo à rejouer à la main avec le serveur lancé)
- Propriétaire humain : @dovatgabriel
- Piste : D (partage des reviews)
- Agent : Claude Code (lead)
- Model : opus
- Escalations : aucune
- Fichiers : `commonMain/share/*` (nouveau), `git/GitRepository.kt` + `JGitRepository.kt`
  (`remoteUrl`), `settings/Settings.kt`, `ui/repository/{RepositoryViewModel,ReviewPanel,
  RepositoryScreen,ShareReview}.kt`, `ui/phone/{PhoneFlow,PhoneGraphScreen,PhoneReviewScreen,
  PhoneSharedReviewsScreen}.kt`, `ui/settings/*`, `ui/App.kt`, `AppContainer.kt`, strings EN/FR,
  `androidApp` (manifest + test sur ART), Gradle.

## Journal

- 19:38 — worktree créé.
- 19:45 — `ReviewShareClient` (§6) + `KtorReviewShareClient` (commonMain, `HttpClient()` trouve
  le CIO de jvmSharedMain), 5 tests MockEngine.
- 19:55 — `GitRepository.remoteUrl()`, `Settings.serverUrl` ; ViewModel : `shareReview`,
  `loadSharedReviews`, `openSharedReview` ; 4 tests.
- 20:10 — UI : bouton « Share review » sur le résultat (desktop + téléphone), liste « Shared
  reviews » dans l'état vide de l'onglet Review (desktop) et écran « Shared » depuis le Graph
  (téléphone), champ « Review server URL » dans les deux réglages.
- 20:20 — tests du vrai moteur : port fermé → `ReviewShareException`, sur JVM et sur ART.

## Décisions

- Ouvrir une review partagée remplit `state.review` : mêmes nits, même apply, même ancrage par
  `originalSnippet` qu'une review locale, et aucun appel au LLM.
- Le token d'une URL de remote (`https://token@host/…`) est retiré avant l'envoi ; le serveur
  normalise SSH/HTTPS (#46).
- Une seule erreur côté client, `ReviewShareException` ; le ViewModel la type en
  `ShareFailure.NoRemote` / `Unreachable`, l'UI affiche un message qui dit comment lancer le
  serveur. Le reste de l'app ne change pas sans serveur.
- Timeouts courts (connexion 3 s, requête 10 s) : une IP LAN fausse échoue vite.
- `usesCleartextTraffic` sur Android : le serveur de démo parle HTTP sur le LAN.
- `ReviewShareClient` dans `AppContainer`, l'URL lue dans les réglages à chaque appel.

## Écarts au ticket

- **Pas de date** dans la liste : `SharedReview` n'a pas de champ date ; l'ordre (le plus récent
  d'abord) est celui du serveur. Ajouter `sharedAt` = changement de contrat + serveur.
- **« Grisé sans serveur »** : le bouton reste actif (pour réessayer une fois le serveur lancé)
  et un message rouge explique quoi faire ; il est grisé pendant l'envoi.

## Vérifications

- `:shared:jvmTest` : `KtorReviewShareClientTest` 5/5, `KtorReviewShareClientEngineTest` 1/1,
  `ShareReviewViewModelTest` 4/4, `RepositoryViewModelTest` 19/19.
- `ReviewShareOnDeviceTest` sur l'émulateur API 36 : 1/1.
- `./scripts/check.sh` vert.

## Incertain

- Pas rejoué à la main : Share sur desktop → liste sur le téléphone avec le serveur lancé.

## Suivant

- Rejouer le point 5 de la démo : `./gradlew :server:run`, `adb reverse tcp:8080 tcp:8080`,
  Share sur desktop, « Shared » sur le téléphone.
