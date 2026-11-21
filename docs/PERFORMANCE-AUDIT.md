# Audit de performance — 21 novembre 2026

> Périmètre : desktop JVM et Android, cœur partagé (`shared/`). Mesures faites sur la machine
> de développement (macOS, JDK 22, JVM chaude), avant/après les corrections de cette passe.

## Résumé

Le travail **local** était le vrai goulot sur les dépôts de taille réelle, pas l'UI :

- `log(500)` reparcourait **tout l'historique** à cause du tri topologique JGit : **449 ms**
  sur un dépôt de 100 000 commits, et `buildReviewContext` repayait ce coût pour afficher
  10 messages de commit.
- `diffAgainst` hachait **tous les fichiers suivis** du working tree à chaque refresh :
  **234 ms** sur 3 000 fichiers, même quand seuls 4 fichiers avaient changé.
- Un refresh de diff annulé puis relancé (clic sur l'onglet Diff, apply d'une suggestion)
  empilait plusieurs walks JGit non annulables.

Après correction, sur les mêmes dépôts :

| Opération | Avant | Après | Gain |
| --- | ---: | ---: | ---: |
| `log(500)`, 100 000 commits | 449 ms | **5 ms** | **90×** |
| `buildReviewContext`, 100 000 commits | 458 ms | **19 ms** | **24×** |
| Ouverture complète, 100 000 commits | 894 ms* | **31 ms*** | **~29×** |
| `diffAgainst`, 3 000 fichiers, arbre propre | 234 ms | **28 ms** | **8,4×** |
| `diffAgainst`, 3 000 fichiers, 4 fichiers modifiés | 234 ms | **107 ms** | **2,2×** |
| `diffAgainst`, ce dépôt (147 commits) | 41 ms | 41 ms | — (plancher `git status`) |

\* La sonde « ouverture complète » appelait `log()` deux fois, avant comme après : le rapport
reste comparable, la valeur réelle est plus basse des deux côtés.

La latence restante de la **review** est dominée par le LLM, pas par le code local. Le modèle
par défaut était le modèle phare (Opus) alors que `CONTEXT.md` §14 demande un « modèle rapide
par défaut » : c'est corrigé (Sonnet en premier), mais un réglage existant garde Opus.

## Protocole

Quatre dépôts, pour séparer taille d'historique et taille d'arbre :

| Dépôt | Commits | Fichiers suivis | Usage |
| --- | ---: | ---: | --- |
| ce dépôt | 147 | ~150 | cas réel de l'équipe |
| `/tmp/bigrepo` | 5 000 | 3 000 | arbre large, working tree sale |
| clone propre de `bigrepo` | 5 000 | 3 000 | arbre large, working tree propre |
| `/tmp/hugerepo` | 100 000 | 2 000 | historique profond |

Mesures : médiane de 3 exécutions après un warm-up, `System.nanoTime()`, JVM chaude. Les
valeurs Compose viennent d'un test `ui-test-junit4` temporaire (scène 1024×768, 500 lignes de
graphe, diff de 100 fichiers) : elles sont indicatives, pas des profils GPU.

## Corrections livrées

1. **`log()` borné par la fenêtre demandée** — `JGitRepository.kt:44`
   Le walk passe en `COMMIT_TIME_DESC`, s'arrête à `limit`, puis un tri de Kahn en mémoire
   remet les enfants avant les parents (le contrat des lanes). Coût borné par `limit`, plus par
   la taille du dépôt. Un test couvre le cas d'horloge inversée (merge plus vieux que son
   parent).

2. **`diffAgainst()` filtré sur les chemins réellement modifiés** — `JGitRepository.kt:133`
   `git status()` donne les chemins changés (index + working tree) ; on y ajoute les chemins
   modifiés entre le fork point et HEAD. Le `DiffFormatter` ne visite plus que ces chemins.
   Arbre propre : diff arbre→arbre, aucun fichier haché. Le `TrackedFilter` maison disparaît
   (les fichiers non suivis sont déjà exclus).

3. **Refresh de diff single-flight** — `RepositoryViewModel.kt:211`
   Un seul walk à la fois ; une demande arrivée pendant le walk mémorise la base la plus
   récente et le job reboucle une fois. Fini les diff concurrents quand on clique sur Diff
   ou qu'on applique plusieurs suggestions.

4. **Compteurs de lignes précalculés** — `Diff.kt:14`, `DiffPanel.kt:58`
   `FileDiff.addedLines/removedLines` sont calculés une fois au parsing. Le panneau ne
   rescanne plus toutes les lignes à chaque recomposition (et `selectReviewDiffs` non plus).

