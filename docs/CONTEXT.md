# Nitpick — Contexte projet : LauzHack 2026, challenge JetBrains (Kotlin Multiplatform)

Ce document est le contexte maître du projet. Tout agent (orchestrateur ou subagent) le lit
avant de produire quoi que ce soit. En cas de doute sur une décision de scope, ce document
fait foi. Si une demande le contredit, l'agent le signale au lieu de trancher seul.

## 1. Mission

Construire en 24 heures (du samedi 21.11.2026 à 12h00 au dimanche 22.11.2026 à 12h00)
Nitpick. C'est un client git visuel multiplateforme (dans l'esprit de Fork ou SourceGit)
avec une review de code par IA intégrée sur la branche courante : commentaires ancrés sur
les lignes, comme une review de pull request, et suggestions applicables en un clic.

**Le problème** : avec les agents de code, plus personne ne lit vraiment ce qui part sur
main. Les outils de review IA existants (CodeRabbit, Copilot review, Graphite…)
interviennent après le push, sur la PR, côté serveur.

**Notre différence** : une review en local, avant le push, avec le contexte du repo, et
branchée sur le LLM choisi par l'utilisateur (Claude, OpenAI, Gemini, ou Ollama en local).

### Identité

- **Nom** : Nitpick. Dans le jargon de la review, un « nit » est un petit commentaire sur
  une ligne précise. Le nom dit la feature phare : la review ligne par ligne.
- **Tagline** : « Nitpick your branch before main does. » (FR : « Faites pinailler votre
  branche avant qu'elle n'arrive sur main. »)
- **Action principale** : le bouton « Nitpick this branch » (FR : « Nitpicker cette
  branche »). Un commentaire de review s'appelle un nit dans l'UI (« 7 nits · 1 bug »).
- **Identifiants techniques** : nom Gradle `nitpick`, package `dev.nitpick.app`,
  applicationId Android `dev.nitpick.app`, fenêtre desktop titrée « Nitpick ».
- **Ton visuel** : outil de dev sobre et précis, aligné sur la DA **JetBrains IDE /
  New UI** (`docs/DESIGN-SYSTEM.md`). Accent unique **teal** (clair/sombre), chrome
  graphite, contrôles 28 dp, code en JetBrains Mono. Icône d'app simple (loupe ou pince à
  épiler stylisée, rien de générique).

## 2. Ce que le jury évalue (et donc ce qu'on optimise)

- **Implémentation / proof of concept** : l'app tourne réellement sur au moins 2
  plateformes, et la fonctionnalité centrale marche.
- **UX / UI** : un rendu soigné et natif sur chaque plateforme, pas seulement fonctionnel.
- **Technique** : la part de logique réellement partagée dans commonMain par rapport au code
  dupliqué par plateforme. Points bonus pour : Compose Multiplatform, localisation, logique
  IA/agent partagée (Koog), backend Kotlin.
- **Innovation / créativité** : l'originalité de l'idée.
- **Process agentique (bonus)** : construire avec **JetBrains Air** dans l'IDE. Air
  orchestre côte à côte les agents (Junie, Codex, Gemini, GitHub Copilot, Claude, autres via
  ACP) ; les humains relisent, corrigent et pilotent au lieu de tout accepter. Air est un
  **outil de développement**, pas un provider IA ni une dépendance runtime de Nitpick.
  Bonus supplémentaire pour le multi-agent (agents différents sur le code partagé et sur le
  code plateforme, ou comparaison de leurs résultats).
- **Présentation** : une démo claire, qui explique ce qui est partagé et ce qui reste
  spécifique à chaque plateforme.

Features bonus explicitement listées : dark/light mode, localisation, Compose
Multiplatform, backend Kotlin, IA partagée via Koog. Les quatre que nous visons
(dark/light, localisation, Compose MP, Koog) sont obligatoires pour nous.

> Le challenge recommande Air pour **construire** le projet. Le moteur IA **du produit**
> reste Koog : Air/Junie ne sont pas appelés par l'app. Le brief officiel est dans
> [`docs/JETBRAINS-CHALLENGE.md`](JETBRAINS-CHALLENGE.md).

