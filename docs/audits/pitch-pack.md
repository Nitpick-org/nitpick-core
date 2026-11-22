# Pitch pack — verified claims and guardrails

Source: six audit lanes on `origin/main` @ `54c37a2`, plus #133/#135 merged after the pin. Do not claim anything below the "do not claim" line until its issue lands.

## Verified claims

- **~99 % shared**: `./scripts/count-lines.sh` gives 7,960 / 8,044 production lines = 99.0 % (`commonMain` + `jvmSharedMain`); raw `.kt` count gives 98.9 % (8,687 / 8,780); `commonMain` alone is 93.2 %. Say "shared across desktop and Android", not "multiplatform common".
- **178 JVM tests + 9 server tests**, 0 failures at the pin (4 provider tests opt-in/skipped). Re-run `./scripts/check.sh` on a machine with the Android SDK before quoting.
- **10 Android captures + 1 dogfooding capture**: `docs/demo/android-01..10`, `docs/demo/dogfooding-104-review.png`.
- **Real Koog review**: Anthropic path proven; OpenAI/Google/Ollama configurable, opt-in tests only (#82). Tool calls (`read_file`/`list_files`) and snippet anchoring work.
- **Ktor sharing backend**: `POST/GET /reviews`, JSON store, desktop + Android UI; 9 server tests.
- **Dark/light**, **EN/FR** (177 strings + 5 plurals), Compose Multiplatform.
- **Security**: no live secret in `origin/main` or `--all`; `ReviewToolbox` rejects absolute/`..`/`.git`/untracked paths; tracked symlinks cannot escape.

## Do not claim

- "Nits inline" — they are in the Review tab, not on diff lines (until #145).
- "Apply all for this file" — absent (until #145).
- "Before/after suggestion" — only the after half (until #145).
- "Keyboard shortcuts" — none; the `⌘⇧R` hint is in the internal catalogue (P2 #171).
- "Share proven end-to-end" — pass: #170 / PR #184 has desktop shared, phone opened, phone applied captures. Show `docs/demo/share-*.png`.
- "Built with Air / Junie" — no capture; the comparison is Claude Code/subagents (until #169).
- "No security issue" — no live secret, but the LAN server is unauthenticated (#167) and there is no LICENSE (#162).
- "All four providers tested" — only Anthropic has a real recorded call (#82).
- "99 % multiplatform common" — 99 % is shared+JGit; commonMain is 93.2 %.

## Fixed since the audit

- #140 Pull refusal message (PR #161).
- #141 before/after lines (PR #174).
- #142 anchor keeps the line number (PR #173).
- #143 sheet insets (closed as not reproducible since #135).
- #144 44 dp top-bar actions (PR #176).
- #170 share desktop → Android proof (PR #184).

## Demo risks

- Repo private + no LICENSE: submission blocker (#162).
- README/DEMO stale, wrong clone URL, `pocket-ledger` missing, `DEMO_MODE=mock` does not exist (#163).
- No backup video (#164).
- Server on `0.0.0.0` with no auth (#167).
- Share desktop → Android unproven (#170).
- `#133/#135` merged after the audit pin: re-check phone UX claims before the pitch.

## Process evidence to show

- `progress/16-graph.md:37-52`: two subagents, comparison table, decision.
- `docs/demo/dogfooding.md`: 2 nits, 1 real bug (#114), capture.
- `progress/*.md`: per-ticket journals.
- State honestly that `AGENT_LOG`/`REVIEW_LOG` are being filled and no Air capture exists, unless #169 produces one.
