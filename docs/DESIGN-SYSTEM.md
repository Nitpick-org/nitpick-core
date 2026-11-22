# Design system — Nitpick

> **Art direction: JetBrains IDE / New UI (expUI).** Nitpick is a developer tool, not a
> marketing site. The interface must look like the IDE hosting the challenge: graphite,
> dense, a single action color, code at the center, color only when it carries meaning.
>
> The document structure (tokens → components → usages) is inspired by Oria, but **no Oria
> token or component is reused**: the values come from the IntelliJ IDEA expUI themes
> (`themes/expUI/expUI_light.theme.json` and `expUI_dark.theme.json`) and from the editor
> scheme (`DefaultColorSchemesManager.xml`).

## 1. Principles

1. **Code is the hero.** Chrome (toolbar, tool windows, status bar) is dark and calm; the
   diff, commits and nits get the spotlight.
2. **IDE density.** 28 dp controls, 24 dp lines, 40 dp toolbar, 22 dp status bar. No
   Material 56 dp padding.
3. **A single saturated action.** The teal `#0F766E` (light) / `#5EEAD4` (dark) is reserved
   for primary actions, focus and active selection. The rest is graphite.
4. **Color is semantic.** Green = addition/success, red = removal/error, yellow =
   warning, purple = AI. Never decorative color.
5. **Two platforms, one language.** The tokens live in `commonMain`. Desktop keeps the IDE
   density; Android enlarges touch targets to 44–48 dp without changing colors.
6. **Review before push.** A nit is readable without color: text badge, `+/-` gutter,
   line number, icon. Color reinforces, it does not replace.

## 2. Sources of truth

| Source | Usage |
| --- | --- |
| `shared/src/commonMain/kotlin/dev/nitpick/app/ui/theme/NitpickTokens.kt` | colors, spacing, radii, sizes |
| `ui/theme/NitpickTypography.kt` | UI + code scale |
| `ui/theme/NitpickTheme.kt` | Compose theme, light/dark, Material 3 mapping |
| `ui/components/` | public components |
| `ui/designsystem/DesignSystemCatalog.kt` | visual catalog (IDE preview) |
| IntelliJ themes `expUI_light/dark` | reference values |
| `DefaultColorSchemesManager.xml` | editor, diff, line number colors |

## 3. Colors

### 3.1 Base (light / dark)

| Token | Light | Dark | Usage |
| --- | --- | --- | --- |
| `canvas` | `#F7F8FA` | `#1E1F22` | app background, empty areas |
| `surface` | `#FFFFFF` | `#2B2D30` | panels, tool windows |
| `surfaceRaised` | `#FFFFFF` | `#393B40` | popovers, hovered rows |
| `surfaceSunken` | `#F7F8FA` | `#1E1F22` | fields, code blocks |
| `toolbar` | `#27282E` | `#2B2D30` | main bar (dark in both themes) |
| `statusBar` | `#F7F8FA` | `#1E1F22` | status bar |
| `panelHeader` | `#F7F8FA` | `#2B2D30` | tool window header |
| `border` | `#EBECF0` | `#393B40` | 1 px separators |
| `borderStrong` | `#C9CCD6` | `#4E5157` | field border, focus |
| `text` | `#000000` | `#DFE1E5` | main text |
| `textSecondary` | `#6C707E` | `#9DA0A8` | metadata |
| `textMuted` | `#818594` | `#6F737A` | hints, labels |
| `primary` | `#0F766E` | `#5EEAD4` | action, focus, link |
| `onPrimary` | `#FFFFFF` | `#0B2E2A` | text on `primary` |
| `primaryHover` | `#115E59` | `#7FF0DE` | hover |
| `primarySoft` | `#D2EFEA` | `#1F403C` | secondary action background |
| `selection` | `#D2EFEA` | `#1F403C` | selected row/tab |
| `hover` | `#EDF7F5` | `#393B40` | hover |

> The teal replaces JetBrains **Blue6** `#3574F0`, which moves to lane 5 of the graph. In
> light, the text on teal is white; in dark, the light teal requires dark text `#0B2E2A`.
> Do not use `primary` as the background of a large surface.

### 3.2 Semantic

| Token | Light | Dark | Usage |
| --- | --- | --- | --- |
| `success` | `#208A3C` | `#5FAD65` | applied commit, clean review |
| `warning` | `#A46704` | `#BA9752` | warning, diverging branch |
| `danger` | `#DB3B4B` | `#DB5C5C` | bug, conflict, deletion |
| `info` | `#3369D6` | `#548AF7` | information, link |
| `suggestion` | `#1F8039` | `#5FAD65` | applicable suggestion |
| `ai` | `#7444D4` | `#8150BE` | AI action/annotation |

