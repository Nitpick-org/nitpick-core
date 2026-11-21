# Ticket 95-checkout-conflict

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : B desktop
- Agent : lead
- Fichiers : `ui/repository/RepositoryViewModel.kt`,
  `commonTest/.../{RepositoryViewModelTest,FakeGitRepository}.kt`

## Journal

- 23:45 — JGit met les chemins en conflit après un `\n`; la status bar est sur une ligne
  et n'affichait que « Checkout conflict with files: ». Le message est aplati
  (`lineSequence().joinToString(" ")`) avant d'être stocké.
- Test ViewModel : `FakeGitRepository.checkoutError` jette un conflit multi-ligne et on
  vérifie `Checkout conflict with files: src/Graph.kt`.

## Décisions

- Pas de nouveau type d'erreur : le contrat `GitRepository` reste inchangé, le VM normalise
  le message JGit existant.
- Les autres erreurs de checkout gardent leur message.

## Vérifications

- `:shared:jvmTest --rerun-tasks` + `:desktopApp:compileKotlin` verts.