## 3. Équipe et contraintes humaines

3 personnes, toutes débutantes en Kotlin. Elles sont développeurs par ailleurs (fullstack,
TypeScript/React Native), donc les concepts généraux sont acquis, mais la syntaxe Kotlin,
Gradle, KMP et Compose sont nouveaux.

Conséquences pour les agents :

- Expliquer brièvement chaque choix non trivial (en commentaire de PR ou de commit, pas en
  commentaire de code verbeux), pour que l'humain puisse relire et comprendre.
- Privilégier du Kotlin simple et lisible à du Kotlin « malin ». Pas de DSL maison, pas de
  génériques gratuits, pas de métaprogrammation.
- Ne jamais livrer du code qui ne compile pas. Avant de rendre la main, lancer le build
  desktop et Android.
- Les humains relisent tout et rejettent ce qui ne va pas. C'est un critère de jugement
  (voir §11).
- Chaque humain installe le plugin **Air** dans son IDE et connecte un agent (Junie Lite en
  free tier, ou Codex/Gemini/Copilot/Claude). Chaque piste travaille dans Air, avec un
  humain qui relit les diffs.
- Les preuves de process (agents lancés, corrections humaines) vont dans `AGENT_LOG.md` et
  `REVIEW_LOG.md` ; garder 2–3 captures Air pour la démo.

## 4. Plateformes cibles

| Cible | Rôle | Priorité |
| --- | --- | --- |
| Desktop JVM (macOS, Windows, Linux) | L'app principale : client git complet + review IA | P0 |
| Android | Mode « review en déplacement » : cloner un repo en HTTPS avec un token, voir le graph, lancer la review IA, accepter des suggestions, commit et push | P0 |
| iOS / Web | Hors scope. Pas de JGit sur ces cibles, il faudrait réécrire tout le backend git. | ✗ |

macOS, Windows et Linux forment une seule cible KMP. Android est la deuxième plateforme
obligatoire et ne doit pas être une coquille vide.

## 5. Stack technique

Toujours prendre la dernière version stable de chaque lib. Ne pas inventer d'API : vérifier
dans la doc officielle (liens §15) avant d'écrire du code Koog ou Compose. Si une API a
changé, l'adapter et le signaler.

- **Langage** : Kotlin (dernière stable), Kotlin Multiplatform.
- **Projet** : généré depuis le wizard kmp.jetbrains.com (cibles Android + Desktop, UI
  partagée en Compose).
- **UI** : Compose Multiplatform + Material 3, toute l'UI dans commonMain, tokens et
  composants dans `docs/DESIGN-SYSTEM.md` (DA JetBrains New UI, accent teal, shell IDE).
- **Thème** : clair et sombre, qui suit le système par défaut, avec un toggle manuel dans
  les réglages.
- **Localisation** : Compose Multiplatform Resources
  (`composeResources/values/strings.xml` et `values-fr/strings.xml`). Anglais par défaut,
  français complet. Aucune string UI en dur.
- **État** : ViewModel multiplateforme (lifecycle-viewmodel-compose de JetBrains) +
  StateFlow. Architecture simple de type MVI/UDF : State immuable, Intent/actions,
  ViewModel.
- **Navigation** : un simple état d'écran dans un ViewModel racine suffit. Ajouter une lib
  de navigation seulement si c'est vraiment nécessaire.
- **DI** : injection manuelle (un objet `AppContainer`). Pas de Koin ni de Hilt, pour
  réduire la surface d'apprentissage.
- **Concurrence** : Kotlin Coroutines. Les opérations git et LLM tournent toujours hors du
  thread UI (`Dispatchers.IO` côté JVM).
