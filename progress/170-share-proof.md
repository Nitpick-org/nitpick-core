# Ticket 170-share-proof

- Status: ready for PR
- Human owner: @pepito2t (H3)
- Track: D sharing
- Agent: Claude Code, human session
- Model: Fable 5.1
- Escalations: none
- Allowed files: docs/demo/share-*.png, docs/demo/share-proof.md, docs/DEMO.md

## Log

- 02:00 — `:server:run` server on localhost:8080, `adb reverse tcp:8080 tcp:8080` on the `nitpick-qa` emulator (API 36). Test repository served at `git://10.0.2.2/remote.git`; the desktop clone and the phone clone have the same remote URL, therefore the same `repoUrl` key on the server side.
- 02:05 — desktop (`main` 3b14570, dev-run build): Anthropic review of `feature/search-pages-import` vs `main`, 2 nits (lastPage, toInt); **Share review** → "Shared" badge, `GET /reviews?repoUrl=…` lists review `05bc388` with 2 nits.
- 02:06 — phone: Shared reviews → Refresh → "feature/search-pages-import · 05bc388 · 2 nits" line → opening with the "Shared review" badge (no model call) → nit `src/Search.kt:17` → Apply suggestion → "Applied", "1 suggestion applied", `require(size > 0)` present in `files/repos/remote/src/Search.kt` on the device.
- Screenshots: `docs/demo/share-desktop-shared.png`, `share-android-opened.png`, `share-android-applied.png`.

## Uncertain

- The phone cloned the same remote as the desktop; with two different URLs (SSH vs HTTPS) it is the server normalisation that makes the link, not replayed here.
