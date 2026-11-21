# Audit perf — chemins chauds git / diff

- Statut : relu (seconde passe, Fable, 21/11 soir) ; reste à rejouer sur un appareil ART
- Piste : performance desktop + Android, cœur partagé
- Agent : lead (DeepSeek), 21/11 ~22:00 ; relecture + volet Android : lead (Fable)
- Fichiers : `git/JGitRepository.kt`, `ui/repository/{RepositoryViewModel,DiffPanel,
  RepositoryScreen,CommitGraph}.kt`, `domain/Diff.kt`, `review/ReviewContext.kt`,
  `settings/{Settings,LlmModels}.kt`, `ui/settings/SettingsViewModel.kt`,
  `ui/theme/NitpickTheme.kt`, `ui/phone/PhoneReviewScreen.kt`, tests JVM/common,
  `docs/PERFORMANCE-AUDIT.md`
- Rapport complet : `docs/PERFORMANCE-AUDIT.md` (mesures, protocole, pistes non faites)

## Mini explication

La lenteur ne venait pas de Compose mais de deux parcours JGit non bornés :

- `log()` utilisait le tri TOPO de JGit, qui parcourt **tout l'historique** avant de rendre la
  première ligne. `buildReviewContext` repayait ce coût pour 10 messages de commit.
- `diffAgainst()` comparait le working tree entier : chaque fichier suivi était haché à chaque
  refresh, même quand 4 fichiers avaient changé.

Corrigé : fenêtre temporelle + tri topologique en mémoire pour `log()` ; filtre sur les
chemins réellement changés (status + fork→HEAD) pour `diffAgainst()`, avec un chemin
arbre→arbre quand le working tree est propre. Mesures : `log(500)` 449 → 5 ms sur 100k
commits ; `diffAgainst` 234 → 28 ms (propre) / 107 ms (sale) sur 3k fichiers.

Ajouts : refresh de diff single-flight, compteurs de lignes précalculés, `stage()` groupé,
lecture unique par fichier au re-anchoring, settings non réécrits sans changement,
micro-allocations UI. Le défaut Anthropic passe à Sonnet (Opus reste dans la liste).

## Relecture faite (Fable, 21/11 soir)

Les 4 points ci-dessous sont relus : verdicts et le cas limite trouvé (fichier supprimé
fork→HEAD mais recréé non suivi : il entre dans le diff de review depuis la suppression du
`TrackedFilter`) sont dans `docs/PERFORMANCE-AUDIT.md` § Relecture. `check.sh` vert sur une
machine avec SDK ; comptes vérifiés : jvmTest 147, testAndroidHostTest 107, 0 échec. Volet
Android ajouté au rapport (pas d'appareil branché : analyse code + leviers, pas de mesures
ART). Hors perf, remarqué au passage : après une mort de process, `PhoneFlow` restaure
`screen = Graph` (rememberSaveable) sur un état vide — écran Graph vide au lieu de Repos.

## À relire en priorité (Fable)

1. `JGitRepository.diffAgainst` + `changedPathsBetween` : rename, suppression, fichier
   staged, working tree propre/sale. Tests `JGitRepositoryTest` correspondants.
2. `JGitRepository.log` + `childrenBeforeParents` : ordre enfants-avant-parents, limite 500,
   horloge inversée (`logKeepsChildrenBeforeParentsWhenAClockSkewInvertsCommitTimes`).
3. `RepositoryViewModel.refreshDiff` : single-flight, cancellation, changement de dépôt.
4. `LlmModels` : Sonnet par défaut est un choix produit ; un `settings.json` existant garde
   Opus.

## Incertain / non fait

- Le prompt de review peut encore atteindre 120k caractères de contenu de fichiers ; réduire
  `MAX_PROMPT_TOTAL_CHARS` à 60k est une piste qualité/latence, pas appliquée.
- `git status()` reste un plancher de ~20–30 ms par diff ; un cache indexé sur HEAD + mtime
  de l'index est possible mais risqué (édition externe).
- Le défaut Sonnet ne migre pas un réglage existant.
- ~~`./scripts/check.sh` complet n'a pas tourné~~ : fait en seconde passe, vert (Android
  compris). Rien sur ART, toujours.
- Cas limite `diffAgainst` (fichier non suivi à un chemin supprimé par la branche) : non
  corrigé, décision H2 — fix propre = refiltrer sur l'index.

## Suivant

- Rejouer la démo sur un vrai dépôt et sur ART, comparer le ressenti avant/après ; cold
  start via `am start -W`.
- Décision H3 : démo Android en release (une `signingConfig` à ajouter) ou en debug.
- Décider avec H1/H2 : budget de prompt, cache de review pour la démo, streaming.
