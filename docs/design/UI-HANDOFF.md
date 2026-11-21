# UI handoff — maquette Nitpick → Compose

> À lire avec `docs/DESIGN-SYSTEM.md` (Eliot), **qui fait foi** pour les tokens, les tailles
> et les composants. Ce document dit ce que la maquette ajoute : les écrans, leurs états,
> les textes, et le seul écart de tokens (l'accent vert).
>
> - Maquette interactive : https://claude.ai/artifact/1PBvUhR38NmGVYzp6ajL9e
> - Source HTML de la maquette : `docs/design/mockups/` (`Main.dc.html` = desktop,
>   `Phone.dc.html` = Android, `Tokens.dc.html` = planche de tokens). Les données de démo et
>   les couleurs exactes sont dans le bloc `renderVals()` en bas de chaque fichier.
>   Le HTML est une **référence visuelle**, pas du code à porter : on reconstruit en Compose
>   avec les composants de `ui/components/`.

## 0. Règles pour l'implémentation

1. Aucune couleur littérale dans un composant : tout passe par `NitpickTheme.colors`.
2. Réutiliser les composants existants avant d'en créer (`NitpickButton`, `NitpickBadge`,
   `NitpickTextField`, `NitpickToolbar`, `NitpickToolWindowHeader`, `NitpickTab`,
   `NitpickStatusBar`, `ReviewCommentCard`, `DiffLineRow`, `RefBadge`, `NitpickKbd`,
   `NitpickEmptyState`, `NitpickCard`).
3. Chaque nouveau composant va dans `ui/components/` et dans `DesignSystemCatalog.kt`.
4. Toutes les chaînes vont dans `composeResources/values/strings.xml` **et**
   `values-fr/strings.xml` (§6).
5. Desktop : densité IDE (28 dp). Android : mêmes couleurs, cibles tactiles 44–48 dp.
6. Vérification : la commande du §15 de `DESIGN-SYSTEM.md`, plus le catalogue en clair et
   en sombre.

## 1. Seul écart de tokens : l'accent vert

Dans `NitpickTokens.kt`, on ne change que la famille primary. Tout le reste garde les
valeurs expUI d'Eliot.

| Token | Light (avant → après) | Dark (avant → après) |
| --- | --- | --- |
| `primary` | `#3574F0` → `#0F766E` | `#3574F0` → `#5EEAD4` |
| `onPrimary` | `#FFFFFF` (inchangé) | `#FFFFFF` → `#0B2E2A` (le blanc est illisible sur le teal clair) |
| `primaryHover` | `#4682FA` → `#115E59` | `#467FF2` → `#7FF0DE` |
| `primarySoft` | `#D4E2FF` → `#D2EFEA` | `#2E436E` → `#1F403C` |
| `selection` | `#D4E2FF` → `#D2EFEA` | `#2E436E` → `#1F403C` |
| `hover` | `#EDF3FF` → `#EDF7F5` | `#393B40` (inchangé) |
| `lanePalette[0]` | `#3574F0` → `#0F766E` | `#3574F0` → `#5EEAD4` |
| `lanePalette[5]` | `#058B90` → `#3574F0` | `#24A394` → `#548AF7` |

`lanePalette[5]` : le cyan se confondait avec le teal, on y met l'ancien bleu.
Mettre à jour le §3.1 et le §3.4 de `DESIGN-SYSTEM.md` en conséquence (« le bleu est
réservé… » devient « le teal est réservé… »).

## 2. Sévérités : la maquette vs le domaine

Le domaine a `Severity { INFO, SUGGESTION, WARNING, BUG }`. La maquette affiche
BUG / WARNING / NIT / STYLE. **Le code fait foi**, on garde l'enum et on mappe :

| Maquette | `Severity` | `NitpickTone` | Badge |
| --- | --- | --- | --- |
| BUG | `BUG` | `Danger` | `BUG` |
| WARNING | `WARNING` | `Warning` | `WARNING` |
| NIT | `SUGGESTION` | `Suggestion` | `SUGGESTION` |
| STYLE | `INFO` | `Info` | `INFO` |

