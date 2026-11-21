# Dogfooding : Nitpick relit nos propres branches

Preuve pour le critère « la review relit les branches des agents » (`AGENTS.md`, façon de
travailler). Une entrée par passage, avec ce que la review a trouvé et ce qu'on en a fait.

## 22/11 00:50 — `ticket/104-audit-data-safety` (Gabriel, 6 commits, 11 fichiers)

Desktop, Anthropic `claude-sonnet-5-5`, diff contre `main`. Capture : `dogfooding-104-review.png`.

| Nit | Fichier | Vérification | Suite |
| --- | --- | --- | --- |
| Avec des modifs locales et une branche divergée, JGit répond `ABORTED` sans lever d'exception : le `catch` qui écrit « Uncommitted changes… » ne tourne jamais, l'utilisateur voit « Pull failed: Aborted ». | `JGitRepository.kt:243-270` | Reproduit sur `main` : le test `aDirtyTreeIsNotResetWhenThePullDiverged` passe parce qu'il ne vérifie que le type de l'exception ; le message réel est `Pull failed: Aborted`. | Issue #114 |
| `Files.move(…, ATOMIC_MOVE)` lève `AtomicMoveNotSupportedException` sur certains systèmes de fichiers, sans repli. | `FileSettingsStore.kt` | Vrai sur la branche relue ; la version mergée (#109) a déjà le repli `REPLACE_EXISTING`. | Rien à faire |

Bilan : 2 nits, 1 vrai bug encore présent sur `main`, 1 vrai problème déjà corrigé avant le merge. Zéro faux positif.
