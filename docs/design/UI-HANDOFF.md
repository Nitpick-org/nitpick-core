# UI handoff — Nitpick mockup → Compose

> Read alongside `docs/DESIGN-SYSTEM.md` (Eliot), **which is authoritative** for tokens, sizes
> and components. This document states what the mockup adds: the screens, their states,
> the copy, and the only token deviation (the green accent).
>
> - Interactive mockup: https://claude.ai/artifact/1PBvUhR38NmGVYzp6ajL9e
> - Mockup HTML source: `docs/design/mockups/` (`Main.dc.html` = desktop,
>   `Phone.dc.html` = Android, `Tokens.dc.html` = token sheet). The demo data and
>   exact colors live in the `renderVals()` block at the bottom of each file.
>   The HTML is a **visual reference**, not code to port: we rebuild it in Compose
>   with the components from `ui/components/`.

## 0. Implementation rules

1. No literal color in a component: everything goes through `NitpickTheme.colors`.
2. Reuse existing components before creating new ones (`NitpickButton`, `NitpickBadge`,
   `NitpickTextField`, `NitpickToolbar`, `NitpickToolWindowHeader`, `NitpickTab`,
   `NitpickStatusBar`, `ReviewCommentCard`, `DiffLineRow`, `RefBadge`, `NitpickKbd`,
   `NitpickEmptyState`, `NitpickCard`).
3. Every new component goes in `ui/components/` and in `DesignSystemCatalog.kt`.
4. All strings go in `composeResources/values/strings.xml` **and**
   `values-fr/strings.xml` (§6).
5. Desktop: IDE density (28 dp). Android: same colors, touch targets 44–48 dp.
6. Verification: the command from §15 of `DESIGN-SYSTEM.md`, plus the catalog in light and
   dark.

## 1. The only token deviation: the green accent

In `NitpickTokens.kt`, only the primary family changes. Everything else keeps Eliot's
expUI values.

| Token | Light (before → after) | Dark (before → after) |
| --- | --- | --- |
| `primary` | `#3574F0` → `#0F766E` | `#3574F0` → `#5EEAD4` |
| `onPrimary` | `#FFFFFF` (unchanged) | `#FFFFFF` → `#0B2E2A` (white is unreadable on the light teal) |
| `primaryHover` | `#4682FA` → `#115E59` | `#467FF2` → `#7FF0DE` |
| `primarySoft` | `#D4E2FF` → `#D2EFEA` | `#2E436E` → `#1F403C` |
| `selection` | `#D4E2FF` → `#D2EFEA` | `#2E436E` → `#1F403C` |
| `hover` | `#EDF3FF` → `#EDF7F5` | `#393B40` (unchanged) |
| `lanePalette[0]` | `#3574F0` → `#0F766E` | `#3574F0` → `#5EEAD4` |
| `lanePalette[5]` | `#058B90` → `#3574F0` | `#24A394` → `#548AF7` |

