# Ticket 105-audit-vm-errors

- Statut : prêt pour PR
- Propriétaire humain : @dovatgabriel
- Piste : B git+desktop, C android+finitions
- Agent : Claude Code, session principale (audit qualité du 22/11)
- Model : Fable, choisi à la main par H1
- Escalations : aucune
- Fichiers : `ui/phone/{PhoneReviewScreen,CloneScreen}.kt`,
  `ui/repository/{RepositoryViewModel,RepositoryScreen,DiffPanel,CloneDialog,CloneProgress}.kt`,
  `ui/components/NitpickReview.kt`, `ui/settings/SettingsViewModel.kt`,
  `jvmSharedMain/.../git/JGitRepository.kt`, leurs tests, `README.md`, `docs/QUESTIONS.md`,
  `.editorconfig`

## Contexte

H2 a repris les issues de l'audit et mergé #109, #111 et #112 pendant que j'écrivais les mêmes
correctifs. Ma PR #113 a été fermée comme doublon. Ce ticket ne contient que ce que ces trois
PR ont laissé ouvert, relu sur `main` après leur merge.

## Journal

- 00:35 — worktree créé depuis `main` à jour.
- `PhoneReviewScreen` n'affichait toujours pas `actionError` : le journal de #111 le dit fait,
  mais le fichier n'était pas dans son diff. Ajouté entre la liste des nits et la barre de
  commit.
- Badge de sévérité du téléphone : `nit.severity.name` remplacé par le `severityLabel` de la
  carte ; un seul `severityTone`.
- Messages du pull (#109) : trois tests rouges sur le code mergé. Divergence → « Pull failed:
  Aborted » ; origin injoignable → « Uncommitted changes… » à tort ; fast-forward sur un fichier
  modifié → phrase sur une branche divergente. Les deux cas réels disent maintenant « Commit or
  discard your changes before pulling: <chemins> », les autres erreurs gardent leur message.
- Apply ou commit pendant un pull ou un checkout : ignoré. Trouvé par l'agent `reviewer` : une
  suggestion appliquée pendant le fetch pouvait être effacée par le reset d'un pull en conflit.
- Docs et commentaires alignés sur minSdk 33 ; Q-020 ajoutée ; `DiscardOutputStream` supprimé.
- Reste de #106 : helpers de clone déplacés dans `ui/repository/CloneProgress.kt` (plus de
  cycle `ui/phone` ↔ `ui/repository`), deux doublons fusionnés, 3 tests du wizard supprimés,
  `.editorconfig` en 4 espaces pour Kotlin.

## Décisions

- Le garde « pas d'apply pendant une synchro » ne couvre que pull et checkout, qui écrivent
  des fichiers. Un fetch ou un push ne bloque pas un tap.
- `CHECKOUT_CONFLICT` sort de la branche du reset : JGit 6.10 lève une exception pour ce cas
  et `merge.conflicts` y est nul.
- `LocalReviewAgent` reste : le supprimer rend un paramètre obligatoire et casserait `main` si
  une PR en cours ajoute un test du ViewModel. Noté dans #107.

## Vérifications

- Tests rouges puis verts : 3 sur le pull, 1 sur l'apply pendant un pull.
- `:shared:allTests` : 169 tests sur la JVM, 123 sur l'hôte Android, 0 échec.
- `./scripts/check.sh` vert à chaque commit (hook pre-commit).

## Incertain

- Le rendu de `actionError` et du badge sur le téléphone n'a pas de test Compose : à vérifier
  à la main sur l'appareil.
- Tests sur ART non relancés (aucun émulateur démarré).
- Le téléphone de démo doit être en Android 13 ou plus (Q-020).

## Suivant

- Relecture humaine de la PR. Les points d'architecture et d'outillage sont dans #107.
