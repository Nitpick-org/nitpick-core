# Partial audit synthesis — spec + security (22/11, 01:45)

Scope: #126 (spec) + #129 (security), audited on `origin/main` @ `54c37a2`.
The final synthesis (`docs/audits/audit-2026-11-22.md`) waits for #127, #128, #130, #131.

## P0
None in these two lanes.

## P1 — fix or reword before the freeze

| # | Finding | Lane | Action | Cost |
| --- | --- | --- | --- | --- |
| 1 | Nits are not inline in the diff; the review is a separate tab list | spec | Render nit cards under the matching diff line, or reword README/CONTEXT/demo to "nits in the review panel, anchored at file:line" | 1–2 h |
| 2 | Keyboard shortcuts absent; the `⌘⇧R` catalogue hint is decorative | spec | Add desktop `onPreviewKeyEvent` (review, fetch, apply) or remove the hint | 1 h |
| 3 | Process evidence is partial: logs empty, no Air capture; the two-agent comparison and dogfooding exist but are not surfaced | spec | Fill `AGENT_LOG`/`REVIEW_LOG` from `progress/*.md`; put `progress/16-graph.md:37-52` and `docs/demo/dogfooding.md` in the pitch; only claim Air if a real capture exists | 1 h |
| 4 | Share desktop → Ktor → Android is unproven | spec | Replay with the server, `adb reverse`, screenshot both screens | 1 h |
| 5 | Review server binds `0.0.0.0:8080` with no auth; LAN peers can read code snippets and write reviews | security | Bind `127.0.0.1` and use `adb reverse` for the phone; token auth post-freeze | 15 min |
| 6 | No `LICENSE` / third-party notice before going public | security | Add a LICENSE and `THIRD-PARTY-NOTICES` (JGit EPL-2.0, Apache-2.0 deps, JetBrains Mono OFL-1.1) | 1 h |

## Pitch-safe claims

- 99.0 % shared (7,960 / 8,044 production lines), 84 platform lines.
- 178 JVM tests + 9 server tests, 0 failures (4 provider tests skipped without keys).
- Real Koog review with Anthropic/OpenAI/Google/Ollama settings; tool calls and anchoring.
- 10 Android captures + dogfooding capture; the loop works on the emulator.
- Dark/light, EN/FR (177 strings + 5 plurals), Ktor sharing backend.
- Path traversal is defended; no live secret in history.

## Do not claim

- "Nits inline" — they are in the Review tab, not on diff lines.
- "Apply all for this file" — absent.
- "Before/after suggestion block" — only the after half is shown.
- "Keyboard shortcuts" — none; the `⌘⇧R` hint is decorative.
- "Share proven end-to-end" — code and tests only; no desktop→Android capture.
- "Air / Junie" — no capture; the comparison is Claude Code/subagents.
- "No security issue" — no live secret, but the LAN server is unauthenticated and the repo has no LICENSE.

## Security pre-public checklist

- [x] No live secret in `origin/main` or `--all`; `.env` never committed.
- [ ] Bind the review server to `127.0.0.1` for the demo (15 min).
- [ ] LICENSE + third-party notices (1 h).
- [ ] Replace the fake `sk-ant-`/`ghp_` mockup placeholders (15 min).
- [ ] Make the repo public after the secret scan and the README fix (H2/H1).
