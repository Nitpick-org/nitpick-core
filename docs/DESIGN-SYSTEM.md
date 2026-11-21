# Design system — Nitpick

> **DA : JetBrains IDE / New UI (expUI).** Nitpick est un outil de développeur, pas un site
> marketing. L'interface doit ressembler à l'IDE qui héberge le challenge : graphite,
> dense, une seule couleur d'action, du code au centre, de la couleur uniquement quand elle
> porte un sens.
>
> La structure du document (tokens → composants → usages) s'inspire d'Oria, mais **aucun
> token ni composant d'Oria n'est repris** : les valeurs viennent des thèmes expUI
> d'IntelliJ IDEA (`themes/expUI/expUI_light.theme.json` et `expUI_dark.theme.json`) et du
> schéma éditeur (`DefaultColorSchemesManager.xml`).

## 1. Principes

1. **Le code est le héros.** Le chrome (toolbar, tool windows, status bar) est sombre et
   calme ; le diff, les commits et les nits prennent la lumière.
2. **Densité IDE.** Contrôles 28 dp, lignes 24 dp, toolbar 40 dp, status bar 22 dp. Pas de
   padding Material de 56 dp.
3. **Une seule action saturée.** Le bleu `#3574F0` est réservé aux actions primaires, au
   focus et à la sélection active. Le reste est graphite.
4. **La couleur est sémantique.** Vert = ajout/succès, rouge = suppression/erreur, jaune =
   avertissement, violet = IA. Jamais de couleur décorative.
5. **Deux plateformes, une langue.** Les tokens sont dans `commonMain`. Desktop garde la
   densité IDE ; Android agrandit les cibles tactiles à 44–48 dp sans changer les couleurs.
6. **Review avant push.** Un nit est lisible sans couleur : badge texte, gouttière `+/-`,
   numéro de ligne, icône. La couleur renforce, elle ne remplace pas.

## 2. Sources de vérité

| Source | Usage |
| --- | --- |
| `shared/src/commonMain/kotlin/dev/nitpick/app/ui/theme/NitpickTokens.kt` | couleurs, espacements, rayons, tailles |
| `ui/theme/NitpickTypography.kt` | échelle UI + code |
| `ui/theme/NitpickTheme.kt` | thème Compose, light/dark, mapping Material 3 |
| `ui/components/` | composants publics |
| `ui/designsystem/DesignSystemCatalog.kt` | catalogue visuel (preview IDE) |
| Thèmes IntelliJ `expUI_light/dark` | valeurs de référence |
| `DefaultColorSchemesManager.xml` | couleurs éditeur, diff, numéros de ligne |

## 3. Couleurs

### 3.1 Base (light / dark)

| Token | Light | Dark | Usage |
| --- | --- | --- | --- |
| `canvas` | `#F7F8FA` | `#1E1F22` | fond de l'app, zones vides |
| `surface` | `#FFFFFF` | `#2B2D30` | panneaux, tool windows |
| `surfaceRaised` | `#FFFFFF` | `#393B40` | popovers, lignes survolées |
| `surfaceSunken` | `#F7F8FA` | `#1E1F22` | champs, blocs de code |
| `toolbar` | `#27282E` | `#2B2D30` | barre principale (sombre dans les deux thèmes) |
| `statusBar` | `#F7F8FA` | `#1E1F22` | barre de statut |
| `panelHeader` | `#F7F8FA` | `#2B2D30` | en-tête de tool window |
| `border` | `#EBECF0` | `#393B40` | séparateurs 1 px |
| `borderStrong` | `#C9CCD6` | `#4E5157` | bord de champ, focus |
| `text` | `#000000` | `#DFE1E5` | texte principal |
| `textSecondary` | `#6C707E` | `#9DA0A8` | métadonnées |
| `textMuted` | `#818594` | `#6F737A` | hints, labels |
| `primary` | `#3574F0` | `#3574F0` | action, focus, lien |
| `primaryHover` | `#4682FA` | `#467FF2` | hover |
| `primarySoft` | `#D4E2FF` | `#2E436E` | fond d'action secondaire |
| `selection` | `#D4E2FF` | `#2E436E` | ligne/onglet sélectionné |
| `hover` | `#EDF3FF` | `#393B40` | survol |