`lanePalette[5]`: cyan was too close to the teal, so it gets the former blue.
Update §3.1 and §3.4 of `DESIGN-SYSTEM.md` accordingly ("blue is
reserved…" becomes "teal is reserved…").

## 2. Severities: the mockup vs the domain

The domain has `Severity { INFO, SUGGESTION, WARNING, BUG }`. The mockup shows
BUG / WARNING / NIT / STYLE. **The code is authoritative**, we keep the enum and map:

| Mockup | `Severity` | `NitpickTone` | Badge |
| --- | --- | --- | --- |
| BUG | `BUG` | `Danger` | `BUG` |
| WARNING | `WARNING` | `Warning` | `WARNING` |
| NIT | `SUGGESTION` | `Suggestion` | `SUGGESTION` |
| STYLE | `INFO` | `Info` | `INFO` |

Sort order everywhere: BUG, WARNING, SUGGESTION, INFO, then line number.
Summary format: `"{total} nits · {bugs} bug"` (the bug segment only appears when there are some).

## 3. Desktop (1440×900)

### 3.1 Shell

```text
┌ Toolbar 40 : [icon] Nitpick / nitpick-app ▾  ⑂ feature/nit-sheet ▾      Fetch  Pull 2  Push 3 │ [Nitpick branch]  ⚙ ┐
├ BRANCHES (240) ─┬ COMMITS (flex) ─────────────────────┬ [Details|Diff|Review] (320, 680 if Diff/Review) ┤
│ filter 28       │ filter "Text or hash" + Branch/User/Date ▾ │                                       │
│ tree 24 dp      │ lines 40 dp : subject + refs / author · date · hash │                                 │
├ Status bar 22 : ⑂ feature/nit-sheet  214 commits  ↑3 ↓0          Anthropic · claude-sonnet-4-5  Git ┤
```

- **Toolbar** (`NitpickToolbar`): repo and branch selector on the left; on the right Fetch,
  Pull (incoming counter), Push (outgoing counter) as `NitpickToolbarAction`, separator,
  then the only primary button **Nitpick branch** (tweezers icon), then Settings (icon).
  On hovering Nitpick branch: tooltip `surfaceRaised`, radius 10, "Review this branch
  against main" + `NitpickKbd` ⌘⇧R.
- **Branches**: `NitpickToolWindowHeader("BRANCHES")` + add button, filter field, tree:
  `Local` / `Remote branches` / `Tags`, expandable nodes, current branch in `selection`
  with ↑ ahead on the right.
- **Commits**: `COMMITS` header + "214 commits" on the right; filter bar; the existing graph
  (`CommitGraph.kt`) in 40 dp rows over two lines (subject + `RefBadge`s;
  then `small`: author · relative date · mono hash). Selected commit in `selection`.
  HEAD dot = ring; merge = hollow circle.
- **Inspector**: `NitpickTab`s Details / Diff / Review. When the review is not open,
  the Review tab carries a `1 BUG` badge. Width 320 dp on Details, **680 dp** on Diff and
  Review (the center shrinks; this is a mockup choice, to be validated).

### 3.2 Details tab

Commit title (`section`), body (`body`, `textSecondary`), Author / Date / Commit
/ Parents grid (clickable parent in `primary`), `RefBadge`s, separator, `4 FILES` (kicker) with
`+169 −15` on the right, file list (24 dp rows, `+n` green, `−n` red).

### 3.3 Diff tab

40 dp row: `feature/nit-sheet vs [Base: main ▾]` (selector, default `main`);
"3 commits · 6 files" and `+214 −37` on the right. File list (24 dp, selected file in
`selection`). Then file header (28 dp, mono path), hunk header
(`surfaceSunken`, `codeSmall`), and `DiffLineRow`s.

### 3.4 Review tab (result)

1. **Summary card** (`NitpickCard`): `AI` badge (tone `Ai`), "8 nits · 1 bug" (`section`),
   meta "vs main · 6 files · claude-sonnet-4-5 · 34 s", ghost Re-run button; verdict
   paragraph; a row of count badges per severity ("1 BUG", "2 WARNINGS"…).
2. **One block per file**, files sorted by worst severity: 32 dp header (mono path,
   count badges, ghost **Apply all for this file** button on the right), then the hunks.
3. **Nits anchored under their line** (`ReviewCommentCard`, indented by 56 dp to keep the
   gutters visible): severity badge + mono `fichier:ligne` + **Dismiss**; message;
   `SUGGESTED CHANGE` block (kicker in the `suggestion` color) with a `-` line before
   (`diffRemovedBg`) and a `+` line after (`diffAddedBg`); small primary
   **Apply suggestion** button.
   → `ReviewCommentCard` already has `suggestion`; the **before** text must be added to
   show the `-` / `+` pair.
4. **Applied nit**: the card collapses into a 28 dp row, `suggestionSoft` background,
   `suggestion` border at 32 %, ✓ "Applied" + title + Undo.
5. **Commit bar** (new component, pinned to the bottom of the inspector, `panelHeader`
   background, top border): "1 suggestion applied · NitAnchor.kt", prefilled
   `NitpickTextField` commit message, Secondary **Later** button, Primary **Stage & commit** button.
   It appears as soon as a suggestion is applied.

### 3.5 Settings

Dialog 860×560, radius 10, `surface`. Left 220 dp (`canvas`): Search field + sections
General (selected), Review, Git, Keymap. Right:

- Kicker `APPEARANCE` → **Theme**: System / Light / Dark segmented control (28 dp,
  `surfaceSunken` container + border, active segment `surface` + border).
- Kicker `AI REVIEW` → **LLM provider**: 4 horizontal radios Anthropic / OpenAI / Google /
  Ollama (active radio filled `primary`, dot `onPrimary`); **Model**: 300 dp
  dropdown, mono value; **API key**: 380 dp password field + eye button, 11 sp help
  "Stored in the system keychain. Sent only to Anthropic."
- Footer: Cancel (Secondary), Save (Primary).

`SettingsScreen.kt` already exists with these fields: apply this layout on the desktop side.

### 3.6 States

| State | Where | Content |
| --- | --- | --- |
| No repository | the whole window (toolbar + canvas + status bar "no branch") | `NitpickEmptyState`: folder icon, "No repository open", "Pick a local git repository to see its commits and branches.", a single action **Open repository…** + `NitpickKbd` ⌘O. No Fetch/Pull/Push and no CTA in the toolbar, just "Open repository…". |
| Review in progress | Review tab + toolbar + status bar | Card: AI badge, "Nitpicking feature/nit-sheet vs main", Cancel; 4 dp progress bar; 4 steps: ✓ "Collecting diff against main" ("6 files · +214 −37"), spinner "Reading 12 files…" ("7 / 12"), ○ "Asking the model…", ○ "Placing nits on lines". Below, 3 card skeletons. The CTA becomes "Nitpicking…" with a spinner. The status bar shows "Reading 12 files…" + a mini bar. |
| Clean branch | Review tab | ✓ `success` icon, "Clean branch. Nothing to nitpick.", "Reviewed 6 files on feature/nit-sheet against main. No bugs, no warnings, no nits.", action **Push 3 commits**. |
| Missing API key | Review tab | Banner tone Warning (`WARNING` badge + "No API key for Anthropic."); empty state: key icon, "Add an API key to start", "Nitpick needs an Anthropic API key to ask the model. Your code stays on this machine until you run a review.", action **Open Settings**. |
| Network | Review tab | Banner tone Danger (`ERROR` + "Review failed: network error."); "Could not reach Anthropic", "The review request failed before reaching the provider. Check your connection, then retry. Nothing was sent.", code `ENOTFOUND api.anthropic.com` in a `NitpickKbd`-like style, action **Retry**. |
| LLM timeout | Review tab | Danger banner ("Review failed: request timed out."); "The model took too long", "claude-sonnet-4-5 did not answer within 90 s. Large diffs can take longer: try again.", action **Try again**. |

One action per state. The provider and model names come from settings.

## 4. Android (393×852)

Top bar 56 dp in `toolbar` color (dark in both themes), `canvas` background,
`surface` cards + 1 px border (no shadow), 48–56 dp buttons with radius 8, 48 dp fields with
radius 6 and a 12 sp label above (no floating label).

1. **Repos**: bar (app icon, "Repositories", Settings); search field; one card
   per repo (name, mono branch, "reviewed 2 h ago", `1 BUG` / `CLEAN` / `NEW` badge);
   primary extended FAB 56 dp "Clone repo" at the bottom right.
   - **Clone repo** (full screen, cross): help text; HTTPS URL, Access token
     (password + eye; help "Needs read access. Push needs write access."), Folder name;
     full-width 56 dp **Clone** button at the bottom.
2. **Graph**: bar (back, repo name, Settings, menu); branch card: `feature/nit-sheet ▾` selector
   + "↑3 ↓0"; 3 Secondary 44 dp buttons Fetch / Pull 2 / Push 3;
   compact graph (60 dp rows, subject + "author · date · hash"); primary extended FAB
   60 dp **Nitpick branch** at the bottom right.
3. **Review**:
   - **Files**: bar (back, "Review", subtitle "feature/nit-sheet vs main",
     Re-run); summary card (AI badge, "8 nits · 1 bug", verdict, counters);
     "Files with nits · 4" list (72 dp row: name, folder, badges, chevron); "2 more files
     reviewed, no nits."
   - **Diff**: bar (back, file name, "review/ · +18 −3"); "2 nits ·
     1 applied" row + Secondary 44 dp **Apply all for this file** button; mono diff 12 sp, a single
     line-number column; nits as compact 56 dp rows (badge + title + chevron), tapping
     opens the bottom sheet; applied nit = green "Applied" row; snackbar
     "1 suggestion applied" + **Stage & commit** action.
   - **Bottom sheet** (scrim + sheet with 12 radius at the top): handle; badge + "NitAnchor.kt ·
     line 23" + "1 of 8"; title; message; `SUGGESTED CHANGE` block (`-` / `+`); two
     56 dp buttons side by side: **Dismiss** (Secondary) and **Apply suggestion** (Primary).
4. **Settings**: Theme (48 dp segmented), LLM provider (radio list, 52 dp per row, with
   subtitle; Ollama: "Self-hosted, needs a server URL"), Model, API key ("Stored in
   Android Keystore.").

**Android states**: same texts as the desktop, centered, a single action as a full-width
56 dp button at the bottom (Clone repo, Cancel, Push 3 commits, Open Settings, Retry, Try again).
No repository: "No repository open" / "Clone a git repository over HTTPS to see its commits
and branches."

## 5. New components to create

| Component | Role | Notes |
| --- | --- | --- |
| `NitpickSegmentedControl` | Theme | 28 dp desktop, 48 dp Android |
| `NitpickRadioGroup` | LLM provider | horizontal on desktop, list on Android |
| `ReviewSummaryCard` | summary + counters | `Ai` badge, sorted counters |
| `ReviewFileSection` | file block + Apply all | contains `DiffLineRow` and `ReviewCommentCard` |
| `AppliedNitRow` | collapsed nit | `Suggestion` tone |
| `CommitComposerBar` | after Apply | field + Later + Stage & commit |
| `ReviewProgress` | loading state | 4 steps, bar, skeletons |
| `NitpickBanner` | review errors | Warning or Danger tone, badge + text |
| `NitpickTooltip` | toolbar | text + `NitpickKbd` |
| `NitBottomSheet` | Android | `ModalBottomSheet` styled with the tokens |

## 6. Strings to add (EN / FR)

| Key | EN | FR |
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

The long state texts (message bodies) are in §3.6; translate them in the same
spirit, short sentences. Keep git terms in English (commit, branch, diff, push, nit).

## 7. Proposed work order

1. Tokens (§1) + update of `DESIGN-SYSTEM.md` + light/dark catalog.
2. Desktop shell: toolbar with CTA, tool windows, status bar, tabbed inspector.
3. Diff tab with base selector.
4. Review tab: summary, file sections, inline nits, Apply / Dismiss, applied nit,
   commit bar.
5. States (empty, loading, clean, 3 errors).
6. Desktop settings.
7. Android: Repos + Clone, Graph + FAB, Review (files, diff, bottom sheet), Settings,
   states.

Each step: JVM + desktop compile, light/dark screenshots compared against the mockup.