5. **`stage()` en une écriture d'index** — `JGitRepository.kt:209`
   Un `add()` et un `rm()` groupés au lieu d'un `call()` par fichier. Le coût ne dépend plus
   du nombre de suggestions appliquées.

6. **Re-anchoring : un fichier lu une fois** — `RepositoryViewModel.kt:570`
   Les nits d'un même fichier partagent la lecture au lieu de relire le fichier par nit.

7. **Pas d'écriture settings inutile** — `Settings.kt`, `SettingsViewModel.kt:34`
   Rouvrir le dépôt déjà en tête de liste ne réécrit plus le fichier (et ne réémet plus
   l'état).

8. **Tokens de thème stables** — `NitpickTheme.kt:64`
   Typographie, formes, espacements et `ColorScheme` sont `remember`és. C'était une
   invalidation de sous-arbre à chaque ré-exécution du thème (bascule de thème, previews) ;
   ce n'était pas la cause principale de la lenteur, mais c'était gratuit à corriger.

9. **Modèle rapide par défaut** — `LlmModels.kt:10`
   Sonnet passe devant Opus pour Anthropic, conformément au fallback de `CONTEXT.md` §14.
   Un fichier `settings.json` existant garde son modèle : il faut le changer dans les
   réglages pour une machine de démo déjà configurée.

10. **Micro-allocations UI** — `RefsSidebar` (filtre mémorisé, sections hoistées),
    `DiffPanel` (sélection et bases mémorisées), `CommitGraphLane` (lanes passantes calculées
    une fois par ligne), `PhoneReviewScreen` (`groupBy` mémorisé).

## Ce qui reste lent, et pourquoi

- **La review LLM (dizaines de secondes ; timeout à 90 s).** Le prompt peut atteindre
  120 000 caractères de contenu de fichiers en plus du diff, avec jusqu'à 4 rounds d'outils,
  sans streaming. Leviers, par ordre de sécurité :
  1. modèle rapide (fait pour les nouvelles installations) ;
  2. budget `MAX_PROMPT_TOTAL_CHARS` à 60 000 (moins de tokens d'entrée, donc moins de
     latence ; perte de contexte à valider sur la qualité) ;
  3. streaming ou affichage des nits au fil de l'eau (gros changement UI) ;
  4. cache d'une review pour le dépôt de démo (déjà prévu par `CONTEXT.md` §14).
- **`git status()` à chaque diff (~20–30 ms).** C'est le plancher du diff filtré. Un cache
  indexé sur `HEAD` + mtime de l'index éviterait la plupart des refreshes d'onglet, au prix
  d'un risque de diff périmé après une édition externe. Non fait : le gain ne valait pas le
  risque de correctness.
- **Démarrage desktop (~1,4 s jusqu'à la première composition).** Surtout du chargement
  Compose/Skiko et des ressources ; pas de levier simple sans dégrader le premier écran.
- **Android.** `isMinifyEnabled = false` en release et aucune baseline profile : la cold
  start et le débit Compose en debug sont nettement moins bons qu'en release. À traiter
  après le freeze, avec des règles R8 testées (Compose, Koog, JGit).
- **Graphe.** 500 lignes, `LazyColumn`, lanes hors thread UI : déjà conforme au fallback de
  `CONTEXT.md`. Le dessin alloue encore un `Path` par courbe et par frame ; négligeable au
  regard du reste.

## Vérification

- `./gradlew :shared:jvmTest :server:test :desktopApp:assemble` : **vert**, avec les nouveaux
  tests (tri topologique sous horloge inversée, diff sur arbre propre, `stage()` groupé,
  compteurs de diff, settings sans écriture inutile).
- `./scripts/check.sh` ne peut pas aller au bout sur cette machine : le SDK Android est
  introuvable (`ANDROID_HOME` et `local.properties` absents), donc
  `:shared:testAndroidHostTest` et `:androidApp:assembleDebug` ne démarrent pas. Ce n'est
  pas une régression du code ; à relancer sur une machine avec le SDK.

## Note de travail

Le diff non commité déjà présent au début de la passe (`AppContainer`, `LlmReviewAgent`,
`ReviewToolbox` : trace des appels d'outils) n'est **pas** inclus dans ce commit : il est
resté dans le working tree. Les mesures et cette passe n'ont pas touché au contrat
`GitRepository` ni au format des reviews partagées.