Ordre de tri partout : BUG, WARNING, SUGGESTION, INFO, puis numéro de ligne.
Format du résumé : `"{total} nits · {bugs} bug"` (le segment bug n'apparaît que s'il y en a).

## 3. Desktop (1440×900)

### 3.1 Shell

```text
┌ Toolbar 40 : [icône] Nitpick / nitpick-app ▾  ⑂ feature/nit-sheet ▾      Fetch  Pull 2  Push 3 │ [Nitpick branch]  ⚙ ┐
├ BRANCHES (240) ─┬ COMMITS (flex) ─────────────────────┬ [Details|Diff|Review] (320, 680 si Diff/Review) ┤
│ filtre 28       │ filtre « Text or hash » + Branch/User/Date ▾ │                                       │
│ arbre 24 dp     │ lignes 40 dp : sujet + refs / auteur · date · hash │                                 │
├ Status bar 22 : ⑂ feature/nit-sheet  214 commits  ↑3 ↓0          Anthropic · claude-sonnet-4-5  Git ┤
```

- **Toolbar** (`NitpickToolbar`) : sélecteur de repo et de branche à gauche ; à droite Fetch,
  Pull (compteur entrant), Push (compteur sortant) en `NitpickToolbarAction`, séparateur,
  puis le seul bouton primaire **Nitpick branch** (icône pince), puis Settings (icône).
  Au survol de Nitpick branch : tooltip `surfaceRaised`, rayon 10, « Review this branch
  against main » + `NitpickKbd` ⌘⇧R.
- **Branches** : `NitpickToolWindowHeader("BRANCHES")` + bouton +, champ filtre, arbre :
  `Local` / `Remote branches` / `Tags`, nœuds dépliables, branche courante en `selection`
  avec ↑ ahead à droite.
- **Commits** : header `COMMITS` + « 214 commits » à droite ; barre de filtres ; le graphe
  existant (`CommitGraph.kt`) en lignes de 40 dp sur deux lignes (sujet + `RefBadge`s ;
  puis `small` : auteur · date relative · hash mono). Commit sélectionné en `selection`.
  Point HEAD = anneau ; merge = cercle creux.
