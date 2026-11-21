# Ticket 104-audit-data-safety

- Statut : prêt pour PR (#113, rebasée)
- Propriétaire humain : @dovatgabriel
- Piste : B git+desktop, C android+finitions
- Agent : Claude Code, session principale (audit qualité du 22/11)
- Model : Fable, choisi à la main par H1
- Escalations : aucune
- Fichiers : `jvmSharedMain/.../git/JGitRepository.kt`,
  `commonMain/.../ui/settings/SettingsViewModel.kt`,
  `commonMain/.../ui/repository/{RepositoryViewModel,ReviewPanel}.kt`,
  `commonMain/.../ui/phone/PhoneReviewScreen.kt`, tests `FileSettingsStoreTest` et
  `RepositoryViewModelTest`

## Contexte

Cette branche portait les quatre correctifs de #104. H2 les a mergés en premier dans #109, et
#116 a complété. Rebasée sur `main` à la demande de H1, il n'en reste que ce que `main` n'a pas.

## Journal

- 00:23 — worktree créé, quatre correctifs écrits (pull, minSdk 33, `saveDraft`, `Files.move`).
- 00:35 — #109 mergée par H2 avec les mêmes correctifs : PR #113 fermée comme doublon.
- Rebase sur `main` (après #109 et #116) :
  - commit du pull : abandonné, `main` a déjà le correctif et ses messages (#116) ;
  - commit minSdk : abandonné, déjà dans #109 et #116 ;
  - `saveDraft` : réduit à la forme « tout le brouillon sauf les repos récents », pour qu'un
    champ ajouté au dialog ne soit plus oublié une troisième fois ;
  - `Files.move` : réduit au test « deux sauvegardes de suite », que #109 n'avait pas ;
  - `requireAccepted` : gardé tel quel, `push` et `publishBranch` partagent le contrôle.
- Deux suites de la relecture de #116 par l'agent `reviewer` :
  - Apply et Commit sont désactivés pendant un pull ou un checkout, au lieu d'ignorer le tap ;
  - `actionError` est remise à zéro quand un pull ou une review partagée remplace la review.

## Décisions

- Les tests de `SettingsViewModelTest` restent ceux de `main` : ils couvrent déjà les deux
  comportements, la réécriture de `saveDraft` ne change rien d'observable.
- Les deux suites de #116 sont dans cette PR plutôt que dans une troisième : ce sont des
  retouches de quelques lignes sur du code que je viens de faire merger.

## Vérifications

- Test rouge puis vert : `aPullThatDropsTheReviewAlsoDropsItsActionError` (l'erreur « Snippet
  not found for n1 » restait dans le state après le pull).
- `./scripts/check.sh` vert sur la branche rebasée.

## Incertain

- Boutons désactivés sur téléphone et desktop : pas de test Compose, à regarder à la main.
- Windows : le test des deux sauvegardes ne peut pas échouer sur macOS ou Linux.

## Suivant

- Relecture humaine. Le reste de l'audit est dans #107.