- **Sérialisation** : kotlinx.serialization.
- **Git** : JGit (Eclipse), utilisé derrière une interface définie dans commonMain.
- **IA produit** : Koog (framework d'agents Kotlin de JetBrains), dans commonMain. Koog
  supporte JVM, Android, iOS, JS et Wasm, ainsi que plusieurs fournisseurs (Anthropic,
  OpenAI, Google, Ollama…). C'est la pièce qui démontre le « shared AI logic » demandé par
  le jury.
- **Développement** : JetBrains Air + agents (Junie, Codex, Gemini, Copilot, Claude) pour
  produire le code. Ce sont des outils d'IDE, pas des dépendances de l'app.
- **Stockage local** (réglages, clé API, repos récents) : un expect/actual minimal (fichier
  de préférences côté desktop, SharedPreferences/DataStore côté Android). Ne jamais
  commiter de clé API.

## 6. Architecture et répartition du code

Objectif : le maximum de code dans commonMain. Le code plateforme doit être mince et
ennuyeux.

```text
composeApp/src/
├── commonMain/            ← TOUT ce qui peut l'être
│   ├── domain/            modèles purs : Commit, Ref, Branch, FileDiff, Hunk, DiffLine,
│   │                      ReviewComment, Suggestion, ReviewResult
│   ├── git/               interface GitRepository (contrat), GitRepositoryFactory (expect)
│   ├── graph/             algorithme de lanes du graph de commits (100 % pur Kotlin, testé)
│   ├── diff/              parsing du diff unifié → modèle Hunk/DiffLine avec numéros de ligne
│   ├── review/            agent Koog, construction du contexte repo, parsing/validation de
│   │                      la sortie, application des suggestions (patch sur le texte)
│   ├── settings/          modèle des réglages + interface SettingsStore (expect)
│   ├── ui/                tous les écrans et composants Compose, thème, ViewModels
│   └── composeResources/  strings EN/FR, icônes
├── jvmShared/             ← source set intermédiaire partagé desktop + Android
│   └── git/               JGitRepository : implémentation de GitRepository avec JGit
├── desktopMain/           main(), fenêtre, sélecteur de dossier natif, SettingsStore desktop
└── androidMain/           MainActivity, SettingsStore Android, emplacement de stockage des clones
```

`jvmShared` est un source set intermédiaire dont dépendent desktopMain et androidMain (via
`applyDefaultHierarchyTemplate { common { group("jvmShared") { withJvm(); withAndroidTarget() } } }`
ou un `dependsOn` explicite, selon ce que la version de Kotlin accepte). Ainsi,
l'implémentation JGit n'est écrite qu'une fois.

**Règle d'or** : aucun import `org.eclipse.jgit` hors de jvmShared. Aucun import Android ou
AWT hors de son source set.

Prévoir à la fin un script ou une tâche Gradle simple qui compte les lignes par source set,
pour la slide « shared vs platform-specific ».

### Contrats à figer EN PREMIER (heure 1)

Ces interfaces et modèles sont écrits et validés par un humain avant le travail en
parallèle, pour que les pistes ne se bloquent pas entre elles :

```kotlin
interface GitRepository {
    suspend fun log(limit: Int = 500): List<Commit>
    suspend fun refs(): List<Ref>
    suspend fun currentBranch(): String
    suspend fun status(): WorkingTreeStatus
    suspend fun diffAgainst(baseRef: String): List<FileDiff>   // branche courante vs base
    suspend fun readFile(path: String): String?
    suspend fun listFiles(): List<String>
    suspend fun writeFile(path: String, content: String)
    suspend fun stage(paths: List<String>)
    suspend fun commit(message: String)
    suspend fun fetch(credentials: Credentials?)
    suspend fun pull(credentials: Credentials?): PullResult
    suspend fun push(credentials: Credentials?)
    suspend fun checkout(branch: String)
}
```

Modèle de review (à affiner, mais la forme doit rester celle-ci) :

```kotlin
@Serializable data class ReviewResult(val summary: String, val comments: List<ReviewComment>)
@Serializable data class ReviewComment(
    val id: String,
    val filePath: String,
    val startLine: Int,            // numéro de ligne dans la NOUVELLE version du fichier
    val endLine: Int,
    val severity: Severity,        // INFO, SUGGESTION, WARNING, BUG
    val category: String,          // ex. "security", "perf", "readability", "logic"
    val message: String,
    val originalSnippet: String,   // les lignes visées, recopiées : sert à vérifier l'ancrage
    val suggestion: String? = null // code de remplacement pour startLine..endLine, ou null
)
```

## 7. La review IA (cœur du projet)

### Flux

1. L'utilisateur est sur une branche et choisit une base (main par défaut).
2. On calcule `diffAgainst(base)`.
3. On construit le contexte repo : arborescence des fichiers (tronquée), contenu complet des
   fichiers modifiés, README, 10 derniers messages de commit, langage ou framework détecté.
4. L'agent Koog produit un `ReviewResult` en sortie structurée (Koog sait le faire
   nativement, avec des retries et une correction de sortie quand le modèle ne supporte pas
   le JSON natif).
   > Le moteur de review est Koog. Air/Junie servent à construire et relire le code pendant
   > le hackathon ; ils ne font pas partie du runtime de Nitpick.