- **Inspector** : `NitpickTab`s Details / Diff / Review. Quand la review n'est pas ouverte,
  l'onglet Review porte un badge `1 BUG`. Largeur 320 dp sur Details, **680 dp** sur Diff et
  Review (le centre se réduit ; c'est un choix de la maquette, à valider).

### 3.2 Onglet Details

Titre du commit (`section`), corps (`body`, `textSecondary`), grille Author / Date / Commit
/ Parents (parent cliquable en `primary`), `RefBadge`s, séparateur, `4 FILES` (kicker) avec
`+169 −15` à droite, liste des fichiers (lignes 24 dp, `+n` vert, `−n` rouge).

### 3.3 Onglet Diff

Ligne 40 dp : `feature/nit-sheet vs [Base: main ▾]` (sélecteur, défaut `main`) ;
« 3 commits · 6 files » et `+214 −37` à droite. Liste des fichiers (24 dp, fichier
sélectionné en `selection`). Puis en-tête de fichier (28 dp, chemin mono), en-tête de hunk
(`surfaceSunken`, `codeSmall`), et `DiffLineRow`s.

### 3.4 Onglet Review (résultat)

1. **Carte résumé** (`NitpickCard`) : badge `AI` (ton `Ai`), « 8 nits · 1 bug » (`section`),
   méta « vs main · 6 files · claude-sonnet-4-5 · 34 s », bouton ghost Re-run ; paragraphe de
   verdict ; une rangée de badges compteurs par sévérité (« 1 BUG », « 2 WARNINGS »…).
2. **Un bloc par fichier**, fichiers triés par la pire sévérité : en-tête 32 dp (chemin mono,
   badges compteurs, bouton ghost **Apply all for this file** à droite), puis les hunks.
3. **Nits ancrés sous leur ligne** (`ReviewCommentCard`, décalé de 56 dp pour laisser les
   gouttières visibles) : badge sévérité + `fichier:ligne` mono + **Dismiss** ; message ;
   bloc `SUGGESTED CHANGE` (kicker en couleur `suggestion`) avec une ligne `-` avant
   (`diffRemovedBg`) et une ligne `+` après (`diffAddedBg`) ; bouton primaire small
   **Apply suggestion**.
   → `ReviewCommentCard` a déjà `suggestion` ; il faut ajouter le texte **avant** pour
   afficher la paire `-` / `+`.
4. **Nit appliqué** : la carte se replie en une ligne 28 dp, fond `suggestionSoft`, bord
   `suggestion` 32 %, ✓ « Applied » + titre + Undo.
5. **Barre de commit** (nouveau composant, collé en bas de l'inspector, fond `panelHeader`,
   bord haut) : « 1 suggestion applied · NitAnchor.kt », `NitpickTextField` message de
   commit prérempli, bouton Secondary **Later**, bouton Primary **Stage & commit**.
   Elle apparaît dès qu'une suggestion est appliquée.

### 3.5 Settings

Dialogue 860×560, rayon 10, `surface`. Gauche 220 dp (`canvas`) : champ Search + sections
General (sélectionnée), Review, Git, Keymap. Droite :

- Kicker `APPEARANCE` → **Theme** : contrôle segmenté System / Light / Dark (28 dp, conteneur
  `surfaceSunken` + bord, segment actif `surface` + bord).
- Kicker `AI REVIEW` → **LLM provider** : 4 radios horizontales Anthropic / OpenAI / Google /
  Ollama (radio active pleine `primary`, point `onPrimary`) ; **Model** : liste déroulante
  300 dp, valeur mono ; **API key** : champ mot de passe 380 dp + bouton œil, aide 11 sp
  « Stored in the system keychain. Sent only to Anthropic. »
- Pied : Cancel (Secondary), Save (Primary).

`SettingsScreen.kt` existe déjà avec ces champs : appliquer ce layout côté desktop.

### 3.6 États

| État | Où | Contenu |
| --- | --- | --- |
| Aucun repo | toute la fenêtre (toolbar + canvas + status bar « no branch ») | `NitpickEmptyState` : icône dossier, « No repository open », « Pick a local git repository to see its commits and branches. », une seule action **Open repository…** + `NitpickKbd` ⌘O. Pas de Fetch/Pull/Push ni de CTA dans la toolbar, juste « Open repository… ». |
| Review en cours | onglet Review + toolbar + status bar | Carte : badge AI, « Nitpicking feature/nit-sheet vs main », Cancel ; barre de progression 4 dp ; 4 étapes : ✓ « Collecting diff against main » (« 6 files · +214 −37 »), spinner « Reading 12 files… » (« 7 / 12 »), ○ « Asking the model… », ○ « Placing nits on lines ». En dessous, 3 squelettes de cartes. Le CTA devient « Nitpicking… » avec spinner. La status bar montre « Reading 12 files… » + mini barre. |
| Branche propre | onglet Review | Icône ✓ `success`, « Clean branch. Nothing to nitpick. », « Reviewed 6 files on feature/nit-sheet against main. No bugs, no warnings, no nits. », action **Push 3 commits**. |
| Clé API manquante | onglet Review | Bannière ton Warning (badge `WARNING` + « No API key for Anthropic. ») ; état vide : icône clé, « Add an API key to start », « Nitpick needs an Anthropic API key to ask the model. Your code stays on this machine until you run a review. », action **Open Settings**. |
| Réseau | onglet Review | Bannière ton Danger (`ERROR` + « Review failed: network error. ») ; « Could not reach Anthropic », « The review request failed before reaching the provider. Check your connection, then retry. Nothing was sent. », code `ENOTFOUND api.anthropic.com` en `NitpickKbd`-like, action **Retry**. |
| Timeout LLM | onglet Review | Bannière Danger (« Review failed: request timed out. ») ; « The model took too long », « claude-sonnet-4-5 did not answer within 90 s. Large diffs can take longer: try again. », action **Try again**. |

Une seule action par état. Le nom du provider et du modèle viennent des settings.

## 4. Android (393×852)

Barre du haut 56 dp couleur `toolbar` (sombre dans les deux thèmes), fond `canvas`,
cartes `surface` + bord 1 px (pas d'ombre), boutons 48–56 dp rayon 8, champs 48 dp rayon 6
avec label 12 sp au-dessus (pas de label flottant).

1. **Repos** : barre (icône app, « Repositories », Settings) ; champ de recherche ; une carte
   par repo (nom, branche mono, « reviewed 2 h ago », badge `1 BUG` / `CLEAN` / `NEW`) ;
   FAB étendu primaire 56 dp « Clone repo » en bas à droite.
   - **Clone repo** (plein écran, croix) : texte d'aide ; champs HTTPS URL, Access token
     (mot de passe + œil ; aide « Needs read access. Push needs write access. »), Folder name ;
     bouton pleine largeur 56 dp **Clone** en bas.
2. **Graph** : barre (retour, nom du repo, Settings, menu) ; carte branche : sélecteur
   `feature/nit-sheet ▾` + « ↑3 ↓0 » ; 3 boutons Secondary 44 dp Fetch / Pull 2 / Push 3 ;
   graphe compact (lignes 60 dp, sujet + « auteur · date · hash ») ; FAB étendu primaire
   60 dp **Nitpick branch** en bas à droite.
3. **Review** :
   - **Fichiers** : barre (retour, « Review », sous-titre « feature/nit-sheet vs main »,
     Re-run) ; carte résumé (badge AI, « 8 nits · 1 bug », verdict, compteurs) ; liste
     « Files with nits · 4 » (ligne 72 dp : nom, dossier, badges, chevron) ; « 2 more files
     reviewed, no nits. »
   - **Diff** : barre (retour, nom du fichier, « review/ · +18 −3 ») ; ligne « 2 nits ·
     1 applied » + bouton Secondary 44 dp **Apply all for this file** ; diff mono 12 sp, une
     colonne de numéros ; nits en lignes compactes 56 dp (badge + titre + chevron), le tap
     ouvre la bottom sheet ; nit appliqué = ligne verte « Applied » ; snackbar
     « 1 suggestion applied » + action **Stage & commit**.
   - **Bottom sheet** (scrim + feuille rayon 12 en haut) : poignée ; badge + « NitAnchor.kt ·
     line 23 » + « 1 of 8 » ; titre ; message ; bloc `SUGGESTED CHANGE` (`-` / `+`) ; deux
     boutons 56 dp côte à côte : **Dismiss** (Secondary) et **Apply suggestion** (Primary).
4. **Settings** : Theme (segmenté 48 dp), LLM provider (liste radio, 52 dp par ligne, avec
   sous-titre ; Ollama : « Self-hosted, needs a server URL »), Model, API key (« Stored in
   Android Keystore. »).

**États Android** : mêmes textes que le desktop, centrés, action unique en bouton pleine
largeur 56 dp en bas (Clone repo, Cancel, Push 3 commits, Open Settings, Retry, Try again).
Aucun repo : « No repository open » / « Clone a git repository over HTTPS to see its commits
and branches. »

## 5. Nouveaux composants à créer

| Composant | Rôle | Notes |
| --- | --- | --- |
| `NitpickSegmentedControl` | Theme | 28 dp desktop, 48 dp Android |
| `NitpickRadioGroup` | LLM provider | horizontal desktop, liste Android |
| `ReviewSummaryCard` | résumé + compteurs | badge `Ai`, compteurs triés |
| `ReviewFileSection` | bloc fichier + Apply all | contient `DiffLineRow` et `ReviewCommentCard` |
| `AppliedNitRow` | nit replié | ton `Suggestion` |
| `CommitComposerBar` | après Apply | champ + Later + Stage & commit |
| `ReviewProgress` | état de chargement | 4 étapes, barre, squelettes |
| `NitpickBanner` | erreurs de review | ton Warning ou Danger, badge + texte |
| `NitpickTooltip` | toolbar | texte + `NitpickKbd` |
| `NitBottomSheet` | Android | `ModalBottomSheet` stylé avec les tokens |

## 6. Chaînes à ajouter (EN / FR)

| Clé | EN | FR |
| --- | --- | --- |
| `review_cta` | Nitpick branch | Nitpicker la branche |
| `review_cta_running` | Nitpicking… | Analyse… |
| `review_cta_tooltip` | Review this branch against main | Relire cette branche par rapport à main |
| `tab_details` | Details | Détails |
| `tab_diff` | Diff | Diff |
| `tab_review` | Review | Review |
| `diff_base` | Base | Base |
| `review_summary` | %1$d nits · %2$d bug | %1$d nits · %2$d bug |
| `review_rerun` | Re-run | Relancer |
| `review_apply_all_file` | Apply all for this file | Tout appliquer pour ce fichier |
| `review_apply` | Apply suggestion | Appliquer |
| `review_dismiss` | Dismiss | Ignorer |
| `review_suggested_change` | SUGGESTED CHANGE | MODIFICATION PROPOSÉE |
| `review_applied` | Applied | Appliqué |
| `review_undo` | Undo | Annuler |
| `commit_bar_applied` | %1$d suggestion applied | %1$d suggestion appliquée |
| `commit_message` | Commit message | Message de commit |
| `commit_later` | Later | Plus tard |
| `commit_stage_commit` | Stage & commit | Indexer et committer |
| `review_step_diff` | Collecting diff against %1$s | Collecte du diff avec %1$s |
| `review_step_read` | Reading %1$d files… | Lecture de %1$d fichiers… |
| `review_step_model` | Asking the model… | Interrogation du modèle… |
| `review_step_place` | Placing nits on lines | Placement des nits |
| `review_cancel` | Cancel | Annuler |
| `review_clean_title` | Clean branch. Nothing to nitpick. | Branche propre. Rien à redire. |
| `review_clean_action` | Push %1$d commits | Pousser %1$d commits |
| `error_key_title` | Add an API key to start | Ajoutez une clé API pour commencer |
| `error_key_action` | Open Settings | Ouvrir les réglages |
| `error_network_title` | Could not reach %1$s | Impossible de joindre %1$s |
| `error_network_action` | Retry | Réessayer |
| `error_timeout_title` | The model took too long | Le modèle a mis trop de temps |
| `error_timeout_action` | Try again | Réessayer |
| `settings_api_key_help` | Stored in the system keychain. Sent only to %1$s. | Stockée dans le trousseau système. Envoyée uniquement à %1$s. |
| `clone_title` | Clone repo | Cloner un repo |
| `clone_url` | HTTPS URL | URL HTTPS |
| `clone_token` | Access token | Jeton d'accès |
| `clone_folder` | Folder name | Nom du dossier |
| `clone_action` | Clone | Cloner |

Les textes longs des états (corps des messages) sont au §3.6 ; les traduire dans le même
esprit, phrases courtes. Garder les termes git en anglais (commit, branch, diff, push, nit).

## 7. Ordre de travail proposé

1. Tokens (§1) + mise à jour de `DESIGN-SYSTEM.md` + catalogue clair/sombre.
2. Shell desktop : toolbar avec CTA, tool windows, status bar, inspector à onglets.
3. Onglet Diff avec sélecteur de base.
4. Onglet Review : résumé, sections fichiers, nits inline, Apply / Dismiss, nit appliqué,
   barre de commit.
5. États (vide, chargement, propre, 3 erreurs).
6. Settings desktop.
7. Android : Repos + Clone, Graph + FAB, Review (fichiers, diff, bottom sheet), Settings,
   états.

Chaque étape : compile JVM + desktop, captures clair/sombre comparées à la maquette.
