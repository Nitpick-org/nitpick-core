# Ticket 15-open-repo

- Statut : fini, à relire puis merger (issue Nitpick-org/nitpick-core#15)
- Propriétaire humain : @dovatgabriel
- Piste : B git+desktop
- Agent : Claude Code, session principale
- Model : opus
- Escalations : aucune
- Fichiers autorisés : shared/src/jvmSharedMain, shared/src/commonMain/kotlin/dev/nitpick/app/{ui,AppContainer.kt}, shared/src/commonMain/composeResources, shared/src/{jvmTest,commonTest}, desktopApp, androidApp (MainActivity), gradle/libs.versions.toml, shared/build.gradle.kts

## Journal

- 14:16 — worktree créé.
- 14:20 — `JGitRepository` (jvmSharedMain) : log, refs, currentBranch, status, readFile,
  listFiles ; les écritures, le réseau et `diffAgainst` lèvent `NotImplementedError`
  (« … is not implemented yet »). `JGitRepositoryFactory.open` ; `clone` en TODO. Spike supprimé.
- 14:35 — 12 tests `jvmTest` sur des dépôts temporaires créés avec la CLI git (aucun import
  JGit dans les tests). Piège : `fun test() = runBlocking { … }` qui finit sur
  `assertFailsWith` ne renvoie pas `Unit`, JUnit rejette la classe → `runBlocking<Unit>`.
- 14:50 — UI : `AppContainer` (DI manuelle), `RepositoryViewModel` (StateFlow, état immuable),
  `RepositoryScreen` 3 panneaux (refs | commits | placeholder détails) avec états vide,
  chargement et erreur, strings EN + FR, sélecteur de dossier natif dans `desktopApp`.
  3 tests du ViewModel en `commonTest` avec `FakeGitRepository`, verts sur JVM et hôte Android.
- 15:00 — `check.sh` vert (21 tests exécutés). App desktop lancée : un vrai dépôt ouvert via
  le sélecteur affiche ses commits, branches, remote et tag (capture prise).

- 15:40 — revue demandée par l'humain (ponytail-review), 4 points corrigés :
  worktree lié refusé avec un message clair (JGit 6.10 ne sait pas l'ouvrir), indication
  « choisissez un dépôt » masquée sans sélecteur, HEAD détachée affichée en id court, faux
  dépôt réduit à ce que les tests utilisent.
- 15:45 — **incident** : le hook `pre-commit` exporte `GIT_DIR` et `GIT_INDEX_FILE` ; les
  commandes git de `TestGitRepo` en ont hérité et ont écrit dans le vrai dépôt (8 commits de
  test sur la branche du ticket, une branche `feature` et son worktree, index écrasé,
  `core.bare=true`). Rien de poussé, `main` intact. Restauré (`core.bare=false`, branche
  ramenée à `d263555` avec `reset --mixed`, branche et worktree parasites supprimés).
  Cause corrigée : `TestGitRepo` retire tout `GIT_*` hérité ; prouvé en lançant les tests avec
  un `GIT_DIR` inexistant (13 échecs avant, 13 succès après).

## Décisions

- `log` trie en ordre topologique puis par date : chaque enfant passe avant ses parents,
  ce dont les lanes (#16) ont besoin. TOPO parcourt tout l'historique avant de rendre la
  main (commentaire `shortcut:` dans le code).
- Les dates sont celles de l'auteur, comme `git log`.
- `readFile` refuse les chemins absolus, ceux qui sortent du dépôt (y compris par lien
  symbolique) et ceux qui visent `.git` (le config peut contenir un token) : ces chemins
  viendront du LLM.
- `listFiles` lit l'index : fichiers suivis seulement, donc pas de `build/` ni d'ignorés.
- `open` accepte un sous-dossier d'un dépôt (`findGitDir` remonte) et refuse un dépôt bare.
- `formatCommitTime` : `expect` dans commonMain, un seul `actual` dans jvmSharedMain
  (java.time existe sur desktop et sur Android dès l'API 26), localisé par la locale système.
- Le sélecteur macOS est le `FileDialog` natif ; Windows et Linux passent par `JFileChooser`.
- Code d'exemple du wizard supprimé (Greeting, Platform, drawable) : plus rien ne l'utilisait.
- Dépendances : `kotlinx-coroutines-core` déclaré explicitement (déjà présent en transitif,
  utilisé pour `Dispatchers.IO`) et `kotlinx-coroutines-test` pour `Dispatchers.setMain`.

## Incertain

- Le test JGit sur ART (spike) est supprimé avec le spike ; `assembleDebug` dexe toujours
  JGit, mais l'exécution sur Android revient avec le clone (#21).
- `GitRepository` n'a pas de `close()` : le dépôt JGit reste ouvert tant que l'app tourne
  (bénin sur macOS/Linux, peut verrouiller des fichiers sur Windows). Changer le contrat =
  accord de H2.
- Sélecteur Windows/Linux non testé (seulement macOS).

## Blocages

- Aucun. Attention au merge avec #18 (piste C) : les deux tickets créent
  `composeResources/values*/strings.xml`.

## Leçon à ajouter à AGENTS.md (H2)

- Un test qui lance `git` doit retirer les variables `GIT_*` héritées : le hook
  `pre-commit` les exporte, et le test écrit alors dans le vrai dépôt.

## Suivant

- #19 (diff vs base) peut s'appuyer sur `RepositoryViewModel` ; #16 consomme `Commit.parentIds`.
