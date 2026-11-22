# Audit: release and process evidence (#131)

Base: commit b95f8bf (origin/main 33e4f4f), read-only. Date: 2026-11-22.

## Checklist

| # | Item | Owner | Status | Cost | Evidence |
| --- | --- | --- | --- | --- | --- |
| 1 | Repo public (rule: "public GitHub repo with README") | H2 zooldyyk | RED: `PRIVATE` | 15m | `gh repo view Nitpick-org/nitpick-core --json visibility` gives `PRIVATE`; `docs/LAUZHACK.md:82,165` |
| 2 | LICENSE file | H2 | RED: none, `licenseInfo: null` | 15m | `ls LICENSE* NOTICE*` finds nothing; gh `licenseInfo: null` |
| 3 | Third-party notices | H2 | RED: not written | 15m | JetBrains Mono ttf x2 (`shared/src/commonMain/composeResources/font/`, OFL-1.1, attribution not recorded); Simple Icons GitHub/GitLab SVGs CC0 (only in `progress/122-phone-ux.md`); Koog, JGit (EDL), Ktor, Compose deps unlisted |
| 4 | README status and clone URL | H1 Gabriel | RED: stale | 15m | `README.md:20-30` says contracts, graph, diff, AI review, theme "planned", while main ships the full loop. `README.md:91` clones `github.com/zooldyyk/hackathon.git`; the repo is now `Nitpick-org/nitpick-core` (redirect works only while access exists) |
| 5 | Demo video (2 min backup) | H1 | RED: placeholder `<lien>`, no file in `docs/demo/` | 1h | `docs/DEMO.md:83` |
| 6 | Stable fallback URL | H1 | RED: placeholder | 15m | `docs/DEMO.md:99` `<preview stable>` |
| 7 | Plan B boxes | H1 | RED: all `[ ]` | 15m | `docs/DEMO.md:77-81` |
| 8 | Plan B mentions `DEMO_MODE=mock` (`docs/DEMO.md:77`) | H1 | RED: `grep -rl DEMO_MODE shared desktopApp androidApp server` returns nothing; no such mode found in the plan's context. Verify it exists in code or delete the line | 15m | `docs/DEMO.md:77` |
| 9 | Rehearsals | H1 | RED: table empty (5 rows) | 1h | `docs/DEMO.md:103-110` |
| 10 | Demo repo `pocket-ledger` | H1/H3 | RED unverified: not found under `Nitpick-org` (`gh repo view` fails); `progress/44-demo-repo.md:44` says "launch the brief in Air", not done | 1h | `docs/DEMO.md:94`; the Android replay used `pepito2t/OpenTab` instead |
| 11 | `docs/AGENT_LOG.md` | humans (H1 compiles) | RED: header only, 0 rows | 15m | file is 11 lines; do NOT fill it automatically (H2 rule) |
| 12 | `docs/REVIEW_LOG.md` | humans | RED: header only, 0 rows | 15m | file is 9 lines |
| 13 | Air / Junie capture | H1 | RED: none | >1h or fallback | no Air capture in `docs/demo/`; `progress/22-review-loop.md:6,47` "no Air proof"; only `docs/demo/junie-brief.md` (a brief, not a session) |
| 14 | Multi-agent comparison | H2 | GREEN | - | `progress/16-graph.md:37-45` two sub-agents (A one pass, B two passes), table plus decision |
| 15 | Dogfooding | H1 | GREEN | - | `docs/demo/dogfooding.md` (1 entry, #104) plus `docs/demo/dogfooding-104-review.png` (1.7M) exists |
| 16 | Android replay captures | H3 | GREEN | - | `docs/demo/android-01..10-*.png` exist, listed in `docs/DEMO.md:85-92` |
| 17 | DEMO.md reflects phone UX (#133/#135) | H3/H1 | AMBER: not reflected | 15m | `git log` on DEMO.md: last commit bc42929 (#119); no mention of slide transition, "Up to date", haptic, clean-branch screen. Only `progress/122-phone-ux.md` documents it. Captures `android-01..10` predate the new UI (icon top bar, new loading card) |
| 18 | CI | H2 | AMBER: `.github/workflows/ci.yml` fully commented out and is the old Node/pnpm template | 1h | `ci.yml:1-60`; nothing runs on PR. Acceptable for a hackathon, but no "green CI" claim |
| 19 | `check.sh` reproducible | H2 | AMBER: needs JDK 17+ and the Android SDK (`ANDROID_HOME`) to run `:androidApp:assembleDebug`; jury cannot run it without the SDK | 15m | `scripts/check.sh:21-29`, `README.md:87-88`; not run in this audit (read-only, timebox) |
| 20 | Provider tests skipped without keys | H2 | AMBER: `RealReviewCallTest` is skipped by `assumeTrue` when keys are unset, so a green `check.sh` does not prove a real LLM call | 15m | `RealReviewCallTest.kt:52`; `progress/82-provider-paths.md` ("Skip = assumeTrue") |
| 21 | Submission on app.lauzhack.com | H1 | OPEN: before 12:00 Sunday, URL must be public | 15m | `docs/LAUZHACK.md:18,74-88` |

## Pitch-safe claims (verified)

- Multi-agent comparison produced and documented: two independent sub-agents on the graph lane (`progress/16-graph.md:37-45`).
- Dogfooding: Nitpick reviewed our own branch `ticket/104-audit-data-safety`, 2 nits, 1 real bug still on main (issue #114), 0 false positive (`docs/demo/dogfooding.md`, capture present). Claim "one dogfooding pass", not "systematically".
- Android review loop replayed on an emulator, API 36, with 10 captures: clone, checkout, review finding 2 bugs, apply, commit, push (`docs/DEMO.md:85-92`, PNGs present).
- Agents were driven with Claude Code (Fable, Sonnet, Opus) under human review, with `progress/<ticket>.md` journals (about 55 files, e.g. `progress/122-phone-ux.md:5-7`, `progress/82-provider-paths.md:5`) and repo hooks that guard protected files (`CLAUDE.md`).
- Per-provider review test exists (Anthropic, OpenAI, Google, Ollama) but only runs with keys (`progress/82-provider-paths.md`); say "tested manually with a key", only if someone actually did.
- Engine is Koog; Air/Junie are not product dependencies (`docs/CONTEXT.md:60-61`).

## Do not claim

- "Built with JetBrains Air / Junie". No session or capture exists. Remove or soften: `docs/DEMO.md:95` ("Junie via Air, sinon Claude Code"), `docs/AGENT_LOG.md:6-7` (instructions only, fine), `docs/CONTEXT.md:45-47,132,355,459-461`. The pitch must say: Claude Code was the fallback, per `docs/CONTEXT.md:427,480`. Honest wording: "we used several agents and reviewed every diff".
- "Open source / MIT / public repo" until items 1 and 2 are done.
- "Green CI". CI is disabled.
- "check.sh proves the AI works". Provider tests skip without keys.
- "Every agent session was logged in AGENT_LOG/REVIEW_LOG". Both are empty. Real material exists in `progress/*.md` (for example `progress/16-graph.md` multi-agent, `progress/22-review-loop.md`, `progress/104-audit-data-safety.md`, `progress/105-audit-vm-errors.md`, `progress/122-phone-ux.md`); the humans choose the wording. Rejections and corrections (REVIEW_LOG) must come from the humans' memory or from PR review comments; I found no source in the repo to back any specific entry, so none is proposed.
- The `98,2 %` figure in `docs/DEMO.md:45` (snapshot from 21/11 19:00, 4 466 / 4 546 lines) is stale: use the number recomputed in `docs/audits/architecture.md`. Do not quote a number that is not computed by the line-count script (#40) or the code.
- "Built entirely during the event" without caveat. `dovatgabriel/fourchette` (public, "A fast and friendly git client for mac", pushed 2026-11-19) predates the hack; be ready for jury Q2 "what did you reuse" (`docs/DEMO.md` Q&A list). Low confidence that it is related; Gabriel to confirm.
- Phone UX captures as current. They predate #133/#135.

## JSON summary

```json
{"lane":"release","findings":[
{"severity":"P0","title":"Repository is private and has no license","evidence":"gh repo view Nitpick-org/nitpick-core --json visibility,licenseInfo -> PRIVATE, licenseInfo null; no LICENSE file; docs/LAUZHACK.md:82,165","impact":"submission","fix":"Make repo public (or mirror), add LICENSE (e.g. MIT/Apache-2.0) before submitting before 12:00","cost":"15m","confidence":"high"},
{"severity":"P0","title":"README is stale and clone URL points to the old repo","evidence":"README.md:20-30 mark graph/diff/review as planned; README.md:91 clones zooldyyk/hackathon.git while the repo is Nitpick-org/nitpick-core","impact":"submission","fix":"Update status table to what ships, give correct public clone URL, add a build/run section with the Android SDK prerequisite","cost":"15m","confidence":"high"},
{"severity":"P0","title":"No backup video and no fallback URL; Plan B and rehearsal tables are empty","evidence":"docs/DEMO.md:77-83,99,103-110 (placeholders <lien>, <preview stable>, all [ ])","impact":"demo","fix":"Record a 2 min video of the desktop loop, host it, link it in DEMO.md and README, tick Plan B, run at least 2 timed rehearsals","cost":">1h","confidence":"high"},
{"severity":"P1","title":"No Air/Junie evidence; Air claims remain in docs","evidence":"no Air capture in docs/demo/; progress/22-review-loop.md:47; docs/DEMO.md:95; docs/CONTEXT.md:45-47,459-461","impact":"pitch","fix":"Drop the 'built with Air' claim or run one real Air session and capture it; state Claude Code as the actual tool","cost":"1h","confidence":"high"},
{"severity":"P1","title":"AGENT_LOG and REVIEW_LOG are empty although jury criterion 5 is the process","evidence":"docs/AGENT_LOG.md (header only), docs/REVIEW_LOG.md (header only); backing material in progress/16-graph.md, progress/22-review-loop.md, progress/122-phone-ux.md","impact":"score","fix":"Humans compile a few real rows from progress/*.md and actual PR rejections; agents must not write them","cost":"1h","confidence":"high"},
{"severity":"P1","title":"No third-party notices (fonts, icons, libraries)","evidence":"shared/src/commonMain/composeResources/font/jetbrains_mono_*.ttf (OFL); progress/122-phone-ux.md Simple Icons CC0; no NOTICE file","impact":"submission","fix":"Add NOTICE/THIRD_PARTY.md listing JetBrains Mono OFL-1.1, Simple Icons CC0, Koog, JGit EDL, Ktor, Compose","cost":"15m","confidence":"medium"},
{"severity":"P1","title":"Demo repo pocket-ledger not found and DEMO_MODE=mock unverified","evidence":"gh repo view Nitpick-org/pocket-ledger fails; docs/DEMO.md:77,94; progress/44-demo-repo.md:44 still 'launch the brief in Air'","impact":"demo","fix":"Confirm where the demo repo and bug branch live (or switch to OpenTab used in the Android replay), update DEMO.md; delete the DEMO_MODE line if no such mode exists","cost":"1h","confidence":"medium"},
{"severity":"P1","title":"Demo docs and captures predate phone UX PRs #133/#135","evidence":"git log docs/DEMO.md last commit bc42929 (#119); progress/122-phone-ux.md lists the new UI; docs/demo/android-*.png predate it","impact":"demo","fix":"Update DEMO.md Android steps (slide, Up to date, clean-branch screen) and recapture 3 phone screens","cost":"1h","confidence":"medium"},
{"severity":"P2","title":"CI workflow is fully commented out (old Node template)","evidence":".github/workflows/ci.yml:1-60","impact":"score","fix":"Replace with a Gradle job (check.sh needs the Android SDK) or delete it; do not claim green CI","cost":">1h","confidence":"high"},
{"severity":"P2","title":"Real provider tests skipped without keys; check.sh needs Android SDK","evidence":"RealReviewCallTest.kt:52 assumeTrue; scripts/check.sh:21-29; README.md:87-88","impact":"score","fix":"Say 'manually verified with a key' only if true; document SDK requirement for jury","cost":"15m","confidence":"high"}
],"coverage_gaps":["check.sh not executed (read-only, timebox)","license compatibility of dependencies not verified (no dependency license scan)","did not verify that the DEMO_MODE=mock code path exists (only grep of docs)","did not check issue/PR comments for human review rejections usable for REVIEW_LOG","relation of dovatgabriel/fourchette to Nitpick not investigated","Q&A answers in DEMO.md not checked for filled content beyond placeholders"]}
```
