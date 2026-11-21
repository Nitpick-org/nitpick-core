# Ticket 36-sync-toolbar

- Statut : en review ; corrections après auto-review (PR #72)
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
- 21:20 — auto-review : tooltips ⌘⇧F/L/P, séparateur avant le CTA, Push masqué sans
  upstream, `↑n` réellement lu dans l'arbre, compteurs Android, strings `sync_*` EN/FR,
  fetchError effacé par un pull. Reste : carte branche Android 44 dp (les actions sont
  encore dans la top bar), `DECISIONS.md` (fichier humain), `check.sh` avec SDK. **126 tests
  JVM, 0 échec**.

## Décisions

- Sans upstream, les compteurs disparaissent et **Push n'est pas proposé** : publier une
  branche est hors scope (l'utilisateur peut le faire avec git). Pull/Fetch restent visibles.
- Fetch ne touche pas l'arbre de travail ; Pull garde la règle #42 (conflit annulé, review
  réinitialisée si des commits arrivent).
- Un seul `isSyncing` pour Fetch / Pull / Push / Checkout.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : **126 tests, 0 échec**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- `./scripts/check.sh` complet non lancé (pas de SDK Android ici) : case DoD encore
  ouverte, à ne pas merger avant la machine avec SDK.
- Non rejoué à la main : toolbar desktop et boutons Android.

## Incertain

- `BranchTrackingStatus.of` exige une branche avec upstream ; une branche locale sans
  upstream renvoie `null` et n'affiche pas de compteurs.
- Les boutons ne sont pas grisés, seulement neutralisés par `isSyncing` ; un état visuel
  disabled peut suivre dans le polish.
- Android : les trois actions sont dans la top bar, pas encore la carte branche 44 dp du
  handoff §4.2 ; les compteurs sont affichés dans l'en-tête. Écart assumé vu l'heure.
- Décision « Push masqué sans upstream » consignée ici, mais **pas encore dans
  `docs/DECISIONS.md`** (fichier réservé aux humains).
