# Ticket 44 (part) — Android dogfooding captures

- Status: done (screenshots), the rest of #44 stays with H1/Gabriel
- Human owner: @pepito2t (H3) for this part
- Track: C android
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Allowed files: `docs/demo/android-*.png`, `docs/DEMO.md` (screenshots line + Android checklist)

## Done

- Full Android walkthrough on the emulator, driven by adb, GitHub account connected through the device
  flow (#64): clone of `pepito2t/OpenTab` → checkout `perf/thumbnail-lookup` (demo branch
  pushed on purpose: lock not released on cache miss, eviction `>=`) → review (the 2 bugs
  found, correct suggestions) → Apply ×2 → Stage & commit → Push. Commit `cb55041` on
  GitHub, diff verified: `defer` restored, `>` restored.
- 8 screenshots at 50 % in `docs/demo/android-*.png`, referenced in `docs/DEMO.md`.

## Observed during dogfooding (ideas, not fixed)

- After the push, the Review screen shows "Run a review to see nits" without saying "pushed"; the
  only confirmation is ↑0 on the Graph branch card.
- The comments added by the faulty branch ("avoids the defer closure", "Evicts
  before the cache reaches capacity") stay wrong after the Apply: the suggestion only touches
  the code.
- The default commit message "Apply review suggestions" was left as is.

## Log

- 00:40 — emulator, LLM key copied from the desktop settings, demo branch pushed.
- 00:46 — full loop, push verified on GitHub.
