# Ticket 23-android-review

- Statut : en cours (issue #23), boucle complète à prouver avec une clé API et un dépôt privé
- Propriétaire humain : @pepito2t (H3)
- Piste : C android+finitions
- Agent : Claude Code, session principale
- Model : fable
- Escalations : aucune
- Fichiers autorisés : shared/src/jvmSharedMain/git, shared/src/commonMain/kotlin/dev/nitpick/app/ui/{phone,components/NitBottomSheet.kt,App.kt,designsystem}, shared/src/commonMain/kotlin/dev/nitpick/app/ui/repository/{RepositoryViewModel.kt (nouvelles fonctions), RepositoryScreen.kt (RefsSidebar), DiffPanel.kt (FileHunks internal)}, composeResources, shared/src/{jvmTest,commonTest}
- Hors limites (worktree #22 en cours) : `ReviewPanel.kt`, `NitpickReview.kt`, `ReviewAnchoring.kt`, `FakeGitRepository.kt`, les corps de `reviewBranch`/`applySuggestion`/`commitAppliedSuggestions`, `JGitRepository.commit`

## Journal

- 18:15 — worktree créé sur `0f11c21` (#21 mergée).
- 18:25 — `JGitRepository.push` (`PushCommand` + credentials ; un ref refusé ne lève pas d'exception
  chez JGit : on lit `RemoteRefUpdate.status` et on lève avec le statut) et `checkout` (branche
  locale, ou branche distante → locale de suivi, comme `git checkout`). 3 tests `jvmTest` : push vers
  un bare local (HEAD du bare = HEAD du clone, `origin/main` à jour), push en retard refusé
  (`REJECTED_NONFASTFORWARD`), checkout local/distant. 23 tests JGit verts.
- 18:35 — `RepositoryViewModel` : `pushBranch(credentials?)`, `checkoutBranch`, `hasUnpushedCommits`
  (dérivé des refs : branche locale ≠ `origin/<branche>`), `hasCredentials`, `isPushing`, `pushError`,
  `isCheckingOut`. Le token du clone reste en mémoire de session pour le push (jamais dans l'état,
  jamais sauvé). 4 tests avec un wrapper `GitRepository by inner` local au test. 13 tests VM verts.
- 18:50 — UI téléphone : `PhoneReviewScreen` (états, carte résumé, nits groupés par fichier en lignes
  56 dp, en-tête de fichier → `PhoneDiffScreen` qui réutilise `FileHunks`, barre commit puis push
  avec saisie du token si absent), `NitBottomSheet` (`ModalBottomSheet` + `NitSheetContent`
  réutilisant `ReviewCommentCard`, boutons Dismiss/Apply 56 dp ; `NitSheetContent` dans le
  catalogue), bouton « Nitpick branch » sur Graph, onglet Branches tappable → checkout. Strings EN/FR.
- 18:55 — `check.sh` vert (96 tests JVM). Émulateur : clone Hello-World → Branches → tap `origin/test`
  → « on test · 4 commits » (checkout sur ART OK) → Nitpick branch → « Review failed : Unknown base:
  main ». Corrigé : la base se choisit au chargement parmi main / origin/main / master /
  origin/master (`defaultBaseRef`, testé). Nouvel essai : « Review · test vs master », erreur « No API
  key for ANTHROPIC » (attendue, pas de clé sur l'émulateur).
- 19:05 — **incident** : le premier `git commit` a lancé `check.sh` via le hook `pre-commit`, avec
  `GIT_DIR` et `GIT_INDEX_FILE` exportés par git. JGit en mémoire (`CloneCommand`) lit ces variables :
  les clones de test se sont initialisés dans le gitdir du worktree et ont **remplacé l'index** par
  celui du dépôt de test (`a.txt`, `b.txt`), d'où « invalid object … for 'a.txt' ». Branches, `main`
  et fichiers sources intacts ; quelques objets pendants dans `.git/objects`. Réparé par
  `git read-tree HEAD && git add -A`. Causes corrigées : le hook lance `check.sh` avec
  `env -u GIT_DIR -u GIT_INDEX_FILE -u GIT_WORK_TREE -u GIT_PREFIX`, et la factory fixe `setGitDir`.
  Le cas de #15 (sous-processus `git`) était couvert par `TestGitRepo` ; celui-ci est le cas JGit en
  mémoire, que rien ne peut filtrer côté test.
- 19:20 — advisor : deux bugs sur le chemin du ticket corrigés avant la review. (1) Après un commit,
  `review` repasse à null et la barre commit/push vivait dans la branche « résultat » : Push était
  inaccessible. La barre est sortie des états et se montre d'elle-même (`appliedFiles`, `isPushing`,
  `hasUnpushedCommits`). (2) Après un push refusé sans token, le champ token n'apparaissait jamais :
  l'erreur est fusionnée dans la branche « à pousser », champ token optionnel. Plus `checkoutError`
  affiché sur Graph. Hook : issue #58 + PR #59 (hotfix séparé pour toute l'équipe, à merger avant le
  prochain commit de H2). `check.sh --demo` vert.

- 19:30 — review ponytail : l'écran Diff n'affiche plus le premier fichier par défaut quand le chemin
  du nit n'est pas dans le diff (état vide) ; la vérification manuelle « Push visible après commit »
  est dans la checklist de `docs/DEMO.md`.

- 19:40 — review ponytail : pas de checkout pendant une review en cours (les nits de l'ancienne branche
  arriveraient sur la nouvelle).

## Décisions

- `checkout` implémenté ici (il est dans le contrat `GitRepository`) : après un clone, le téléphone
  est sur la branche par défaut et n'a rien à relire. #42 garde `pull`. Hypothèse notée sans réponse
  humaine au moment du commit.
- Push : réutilise le token saisi au clone (mémoire de session). Un dépôt rouvert après relance n'en a
  pas : l'écran Review demande le token au moment du push (`hasCredentials`).
- « Quelque chose à pousser » se déduit des refs (locale ≠ `origin/<branche>`), pas d'un flag à
  remettre à zéro dans les fonctions de #22.
- Base de review choisie au chargement : un clone GitHub n'a souvent pas de `main` local
  (Hello-World n'a que `master`). Le sélecteur desktop reste maître ensuite.
- Pas de FAB flottant : bouton pleine largeur 56 dp en bas, comme les autres écrans téléphone.

## Incertain

- Boucle complète (nits → sheet → apply → commit → push) non rejouée sur l'émulateur : il faut la clé
  API dans les Réglages de l'app émulée et un dépôt privé avec une branche à relire.
- Identité d'auteur du commit sur Android : JGit sans `~/.gitconfig` peut produire un auteur vide ou
  `u0_aNNN@localhost`. À vérifier sur le premier commit réel ; correctif possible dans
  `JGitRepositoryFactory.clone` (config locale `user.name/email`).
- `ModalBottomSheet` compile sur desktop et Android ; non vérifié visuellement sur desktop (non utilisé).
- Snackbar « suggestion applied » du handoff remplacée par une ligne verte dans la barre commit.

## Blocages

- Aucun.

## Leçon à ajouter aux règles d'équipe (H2)

- JGit en mémoire lit `GIT_DIR`/`GIT_INDEX_FILE` comme le CLI : un hook qui lance les tests doit
  les retirer de l'environnement (`env -u`), sinon un clone de test écrase l'index du dépôt.

## Suivant

- Rejouer la boucle avec clé + dépôt privé, vérifier le commit sur GitHub ; rebase après merge de #22.