> Le bleu `#3574F0` est le **Blue6** de JetBrains. En light, le texte sur ce bleu est blanc ;
> en dark aussi. Ne pas utiliser `primary` comme fond de grande surface.

### 3.2 Sémantique

| Token | Light | Dark | Usage |
| --- | --- | --- | --- |
| `success` | `#208A3C` | `#5FAD65` | commit appliqué, review propre |
| `warning` | `#A46704` | `#BA9752` | avertissement, branche divergente |
| `danger` | `#DB3B4B` | `#DB5C5C` | bug, conflit, suppression |
| `info` | `#3369D6` | `#548AF7` | information, lien |
| `suggestion` | `#1F8039` | `#5FAD65` | suggestion applicable |
| `ai` | `#7444D4` | `#8150BE` | action/annotation IA |

Chaque famille a un `*Soft` pour les fonds de badge et les surfaces de message.

### 3.3 Diff

| Token | Light | Dark |
| --- | --- | --- |
| ajout fond | `#BEE6BE` | `#294436` |
| ajout gouttière | `#AADEAA` | `#447152` |
| suppression fond | `#D6D6D6` | `#484A4A` |
| suppression gouttière | `#C8C8C8` | `#656E76` |
| contexte | `#FFFFFF` | `#1E1F22` |
| numéro de ligne | `#999999` | `#606366` |
| gouttière éditeur | `#F0F0F0` | `#313335` |

Le préfixe `+` / `-` et les colonnes de numéros sont **toujours** affichés : la couleur
seule ne suffit pas (daltonisme, impression, captures).

### 3.4 Lanes du graphe

Huit couleurs, dans l'ordre :

| Light | Dark |
| --- | --- |
| `#3574F0` | `#3574F0` |
| `#208A3C` | `#5FAD65` |
| `#CE6117` | `#E08855` |
| `#7444D4` | `#8150BE` |
| `#DB3B4B` | `#DB5C5C` |
| `#058B90` | `#24A394` |
| `#DF9303` | `#D6AE58` |
| `#5A5D6B` | `#9DA0A8` |

La couleur est `lane % 8` ; la lane est stable sur toute la ligne. Ne pas réassigner une
couleur à une lane en cours de route.

## 4. Typographie

- **UI** : système sans (Inter si installé, sinon système). Pas de police décorative.
- **Code / diff / ids / numéros** : **JetBrains Mono** (embarqué dans
  `composeResources/font/`), le font de l'éditeur JetBrains.
- Poids : 400, 500, 600. Pas de 700+ dans le chrome, pas d'italique décoratif.
- Chiffres tabulaires pour les numéros de ligne et les compteurs.

| Rôle | Taille / interligne | Style |
| --- | --- | --- |
| `display` | 24 / 30 | SemiBold |
| `title` | 20 / 26 | SemiBold |
| `section` | 15 / 20 | SemiBold |
| `body` | 13 / 18 | Regular |
| `bodyStrong` | 13 / 18 | Medium |
| `small` | 12 / 16 | Regular |
| `micro` | 11 / 14 | Medium |
| `label` | 12 / 16 | Medium |
| `kicker` | 11 / 14 | SemiBold, +0.5 tracking, uppercase |
| `code` | 12 / 18 | JetBrains Mono Regular |
| `codeSmall` | 11 / 16 | JetBrains Mono Regular |

## 5. Espacement, tailles, rayons

Base **4 dp**. Échelle : 2, 4, 8, 12, 16, 20, 24, 32.

| Élément | Taille |
| --- | --- |
| contrôle (bouton, champ) | **28 dp** |
| contrôle large | 32 dp |
| ligne de liste / tree | **24 dp** |
| ligne de commit (2 lignes) | 40 dp |
| toolbar | **40 dp** |
| en-tête de tool window | **28 dp** |
| status bar | **22 dp** |
| sidebar | 240 dp |
| inspector | 320 dp |
| icône | 16 dp (20 dp toolbar) |

