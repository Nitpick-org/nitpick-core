# Ticket 16-graph

- Statut : en cours, algorithm + preview UI prêts pour PR
- Propriétaire humain : @zooldyyk
- Piste : A (core partagé) + UI desktop
- Agent : lead (orchestrateur) + deux sous-agents de comparaison
- Model : sonnet (sous-agents), session principale
- Escalations : aucune
- Fichiers autorisés : `shared/src/commonMain/kotlin/dev/nitpick/app/graph/`, `shared/src/commonTest/kotlin/dev/nitpick/app/graph/`, `shared/src/commonMain/kotlin/dev/nitpick/app/ui/`

## Journal

- 14:20 — branche `ticket/16-graph` créée depuis `origin/main` (`adc6a2d`).
- 14:35 — modèle `CommitGraphRow` et algorithme de lanes en une passe :
  `active[lane] = commit id attendu`. Le commit prend la première lane qui l'attend,
  les autres convergent, le premier parent continue dans la lane, les parents suivants
  prennent une lane libre ou existante. Les parents inconnus libèrent la lane.
- 14:45 — UI `CommitGraphScreen` : liste des commits + lanes colorées + panneau de détails.
  Données de preview en attendant le repo picker (#15) ; `App()` pointe dessus.
- 15:00 — tests `commonTest` ajoutés : linéaire, branche + merge, orphelin, octopus,
  réutilisation de lane, convergence de deux enfants, cap 500, input vide.
- 15:10 — premier run manuel : l'octopus donnait `[0,0,1,2,0]` et non `[0,0,0,0,0]` ;
  le test a été corrigé (les lanes 1 et 2 portent les branches, elles convergent à la
  racine). C'est le comportement attendu.

- 16:00 — rebasée sur `main` après le merge de #15 (#27), poussée en
  `ticket/16-graph-rebased` (pas de force push sur `ticket/16-graph`). Conflit sur `App.kt` :
  gardé le flux réel de #15.
- 16:15 — graph branché sur le vrai dépôt : `RepositoryViewModel` calcule
  `buildCommitGraph(repository.log())` hors du thread UI (`Dispatchers.Default`, injectable
  pour les tests) et garde le commit sélectionné (le plus récent par défaut). Le rendu des
  lanes et le panneau de détails passent dans `ui/repository/CommitGraph.kt`, libellés en EN/FR
  et date localisée via `formatCommitTime` (#15). Supprimés : `CommitGraphScreen`,
  `PreviewCommits` et `CommitFormat` (doublon UTC). 4 tests du ViewModel, dont la sélection.
  Vérifié à l'écran sur un vrai dépôt (lanes, merge, détails du commit sélectionné).

## Comparaison multi-agent (critère bonus du jury)

Deux sous-agents indépendants ont produit un algorithme sur le même modèle `Commit` et
les mêmes cas :

| Agent | Approche | Tests rendus | Points forts | Points faibles |
| --- | --- | --- | --- | --- |
| `graph-agent-a` | une passe, table `active: MutableList<String?>` | 10 cas, dont parent dupliqué/self, lane libérée, octopus | le plus court, snapshots exacts, facile à expliquer à l'équipe | un seul passage mêle allocation et snapshots |
| `graph-agent-b` | deux passes (ledger d'assignation puis projection) | 7 cas, dont lane libérée, orphelin, cap | séparation allocation/rendu, snapshots trimés | ~50 % de code en plus, deux logiques à garder cohérentes, convention de trim différente |

Décision : **garder l'approche en une passe (A)**, pour trois raisons :
1. à 500 commits, le coût est négligeable ;
2. l'équipe débute en Kotlin : moins d'états et de types internes à relire ;
3. les snapshots non trimés gardent des index de lanes stables, ce que l'UI utilise
   directement.

Les cas limites de B (input vide, orphelin, lane réutilisée, convergence) ont été
ajoutés aux tests finaux. Le code final reprend aussi la déduplication des parents et
le garde-fou self-parent de A.

## Vérifications

- `GRADLE_USER_HOME=/tmp/nitpick-gradle ANDROID_USER_HOME=/tmp/nitpick-android ./gradlew :shared:jvmTest --rerun-tasks`
  → **8 tests `CommitGraphTest`, 0 échec**.
- `:shared:compileKotlinJvm` → vert (l'UI Compose compile).
- `./scripts/check.sh` complet non lancé dans cet environnement : pas de SDK Android
  (`~/.android` + `platforms;android-37` absents). À lancer sur la machine de l'équipe.

## Décisions

- Premier parent = continuation de la lane du commit ; les autres parents ouvrent une
  lane. Si un parent est déjà attendu par une lane, on la réutilise.
- Parents inconnus (hors cap 500 ou absents) : pas de lane, pas d'arête ; la lane du
  commit est libérée.
- Couleur = `lane % 8` ; la palette UI est fixe et lisible en clair/sombre.
- L'UI reçoit `List<Commit>` et ne dépend pas de JGit ; #15 branchera `GitRepository.log()`.

## Incertain / blocage

- Le thème sombre n'a pas été capturé ; la palette est choisie pour les deux fonds (le
  thème lui-même arrive avec #18).
- Android n'affiche pas encore le graph : pas de dépôt avant le clone (#21).

## Suivant

1. Ouvrir la PR depuis `ticket/16-graph-rebased`, faire relire l'UI.
2. Ajouter une capture desktop en sombre quand #18 est mergé.
