# Audit lane: UX, i18n, accessibility (#130)

- Pin: `origin/main` = `54c37a2` (22/11 01:10), read in a detached worktree.
- Method: one independent read-only reviewer against `docs/design/UI-HANDOFF.md`, `docs/DESIGN-SYSTEM.md`, `docs/CONTEXT.md` §12, the `docs/demo/*.png` captures and the `ui/` sources; every P1/P2 then re-read by H3 at the cited lines.
- Result: 0 P0, 4 P1, 5 P2. P2 polish belongs to #122 (already merged on `main` after the pin as #133, see coverage gaps).

## P1

| # | Finding | Evidence | Viewer vs handoff | Fix | Cost | Conf. | Impact |
| --- | --- | --- | --- | --- | --- | --- | --- |
| P1-1 | Nits are never inline in the diff, on either platform | `ui/repository/ReviewPanel.kt:142-157` (flat `LazyColumn` of cards), `ui/phone/PhoneDiffScreen.kt:18-26` (hunks without nits), `docs/demo/dogfooding-104-review.png` | Handoff §3.4.2-3 wants a block per file with each nit under its line; §4.3 wants nit rows inside the phone diff; CONTEXT §13 step 3 says « les nits apparaissent inline ». The viewer sees a list beside the graph. Already listed as a known blocker in #125. | Group comments by file in `ReviewPanel`, render that file's `FileHunks` and insert the card after the row whose new line number equals `startLine`; line-0 nits at the top of the block. Or reword the pitch to « anchored to file:line ». | >1h | high | pitch |
| P1-2 | A suggestion shows only the « after » text, no before/after pair | `ui/components/NitpickReview.kt:78-104` (one `Text` for `suggestion`), `docs/demo/android-05-nit-suggestion.png` | CONTEXT §12 requires « bloc de diff avant / après »; handoff §3.4.3 wants a `-` line on `diffRemovedBg` and a `+` line on `diffAddedBg`. The original snippet already exists in `ReviewComment.originalSnippet`. | Add `before: String?` to `ReviewCommentCard`, render `-`/`+` lines with the diff colours; pass the snippet from `ReviewPanel` and `NitBottomSheet`. | 1h | high | score, demo step 4 |
| P1-3 | The review summary has no per-severity counts and its badge is in the Info tone | `ui/repository/ReviewPanel.kt:119-129`, `docs/demo/android-04-review-two-bugs.png` | Handoff §2 wants « {total} nits · {bugs} bug » and §3.4.1 the Ai badge plus a row of severity counters. The « a real bug was found » beat of demo step 3 is not visible at a glance. | A `Row` of `NitpickBadge(label + count)` sorted BUG > WARNING > SUGGESTION > INFO; badge tone `Ai`. | 15m | high | demo |
| P1-4 | The captures do not cover the desktop demo path, shared vs platform, or the agentic process | `docs/demo/`: 10 Android captures, 2 branch cards, 1 desktop dogfooding shot (dark, list only). No desktop apply → commit → share, no light desktop, no Air/Junie capture, no `count-lines.sh` image | CONTEXT §13 steps 6-7 need a full-screen image or the live `scripts/count-lines.sh` output, and 2-3 Air captures with the multi-agent comparison. Plan B video and submission have no evidence for those claims. | Capture the desktop path light and dark, one `count-lines.sh` output, 2-3 Air screenshots into `docs/demo/`. | 1h | high | pitch, submission |

## P2

| # | Finding | Evidence | Fix | Cost | Conf. |
| --- | --- | --- | --- | --- | --- |
| P2-1 | Raw exception text reaches the UI in English, even in FR | `RepositoryViewModel.kt:316, 374, 444` (`actionError = e.message`), `ReviewPanel.kt:215-219` (`ReviewFailure.Other` shows the message); sources `ReviewResponseParser.kt:41-43`, `ReviewAnchoring.kt:16-19` | Map to a localised generic string, keep the detail in the log | 1h | high |
| P2-2 | Muted text and line numbers are below WCAG AA | `theme/NitpickTokens.kt:149` light `textMuted` #818594 on white = 3.67:1; `:208` dark #6F737A on #2B2D30 = 2.9:1; line numbers 2.85:1 / 2.73:1. Used for file:line in `NitpickReview.kt:63` and the phone « line 83 ». Faithful to the IntelliJ expUI palette, so defensible. | Use `textSecondary` for file:line | 15m | high |
| P2-3 | Phone controls at 28-36 dp | `NitpickIdeShell.kt:55` (`sizes.control` = 28 dp) used for phone Back / Re-run / Settings / Shared (`PhoneFlow.kt:182,198`, `PhoneReviewScreen.kt:95`, `PhoneGraphScreen.kt:87`); `ShareReview.kt:73-78` Refresh is Small; `ConnectScreen.kt:174` show/hide has 4 dp padding. Handoff §0.5 says 44-48 dp. Compose may expand the hit area to 48 dp; not measured here. #133 (after the pin) reworked the phone toolbar with icons. | `Modifier.heightIn(min = 48.dp)` on the phone | 15m | low |
| P2-4 | Desktop never shows `PushedBanner` after apply-all → commit → push | Commit with every nit applied sets `review = null` (`RepositoryViewModel.kt:350-357`); the `review == null` branch of `ReviewPanel.kt:87-95` has no banner, only the else branch at `:168`. The phone shows it (`PhoneReviewScreen.kt:227`, `android-10-pushed-banner.png`). The desktop status line still says pushed (`SyncStatus.kt:21`). | Render `state.lastPushedBranch?.let { PushedBanner(it) }` in the `review == null` branch too | 15m | high |
| P2-5 | Desktop commit bar lacks the « Later » button and the « N suggestions applied · file » label | `ReviewPanel.kt:259-280`; `commit_later` unused (also `details_title`, `publish_in_progress`) | Add the applied-count label (`commit_bar_applied` plural exists) | 15m | high |

## Checked, no finding

- EN/FR: 183 keys each, identical name sets, plurals included; FR « one » form covers 0 and 1.
- Severity is never colour-only: a text badge is always present and localised (`NitpickReview.kt:59, 183-188`).
- `DiffLineRow` keeps `+`/`-` gutters and line numbers, so meaning does not rely on colour.
- No hardcoded user-facing strings outside the design-system catalog (only « Nitpick », repo and model names, glyphs, hunk headers).
- Dates: `CommitTimeFormat` uses `ofLocalizedDateTime` with the system zone.
- `contentDescription = null` only on decorative icons beside a text label (`RepositoryScreen.kt:328, 355`, `PushedBanner.kt:52`, `NitpickLayout.kt:78`); no icon-only button at the pin.
- Review states exist: loading, missing key, network, timeout, other, clean, empty, no diff (`ReviewPanel.kt:80-100`); phone flow captured end to end (`android-01` to `android-10`).
- Shared reviews list on desktop (empty review state) and on phone.
- `scripts/count-lines.sh` exists for the live shared-vs-platform beat.

## Coverage gaps

- App not run for this lane: light vs dark compared on captures only (dark desktop, light Android).
- `docs/design/mockups/*.html` and the Settings dialog layout (§3.5) not compared.
- Keyboard shortcuts ⌘⇧R and ⌘O (CONTEXT §12) not checked.
- Desktop empty state and Clone dialog not re-checked here (covered by the 21/11 QA pass).
- Touch-target hit expansion not measured on device.
- The pin predates #133 (phone UX pass): P2-3 may already be resolved on `main`.
