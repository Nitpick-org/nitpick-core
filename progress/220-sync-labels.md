# Ticket 220-sync-labels

- Status: in progress
- Human owner: @
- Track: <A core | B git+desktop | C android+polish>
- Agent: @implementer
- Model: <haiku|sonnet|opus>
- Escalations:
- Allowed files: shared/src/<sourceSet>/<...>

## Log

- 03:28 — worktree created.


- 04:20 — H3 request: Fetch / Pull / Push changed labels mid-action and shifted the bar. Fixed labels on desktop and phone; "Share review" too (`SyncIcon` pointing up while sending). The `*_in_progress` strings remain for the Clone dialog and the phone's bottom button, which shift nothing.
- 04:25 — the click bounce also applies to icons passed through `leading` (`Box(pop.modifier)`), so to the sync icons. More visible sync motion: the arrow travels 6 dp while fading and restarts, instead of a 3 dp back-and-forth.
- 04:50 — replayed on desktop with the test repo served by `git daemon` on 127.0.0.1: Fetch (spinning icon, fixed label), Push (1 commit reached the bare repo, "Push 1" → "Push"), Fetch then Pull ("Pull 1", the clone advances one commit, 7 commits shown), Settings (open, Cancel). The first attempt with `origin` on `10.0.2.2` (emulator alias) timed out: environment error, not the app's.
- Two clean app exits (code 0, no trace) during these tests, not reproduced once the window was activated before each click; `main` does not show them either. Noted, not attributed.