5. **Validation de l'ancrage** : pour chaque commentaire, on vérifie que `originalSnippet`
   correspond bien aux lignes `startLine..endLine` du fichier. Sinon, on recherche le
   snippet dans le fichier pour recaler les numéros. Sinon, le commentaire est rattaché au
   fichier entier. Un commentaire mal ancré ne doit jamais faire planter l'UI.
6. L'UI affiche le diff avec les commentaires inline (style PR GitHub), triés par sévérité.
7. **Appliquer une suggestion** : on revérifie que les lignes n'ont pas bougé, on remplace,
   on écrit le fichier, on rafraîchit le diff, puis on propose de stage et commit. Il faut
   aussi « Ignorer » et « Tout appliquer pour ce fichier ».

### Ancrage fiable

- Dans le prompt envoyé au LLM, préfixer chaque ligne du diff par son numéro dans le nouveau
  fichier (`  42 | code`), et exiger que le modèle cite ces numéros et recopie
  `originalSnippet`.
- Limiter la taille : ignorer les lockfiles, les binaires et les fichiers générés, et ne
  garder que les N fichiers modifiés les plus importants si le diff est énorme.

### Agentique (bonus, si le temps le permet)

- Donner à l'agent Koog des outils `readFile(path)` et `listFiles()` pour qu'il aille
  chercher du contexte lui-même (par exemple la définition d'une fonction appelée dans le
  diff). C'est ce qui justifie le terme « avec le contexte du repo ».
- Toujours garder un mode sans outils (un seul appel) comme fallback, plus rapide et plus
  prévisible pour la démo.

### Fournisseurs

- Réglages : choix du fournisseur (Anthropic / OpenAI / Google / Ollama), du modèle et de la
  clé API.
- Tout ce code est dans commonMain. Seul le stockage de la clé est un expect/actual.

## 8. Scope

### MUST (sans ça, pas de démo)

- Ouvrir un repo local (desktop) ou cloner un repo en HTTPS avec un token (Android et
  desktop).
- Graph des commits avec lanes et couleurs, branches et tags affichés, sélection d'un commit
  avec ses détails.
- Diff de la branche courante par rapport à la base, avec coloration ajout/suppression.
- **Review IA → commentaires inline → appliquer une suggestion → commit.** C'est la boucle
  qui gagne. Elle doit marcher en premier, même sur une UI moche.
- Thème clair/sombre et localisation EN/FR.
- Ça tourne sur desktop et Android.

### SHOULD

- Fetch, pull, push et checkout de branche.
- Outils `readFile`/`listFiles` pour l'agent.
- Résumé global de la review et score ou compteur par sévérité.
- Streaming ou indicateur de progression pendant la review.
- Layout adaptatif : panneaux côte à côte sur desktop, navigation par écrans sur Android.

### WON'T (ne pas commencer, même si « c'est rapide »)