Each family has a `*Soft` for badge backgrounds and message surfaces.

### 3.3 Diff

| Token | Light | Dark |
| --- | --- | --- |
| added background | `#BEE6BE` | `#294436` |
| added gutter | `#AADEAA` | `#447152` |
| removed background | `#D6D6D6` | `#484A4A` |
| removed gutter | `#C8C8C8` | `#656E76` |
| context | `#FFFFFF` | `#1E1F22` |
| line number | `#999999` | `#606366` |
| editor gutter | `#F0F0F0` | `#313335` |

The `+` / `-` prefix and the number columns are **always** displayed: color
alone is not enough (color blindness, printing, screenshots).

### 3.4 Graph lanes

Eight colors, in order:

| Light | Dark |
| --- | --- |
| `#0F766E` (= `primary`) | `#5EEAD4` |
| `#208A3C` | `#5FAD65` |
| `#CE6117` | `#E08855` |
| `#7444D4` | `#8150BE` |
| `#DB3B4B` | `#DB5C5C` |
| `#3574F0` (ex-primary) | `#548AF7` |
| `#DF9303` | `#D6AE58` |
| `#5A5D6B` | `#9DA0A8` |

The color is `lane % 8`; the lane is stable across the whole line. Do not reassign a
color to a lane mid-way.

## 4. Typography

- **UI**: system sans (Inter if installed, otherwise the system font). No decorative font.
- **Code / diff / ids / numbers**: **JetBrains Mono** (bundled in
  `composeResources/font/`), the JetBrains editor font.
- Weights: 400, 500, 600. No 700+ in the chrome, no decorative italics.
- Tabular figures for line numbers and counters.

| Role | Size / leading | Style |
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

## 5. Spacing, sizes, radii

**4 dp** base. Scale: 2, 4, 8, 12, 16, 20, 24, 32.

| Element | Size |
| --- | --- |
| control (button, field) | **28 dp** |
| wide control | 32 dp |
| list / tree row | **24 dp** |
| commit row (2 lines) | 40 dp |
| toolbar | **40 dp** |
| tool window header | **28 dp** |
| status bar | **22 dp** |
| sidebar | 240 dp |
| inspector | 320 dp |
| icon | 16 dp (20 dp toolbar) |

Radii: `4` (kbd, micro-surface), `6` (field/badge), `8` (button, card, panel),
`10` (popover). **No pills** except the status badge (999).

## 6. Borders, elevation, motion

- 1 px `border` everywhere; `borderStrong` for fields and focus.
- **No shadow** on cards/panels. Soft shadow only for popover/tooltip.
- Toolbar and status bar are flat surfaces, with no gradient.
- Motion: 100–150 ms, `ease-out`; hover/press only. No bounce, no
  parallax. Respect `prefers-reduced-motion` on the platform side.

## 7. Iconography

- **16 px** icons, 1.5–2 px stroke, JetBrains style (chevrons, branch, commit, diff,
  check, warning, bug).
- Always accompanied by text or a `contentDescription`; never alone for a
  destructive action.
- No emoji, no Unicode glyphs as icons.

## 8. Components

All components live in `ui/components/` and use only the tokens.

### Button

- `NitpickButton`: `Primary`, `Secondary`, `Ghost`, `Danger`.
- 28 dp height (default), 32 dp wide; 12 dp horizontal padding; 8 dp radius.
- Primary: `primary` background, `onPrimary` text (white). Danger: `dangerSoft` background,
  `danger` text — never a solid red block.
- One primary button per visible zone.

### Badge

- `NitpickBadge`: 11 sp, uppercase, 4–6 dp radius, 1 px border, 7×2 padding.
- Tones: neutral, accent, success, warning, danger, info, suggestion, AI.
- The label carries the meaning ("BUG", "WARNING"), not just the color.

### TextField

- `NitpickTextField`: 28 dp, `surface` background, `borderStrong` border, `primary` focus.
- 11 sp label above, 11 sp error in `danger` below.
- `textMuted` placeholder; no Material floating label.

### Toolbar / tool window / status bar

- `NitpickToolbar`: 40 dp, dark `toolbar`, 28 dp actions, 12 sp text.
- `NitpickToolWindowHeader`: 28 dp, 11 sp uppercase label, actions on the right.
- `NitpickStatusBar`: 22 dp, 11 sp `onStatusBar` text, separated by spaces.
- `NitpickTab`: 28 dp, active tab `surface` + 2 px blue rail.