Rayons : `4` (kbd, micro-surface), `6` (champ/badge), `8` (bouton, carte, panneau),
`10` (popover). **Pas de pilules** sauf badge de statut (999).

## 6. Bordures, élévation, mouvement

- Bordure 1 px `border` partout ; `borderStrong` pour les champs et le focus.
- **Pas d'ombre** sur les cartes/panneaux. Ombre douce uniquement pour popover/tooltip.
- Toolbar et status bar sont des surfaces plates, sans dégradé.
- Mouvement : 100–150 ms, `ease-out` ; hover/press uniquement. Pas de rebond, pas de
  parallaxe. Respecter `prefers-reduced-motion` côté plateforme.

## 7. Iconographie

- Icônes **16 px**, trait 1,5–2 px, style JetBrains (chevrons, branch, commit, diff,
  check, warning, bug).
- Toujours accompagnées d'un texte ou d'un `contentDescription` ; jamais seules pour une
  action destructive.
- Pas d'emoji, pas de glyphes Unicode comme icônes.

## 8. Composants

Tous les composants sont dans `ui/components/` et utilisent uniquement les tokens.

### Button

- `NitpickButton` : `Primary`, `Secondary`, `Ghost`, `Danger`.
- Hauteur 28 dp (défaut), 32 dp large ; padding horizontal 12 dp ; rayon 8 dp.
- Primaire : fond `primary`, texte `onPrimary` (blanc). Danger : fond `dangerSoft`,
  texte `danger` — jamais un bloc rouge plein.
- Un seul bouton primaire par zone visible.

### Badge

- `NitpickBadge` : 11 sp, uppercase, rayon 4–6 dp, bord 1 px, padding 7×2.
- Tons : neutral, accent, success, warning, danger, info, suggestion, AI.
- Le libellé porte le sens (« BUG », « WARNING »), pas seulement la couleur.

### TextField

- `NitpickTextField` : 28 dp, fond `surface`, bord `borderStrong`, focus `primary`.
- Label 11 sp au-dessus, erreur 11 sp en `danger` en dessous.
- Placeholder `textMuted` ; pas de label flottant Material.

### Toolbar / tool window / status bar

- `NitpickToolbar` : 40 dp, `toolbar` sombre, actions 28 dp, texte 12 sp.
- `NitpickToolWindowHeader` : 28 dp, label uppercase 11 sp, actions à droite.
- `NitpickStatusBar` : 22 dp, texte 11 sp `onStatusBar`, séparé par des espaces.
- `NitpickTab` : 28 dp, onglet actif `surface` + rail bleu 2 px.

### ReviewCommentCard

- En-tête : badge de sévérité + `fichier:ligne` en mono + action `Dismiss`.
- Corps : message 13 sp.
- Suggestion : bloc `surfaceSunken`, label `SUGGESTED CHANGE`, code mono, bouton
  `Apply suggestion` (petit).
- La sévérité est aussi dans le texte du badge, pas seulement dans la couleur.

### DiffLineRow

- Deux colonnes de numéros (40 dp), gouttière `+`/`-`/espace (16 dp), code mono.
- Fond `diffAddedBg` / `diffRemovedBg` / `diffContextBg`.
- Une ligne = 20 dp minimum ; pas de wrap (scroll horizontal).

### RefBadge / Kbd / EmptyState

- `RefBadge` : préfixe `branch`, `remote` ou `tag` + nom ; tons accent/neutral/warning.
- `NitpickKbd` : mono 11 sp, fond `surfaceSunken`, bord `border`, rayon 4.
- `NitpickEmptyState` : titre 20 sp, détail 12 sp, action unique ; pas de « Coming soon ».

## 9. États

| État | Light | Dark |
| --- | --- | --- |
| hover | `#EDF3FF` | `#393B40` |
| sélection | `#D4E2FF` | `#2E436E` |
| focus | `#3574F0` (bord 1 px + halo 2 px) | idem |
| disabled | `textMuted` + alpha 0.55 | idem |
| erreur | `danger` + fond `dangerSoft` | idem |

Ne jamais utiliser un contour navigateur par défaut. Le focus clavier est toujours visible.

## 10. Accessibilité

