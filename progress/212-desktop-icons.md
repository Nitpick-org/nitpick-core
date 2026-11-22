# Ticket 212-desktop-icons

- Status: in progress
- Human owner: @pepito2t (H3)
- Track: C desktop polish
- Agent: Claude Code, human session
- Model: Fable 5.1
- Allowed files: ui/components/{NitpickButton,NitpickIdeShell,NitpickLayout}.kt, ui/repository/{RepositoryScreen,ReviewPanel,DiffPanel,ShareReview}.kt, drawable/ic_share.xml

## Log

- 03:20 — `icon: DrawableResource?` on `NitpickButton`, `NitpickToolbarAction` and the action of `NitpickEmptyState`: the icon takes the button's text colour, so one line per call site instead of a `leading` lambda with the tint done by hand.
- 03:25 — toolbar: folder (Open), cloud (Clone), fetch, pull, push; empty screen: two buttons "Open repository…" (primary) and "Clone repository…" (secondary); panels: checkmark on Stage & commit and Apply all, share on Share review, circular arrows on Retry and Refresh.
- 03:35 — verified on screen on desktop (empty screen, toolbar, review card with "Share review"). Stage & commit and Apply all not seen: an applicable suggestion is needed; same code as the others.

## Decisions

- No new glyph for Retry/Refresh: `ic_fetch` is already a double circular arrow.
- The existing `leading` ones (gear, tweezers, chevron) stay: no refactor beyond the ticket.

- 03:50 — H3 request mid-way: "Open…" / "Clone…" in the toolbar (`toolbar_open`, `toolbar_clone`, the long labels stay on the empty screen); `rememberIconPop()` in `NitpickIcon.kt`: on click the icon contracts then springs back, wired into `NitpickButton` and `NitpickToolbarAction`. Seen on screen: Fetch clicked, no crash; the bounce itself cannot be captured in a screenshot.