- Résolution de conflits interactive (c'est seulement de la roadmap dans le pitch).
- Rebase interactif, stash UI, submodules, LFS, SSH.
- iOS, web, backend serveur.
- Authentification OAuth GitHub (un token personnel suffit).
- Intégration runtime d'Air/Junie (appeler Air/Junie depuis l'app, en faire un provider de
  review ou lancer la CLI dans le produit) : hors scope pour le hackathon. Air/Junie sont
  les outils de **développement** ; le produit garde Koog.

## 9. Pistes de travail parallèles (multi-agent)

Chaque humain pilote une piste et son ou ses agents. Les pistes ne se touchent que via les
contrats du §6.

| Piste | Périmètre | Source sets |
| --- | --- | --- |
| A : Core partagé | Modèles, algorithme de lanes du graph (+ tests), parsing du diff, agent Koog de review, validation de l'ancrage, application des suggestions | commonMain (domain, graph, diff, review) |
| B : Git + desktop | JGitRepository, configuration Gradle des source sets, fenêtre desktop, sélecteur de dossier, écrans principaux en layout desktop | jvmShared, desktopMain, commonMain/ui |
| C : Android + finitions | Cible Android (clone HTTPS, stockage), layout mobile, thème clair/sombre, strings EN/FR, réglages, script de comptage des lignes partagées, préparation de la démo | androidMain, commonMain/ui, composeResources |

Pour coller au critère multi-agent : sur au moins une tâche (idéalement l'algorithme de
lanes ou le prompt de review), faire produire deux agents différents sur le même problème,
comparer leurs résultats, garder le meilleur, et documenter la comparaison.

**Process** : chaque piste est pilotée dans **Air** (agent Junie/Codex/Gemini/Copilot/Claude),
avec un humain qui relit chaque diff. Air/Junie ne sont pas une dépendance du produit : le
code de Nitpick ne les importe pas. Le fallback est Claude Code/Codex CLI, documenté dans
`AGENT_LOG.md`.

### 9.1 Propriété des issues (à maintenir par H2)

Mapping de travail (à corriger si un handle ne correspond pas) :

| Rôle | GitHub | Issues ouvertes | Air |
| --- | --- | --- | --- |
| **H1 — Produit & Pitch** | `@dovatgabriel` | #39 démo/soumission/preuves Air, #33 états de review | Une comparaison Junie + un second agent, captures, `AGENT_LOG`/`REVIEW_LOG` |
| **H2 — Architecte & Intégration** | `@zooldyyk` | #19 diff desktop, #20 agent Koog, #22 boucle review desktop | Une session Air sur le prompt/le diff, comparaison, review humaine |
| **H3 — Builder & Démo** | `@pepito2t` | #21 clone Android, #23 boucle review Android, #34/#35 settings Android/desktop | Une session Air sur l'UI/Android, capture, review humaine |

Règles :

- Une issue = un owner = un worktree ; les P2 #36–#38 attendent la fin du chemin critique.
- Chaque owner ajoute au moins une entrée `Agent via Air` dans `AGENT_LOG.md` et une
  correction/rejet humain dans `REVIEW_LOG.md`.
- Une seule comparaison multi-agent est nécessaire pour le bonus, mais elle doit être
  **réelle** (même tâche, deux agents, résultat conservé, raison du choix).


## 10. Planning (les checkpoints sont des points de contrôle bloquants)

