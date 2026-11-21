# Ticket 22-apply-hardening

- Statut : prêt pour review (PR)
- Propriétaire humain : @zooldyyk (H2)
- Piste : A core + B desktop
- Agent : Claude Fable 5.1 (session interactive)
- Model : fable
- Escalations : aucune
- Fichiers autorisés : shared/src/commonMain/kotlin/dev/nitpick/app/review/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/repository/RepositoryViewModel.kt,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/repository/ReviewPanel.kt,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/components/NitpickReview.kt,
  shared/src/jvmSharedMain/kotlin/dev/nitpick/app/git/JGitRepository.kt,
  shared/src/commonMain/composeResources/**, shared/src/{commonTest,jvmTest}/**

## Fait (reste de #22 après l'audit du 21/11)

- Un seul ancrage : `ReviewAnchoring` (lignes comparées en `trim()`, occurrence la plus proche
  de la ligne annoncée) sert à Apply, au recalage et au repli « fichier entier » de l'agent ;
  `Anchoring.kt` supprimé.
- Apply et commit sérialisés par un `Mutex` dans `RepositoryViewModel`.
- `FakeGitRepository` écrit en mémoire ; test ViewModel review → apply → commit.
- `JGitRepository.commit` refuse un commit vide (`setAllowEmpty(false)`), test sur dépôt temporaire.
- `LocalReviewAgent` trie BUG en premier, comme l'agent Koog.
- UI : « whole file » / « fichier entier » pour un nit de fichier entier ; « Dismiss » et
  « N nits on M changed files » passent par les ressources EN/FR.

## Incertain

- Apply et commit avec un vrai nit Koog restent à rejouer à la main (`check.sh --demo`).

## Journal

- 18:50 — worktree créé ; #19 fermé (livré par #53), prise de la suite de #22.
- 19:10 — six points de l'audit corrigés, tests verts (anchoring 7, VM 8, JGit 18).
