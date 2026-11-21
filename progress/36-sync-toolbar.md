# Ticket 36-sync-toolbar

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : B + C (toolbar desktop, écran Graph Android)
- Agent : lead
- Model : session principale
- Escalations : aucune
- Fichiers : `shared/src/commonMain/.../domain/TrackingStatus.kt`,
  `git/GitRepository.kt`, `ui/repository/{RepositoryViewModel,RepositoryScreen,SyncStatus}.kt`,
  `ui/phone/{PhoneFlow,PhoneGraphScreen}.kt`, `ui/App.kt`, `jvmSharedMain/.../JGitRepository.kt`,
  `jvmTest/.../JGitRepositoryTest.kt`, `composeResources/values{,-fr}`

## Journal

- 20:55 — `TrackingStatus(ahead, behind, upstream)` dans le contrat git, implémenté avec
  `BranchTrackingStatus` ; `upstream` raccourci en `origin/main`.
- 21:00 — VM : `fetchBranch`, `isFetching`, `fetchError`, `tracking` rechargé après load,
  commit, fetch, pull, push et checkout. `hasUnpushedCommits` utilise `ahead` quand il existe.
- 21:05 — desktop : Fetch / Pull n / Push n dans la toolbar, `↑n` sur la branche courante,
  `↑n ↓m` dans la status bar. Android : mêmes trois actions sur l'écran Graph.
- 21:10 — test JGit : clone avec commit local (ahead 1), puis commit distant + fetch
  (ahead 1, behind 1, upstream `origin/main`). **126 tests JVM, 0 échec**.

## Décisions

- Sans upstream, les compteurs disparaissent et **Push n'est pas proposé** : publier une
  branche est hors scope (l'utilisateur peut le faire avec git). Pull/Fetch restent visibles.
- Fetch ne touche pas l'arbre de travail ; Pull garde la règle #42 (conflit annulé, review
  réinitialisée si des commits arrivent).
- Un seul `isSyncing` pour Fetch / Pull / Push / Checkout.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : **126 tests, 0 échec**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- `./scripts/check.sh` complet non lancé (pas de SDK Android ici).
- Non rejoué à la main : toolbar desktop et boutons Android.

## Incertain

- `BranchTrackingStatus.of` exige une branche avec upstream ; une branche locale sans
  upstream renvoie `null` et n'affiche pas de compteurs.
- Les boutons ne sont pas grisés, seulement neutralisés par `isSyncing` ; un état visuel
  disabled peut suivre dans le polish.