| Heure | Objectif | Checkpoint |
| --- | --- | --- |
| 12h–13h | Projet généré via le wizard, build OK sur desktop et Android, contrats du §6 écrits et validés, spike JGit sur Android | ✅ « Hello » sur les 2 cibles, JGit lit un log sur Android |
| 13h–17h | A : lanes + diff + 1er appel Koog. B : JGitRepository + écran graph desktop. C : thème, strings, coquille Android. **Air installé sur les 3 laptops + premier agent loggé (17:30, #39)** | ✅ Graph réel affiché sur desktop, capture Air |
| 17h–21h | Boucle review complète sur desktop (diff → Koog → commentaires inline → apply) | ✅ Boucle review de bout en bout qui marche (checkpoint critique) |
| 21h–02h | Android branché sur le même core, clone HTTPS, fetch/pull/push, polish UI | ✅ Boucle review sur Android |
| 02h–07h | Dormir en rotation (au moins 3h chacun). Une personne au plus continue sur du polish non risqué. | |
| 07h–09h30 | Bugfix, outils agentiques si stables, finitions UX | ✅ Feature freeze à 09h30 |
| 09h30–11h | Repo de démo préparé, script de démo, images plein écran (partagé vs spécifique, process agentique : pas de slides, règlement LauzHack), vidéo de secours | |
| 11h–12h | Répétition et soumission | ✅ Soumis avant 12h |

**Règle** : si un checkpoint est raté de plus d'une heure, on coupe du scope (SHOULD →
WON'T). On ne repousse pas le checkpoint suivant.

## 11. Règles de travail pour les agents

- Lire ce document en entier avant toute tâche. Respecter le scope du §8.
- Petits incréments : une tâche = un commit ou une PR ciblée, avec un message clair en
  anglais (`feat(graph): ...`, `fix(review): ...`).
- Ça compile sur les deux cibles avant de rendre la main (`./scripts/check.sh` : tests
  partagés, `:desktopApp:assemble`, `:androidApp:assembleDebug`).
- Pas de nouvelle dépendance sans la justifier dans la PR.
- Code et commentaires en anglais, strings UI dans les fichiers de ressources EN et FR.
- Respecter les frontières de source sets (§6). Si une tâche semble exiger du code
  plateforme, le dire et proposer un expect/actual minimal.
- Ne pas halluciner d'API Koog, Compose ou JGit : vérifier dans la doc. En cas de doute,
  l'écrire noir sur blanc dans la PR.
- Aucun secret dans le repo (clés API, tokens). Utiliser un `.env` local ignoré par git ou
  les réglages de l'app.
- **Journal de l'agent** : l'agent écrit dans `progress/<ticket>.md` (agent, piste, tâche,
  fichiers touchés, décisions, ce qui reste incertain). L'humain qui a lancé la session (Air
  ou CLI) reporte une ligne dans `docs/AGENT_LOG.md` à la fin de la session (Q-017).
- **Les humains relisent et rejettent** : chaque rejet ou correction humaine est noté dans
  `docs/REVIEW_LOG.md` (quoi, pourquoi, ce qui a été changé). C'est la preuve du critère 5.
  Ne pas sauter cette étape.
- **Dogfooding** : dès que la review IA marche, s'en servir pour relire les branches
  produites par les agents, et garder 2 ou 3 captures des cas où elle a trouvé un vrai
  problème. C'est la chute de la présentation.
- **Air/Junie** : lancer les tâches agentiques dans Air, avec un humain qui relit chaque
  diff. Ne jamais importer Air/Junie dans le code de l'app : ce sont des outils de
  développement, pas des dépendances runtime.
- **Si Air est indisponible** : fallback Claude Code/Codex CLI, et le noter dans
  `AGENT_LOG.md`. Le produit ne dépend pas d'Air.

## 12. UX : exigences

- **Desktop** : layout 3 panneaux (sidebar avec branches et refs | graph et liste des
  commits | détails, diff et review). Raccourcis clavier pour les actions principales
  (fetch, review, appliquer la suggestion).
- **Android** : navigation par écrans (Repos → Graph → Diff + Review), gros boutons
  d'action, bottom sheet pour un commentaire de review.
- **Commentaires de review** : badge de sévérité coloré, message, bloc de diff « avant /
  après » pour la suggestion, boutons Appliquer et Ignorer.
- États vides, chargement et erreur soignés partout. Ce sont les détails qui font paraître
  l'app « native » et « polished » (critère 2).
- Police mono pour le code, coloration ajout/suppression lisible dans les deux thèmes.

## 13. Démo (la cible vers laquelle tout converge)

1. **Hook (20 s)** : « Qui ici a mergé du code écrit par une IA sans le lire en entier ?
   Nitpick, c'est le collègue pinailleur qu'on n'a plus le temps d'être. »
