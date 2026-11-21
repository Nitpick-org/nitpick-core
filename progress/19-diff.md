# Ticket 19-diff

- Statut : fini, à pousser et merger
- Propriétaire humain : @dovatgabriel
- Piste : B git + desktop
- Agent : Claude Code, session interactive
- Model : opus
- Escalations : aucune
- Fichiers autorisés : shared/src/jvmSharedMain/.../git/JGitRepository.kt, shared/src/jvmTest/.../git/,
  shared/src/commonMain/.../ui/repository/{DiffPanel,RepositoryScreen,RepositoryViewModel}.kt,
  shared/src/commonMain/.../ui/App.kt, ui/components/NitpickReview.kt (DiffLineRow),
  composeResources/values{,-fr}/strings.xml, shared/src/commonTest/.../ui/repository/

## Journal

- 17:35 : worktree créé. La PR #50 (H2) livrait déjà un premier `diffAgainst` et un
  onglet Diff minimal, mélangés avec le #22. Décision humaine : extraire et compléter le #19
  plutôt que le refaire.
- 17:40 : tests JGit rouges, puis `diffAgainst` : merge-base, renommages, arbre de travail,
  fichiers ignorés exclus.
- 17:45 : ViewModel : `baseRef`, `isDiffLoading`, `diffError`, `selectedDiffPath`,
  `selectBase`, `selectDiffFile`, `refreshDiff`. 7 tests verts.
- 17:50 : `DiffPanel` selon UI-HANDOFF §3.3. `check.sh` vert.
- 17:55 : #50 mergée entre-temps. Rebasé sur `main`, conflits résolus : la review,
  l'apply et le commit de #50 restent, et rechargent le diff par `refreshDiff`.

## Décisions

- Le côté gauche est la **merge-base** entre HEAD et la base, pas la pointe de la base. Sans
  cela, un commit arrivé sur `main` après la bifurcation apparaît comme retiré par la
  branche. Sans ancêtre commun, on compare à la base elle-même.
- Le côté droit est l'**arbre de travail** (demande de la review de #51) : une suggestion
  appliquée apparaît tout de suite. Un nouveau fichier non suivi apparaît aussi ; les fichiers
  ignorés par `.gitignore` sont exclus par le `FileTreeIterator`.
- Lockfiles, binaires et fichiers générés : déjà filtrés pour la review par `isReviewable`
  (`review/ReviewContext.kt`, #20). Le diff les montre ; un binaire affiche « Binary file,
  not shown ».
- Le diff se recharge à l'ouverture du repo, au changement de base, en ouvrant l'onglet Diff
  (l'arbre de travail a pu changer) et après apply ou commit.
- Le sélecteur de base liste les branches locales, sauf la branche courante.
- `DiffLineRow` affichait « null » pour le numéro absent d'une ligne ajoutée ou retirée :
  corrigé.

## Vérifications

- `:shared:jvmTest` : JGitRepositoryTest 17 tests et RepositoryViewModelTest 7 tests, 0 échec.
- `./scripts/check.sh` vert.

## Incertain

- Lignes longues coupées à droite (pas de scroll horizontal dans `DiffLineRow`).
- `FileTreeIterator` hache chaque fichier suivi à chaque chargement : rapide sur un repo
  de démo, à mesurer sur un gros repo.
- Android non câblé ici (piste C, #21/#23) ; `diffAgainst` vit dans `jvmSharedMain`, mais aucun
  test sur ART ne le couvre encore.

## Suivant

- #22 : la boucle review utilise ce diff.