### ReviewCommentCard

- Header: severity badge + `fichier:ligne` in mono + `Dismiss` action.
- Body: 13 sp message.
- Suggestion: `surfaceSunken` block, `SUGGESTED CHANGE` label, mono code,
  `Apply suggestion` button (small).
- Severity is also in the badge text, not only in the color.

### DiffLineRow

- Two number columns (40 dp), `+`/`-`/space gutter (16 dp), mono code.
- `diffAddedBg` / `diffRemovedBg` / `diffContextBg` background.
- One line = 20 dp minimum; no wrap (horizontal scroll).

### RefBadge / Kbd / EmptyState

- `RefBadge`: `branch`, `remote` or `tag` prefix + name; accent/neutral/warning tones.
- `NitpickKbd`: 11 sp mono, `surfaceSunken` background, `border` border, 4 radius.
- `NitpickEmptyState`: 20 sp title, 12 sp detail, single action; no "Coming soon".

## 9. States

| State | Light | Dark |
| --- | --- | --- |
| hover | `#EDF3FF` | `#393B40` |
| selection | `#D4E2FF` | `#2E436E` |
| focus | `primary` (1 px border + 2 px halo) | same |
| disabled | `textMuted` + alpha 0.55 | same |
| error | `danger` + `dangerSoft` background | same |

Never use a default browser outline. Keyboard focus is always visible.

## 10. Accessibility

- Text/background contrast ≥ 4.5:1; lane colors are a reinforcement, not the only info.
- Android touch targets: 44–48 dp even if the visual control is 28 dp (invisible
  padding).
- Full keyboard navigation on desktop; `Tab`/`Shift+Tab`, `Esc`, `Enter`.
- `contentDescription` on every standalone icon.
- The diff stays readable in black and white: prefixes, numbers, labels.
- Respect the system font size; do not block scaling.

## 11. Content and voice

- Audience: developers. Direct, factual tone, without emphasis.
- English by default, complete French (strings in `composeResources`).
- Action verbs: `Nitpick branch`, `Apply suggestion`, `Dismiss`, `Open repository`.
- Errors: what failed, where, what to do. No "Oops".
- No emoji, no shouty capitals outside kicker/badge.
- Git terms stay in English: commit, branch, diff, rebase, push, nit.

## 12. Screen patterns

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

- Tool windows: 240 dp (left), 320 dp (right), resizable later.
- The graph is in the central area; diff/review will replace the details.
- No floating cards or very rounded corners.

### Review

1. List of nits grouped by file; severity badge + line.
2. Comment card: message, suggestion, Apply/Dismiss.
3. Diff with colored lines and anchored comments.
4. Action bar: `Nitpick branch` (primary), `Cancel` (secondary).

## 13. Rules / anti-patterns

| Do | Avoid |
| --- | --- |
| Use `NitpickTheme.colors` | Literal color in a component |
| 28 dp control, 24 dp row | Material 40–56 dp components |
| 1 px border + surface | Drop shadow on a card |
| Blue only for the action | Blue as the background of a large area |
| Badge + text + gutter | Color alone for severity |
| Mono for code/ids | Mono for UI text |
| 4 dp grid | Arbitrary margins |
| One primary per screen | Three blue buttons side by side |

## 14. Files

```text
shared/src/commonMain/kotlin/dev/nitpick/app/ui/
  theme/
    NitpickTokens.kt       colours, spacing, radii, sizes, tones
    NitpickTypography.kt   UI + code scale, Material mapping
    NitpickTheme.kt        Compose theme, light/dark, locales
  components/
    NitpickButton.kt       buttons
    NitpickSurface.kt      card, badge, kbd, separators
    NitpickTextField.kt    dense field
    NitpickLayout.kt       section header, empty state
    NitpickIdeShell.kt     toolbar, tool window header, tab, status bar
    NitpickReview.kt       review card, diff line, ref badge
  designsystem/
    DesignSystemCatalog.kt catalogue visuel (preview)
```

## 15. Verification

```bash
GRADLE_USER_HOME=/tmp/nitpick-gradle ANDROID_USER_HOME=/tmp/nitpick-android \
  ./gradlew :shared:compileKotlinJvm :desktopApp:compileKotlin
```

Open `DesignSystemCatalog` in an IDE preview to check light/dark, density,
diff and review. Any new color must be added to the tokens, never in a
component.