2. Ouvrir un repo sur desktop : le graph, les branches.
3. Se placer sur une branche écrite par un agent pendant le hackathon et cliquer sur
   « Nitpick this branch » : les nits apparaissent inline, dont un vrai bug.
4. Appliquer la suggestion, puis commit.
5. Sur Android : même repo, même review, même code. Accepter une suggestion depuis le
   téléphone, puis push.
6. Partagé vs spécifique, sans slide (règlement LauzHack, Q-007/Q-016) : la sortie du script
   de comptage des lignes par source set (#40) en live, ou une image plein écran avec le
   schéma du §6.
7. Process agentique, sans slide : 2–3 captures Air en images plein écran (agents côte à
   côte), les 3 pistes, la comparaison multi-agent, quelques entrées du REVIEW_LOG (« voici
   ce qu'on a refusé à nos agents »). Le message : on a utilisé Air/Junie pour construire,
   et notre propre outil pour relire nos agents.
8. Roadmap en une phrase : résolution de conflits assistée par IA, iOS via l'API GitHub.

Préparer un repo de démo dédié, avec une branche contenant des défauts volontaires mais
crédibles (bug off-by-one, injection SQL, ressource non fermée), plus une vidéo de secours
au cas où le Wi-Fi ou le LLM lâcherait.

## 14. Risques connus et fallbacks

| Risque | Fallback |
| --- | --- |
| JGit récent incompatible avec Android | Spike dès l'heure 1. Essayer le core library desugaring (java.nio) avec minSdk 26, sinon une version plus ancienne de JGit, sinon un fork Android de JGit. En dernier recours, Android lit le repo via l'API REST GitHub, derrière la même interface GitRepository (ça reste une démonstration propre de l'abstraction). |
| API Koog différente de ce qu'on croit | Doc officielle + exemples du repo GitHub de Koog. Commencer par l'appel le plus simple (un prompt, une sortie structurée), et n'ajouter les outils qu'ensuite. |
| Le LLM renvoie des numéros de ligne faux | La validation par `originalSnippet` (§7) et le rattachement au fichier entier en dernier recours. |
| Latence du LLM pendant la démo | Indicateur de progression, modèle rapide par défaut, et un résultat de review mis en cache pour le repo de démo (activable dans les réglages). |
| Graph lent sur un gros repo | Limiter à 500 commits, LazyColumn, et calcul des lanes hors du thread UI. |
| Gradle ou KMP qui bloque des débutants | Ne pas « optimiser » la config : rester au plus près du template du wizard. |
| Air/Junie indisponible (EAP, compte, réseau) | Utiliser Claude Code/Codex CLI ; le produit ne dépend pas d'Air ; documenter le fallback dans `AGENT_LOG.md`. |
| Air demande un trust projet / des permissions | Accepter le trust sur le repo, vérifier `.junie`/MCP, ne jamais exposer `.env` ; garder les agents dans le repo ouvert. |

## 15. Références

- Brief challenge JetBrains : [`docs/JETBRAINS-CHALLENGE.md`](JETBRAINS-CHALLENGE.md)
- Handoff de review : [`docs/HANDOFF.md`](HANDOFF.md)
- KMP templates / wizard : https://kmp.jetbrains.com
- Doc KMP : https://kotlinlang.org/docs/multiplatform/multiplatform-create-first-app.html
- Compose Multiplatform : https://www.jetbrains.com/compose-multiplatform/
- Koog : https://docs.koog.ai et https://github.com/JetBrains/koog
- Air in JetBrains IDEs (EAP) : https://blog.jetbrains.com/ai/2026/10/air-in-ides-eap/
- Air docs : https://www.jetbrains.com/help/air/
- Junie / Junie Lite : https://junie.jetbrains.com
- JGit : https://github.com/eclipse-jgit/jgit (cookbook : https://github.com/centic9/jgit-cookbook)
- Vidéo MVI + Compose MP : https://youtu.be/dveR4xWid4Q