- Contraste texte/fond ≥ 4,5:1 ; les couleurs de lane sont un renfort, pas la seule info.
- Cibles tactiles Android : 44–48 dp même si le contrôle visuel fait 28 dp (padding
  invisible).
- Navigation clavier complète sur desktop ; `Tab`/`Shift+Tab`, `Esc`, `Enter`.
- `contentDescription` sur chaque icône seule.
- Le diff reste lisible en noir et blanc : préfixes, numéros, libellés.
- Respecter la taille de police système ; ne pas bloquer le scaling.

## 11. Contenu et voix

- Public : développeurs. Ton direct, factuel, sans emphase.
- Anglais par défaut, français complet (strings dans `composeResources`).
- Verbes d'action : `Nitpick branch`, `Apply suggestion`, `Dismiss`, `Open repository`.
- Erreurs : ce qui a échoué, où, quoi faire. Pas de « Oops ».
- Pas d'emoji, pas de majuscules criardes hors kicker/badge.
- Les termes git restent en anglais : commit, branch, diff, rebase, push, nit.

## 12. Patterns d'écran

### IDE shell

```text
┌──────────────────────────────────────────────────────────────┐
│ Nitpick / repo        branche                 Open repository │  toolbar 40
├──────────────┬───────────────────────────────┬───────────────┤
│ BRANCHES     │ COMMITS                       │ DETAILS       │  headers 28
│ main         │ ●─┐ feat(graph): lanes        │ message       │
│ origin/main  │   ● fix: converge             │ author/date   │
│ v1.0         │ ●─┘ chore: tokens             │ id/parents    │
├──────────────┴───────────────────────────────┴───────────────┤
│ main   500 commits                                  Git      │  status 22
└──────────────────────────────────────────────────────────────┘
```

- Tool windows : 240 dp (gauche), 320 dp (droite), redimensionnables plus tard.
- Le graphe est dans la zone centrale ; le diff/review remplaceront les détails.
- Pas de cartes flottantes ni de coins très arrondis.

### Review

1. Liste des nits groupés par fichier ; badge de sévérité + ligne.
2. Carte de commentaire : message, suggestion, Apply/Dismiss.
3. Diff avec lignes colorées et commentaires ancrés.
4. Barre d'action : `Nitpick branch` (primaire), `Cancel` (secondaire).

## 13. Règles / anti-patterns

| À faire | À éviter |
| --- | --- |
| Utiliser `NitpickTheme.colors` | Couleur littérale dans un composant |
| 28 dp de contrôle, 24 dp de ligne | Composants Material 40–56 dp |
| Bordure 1 px + surface | Ombre portée sur une carte |
| Bleu uniquement pour l'action | Bleu en fond de grande zone |
| Badge + texte + gouttière | Couleur seule pour la sévérité |
| Mono pour le code/les ids | Mono pour le texte UI |
| Grille 4 dp | Marges arbitraires |
| Un seul primaire par écran | Trois boutons bleus côte à côte |

## 14. Fichiers

```text
shared/src/commonMain/kotlin/dev/nitpick/app/ui/
  theme/
    NitpickTokens.kt       couleurs, espacements, rayons, tailles, tons
    NitpickTypography.kt   échelle UI + code, mapping Material
    NitpickTheme.kt        thème Compose, light/dark, locals
  components/
    NitpickButton.kt       boutons
    NitpickSurface.kt      carte, badge, kbd, séparateurs
    NitpickTextField.kt    champ dense
    NitpickLayout.kt       section header, empty state
    NitpickIdeShell.kt     toolbar, tool window header, tab, status bar
    NitpickReview.kt       review card, diff line, ref badge
  designsystem/
    DesignSystemCatalog.kt catalogue visuel (preview)
```

## 15. Vérification

```bash
GRADLE_USER_HOME=/tmp/nitpick-gradle ANDROID_USER_HOME=/tmp/nitpick-android \
  ./gradlew :shared:compileKotlinJvm :desktopApp:compileKotlin
```

Ouvrir `DesignSystemCatalog` dans une preview IDE pour vérifier light/dark, densité,
diff et review. Toute nouvelle couleur doit être ajoutée aux tokens, jamais dans un
composant.
