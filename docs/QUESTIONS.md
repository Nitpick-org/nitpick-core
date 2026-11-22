# Questions agents → humans

> Agents write here when no one is available. **Append-only**: we add a line, we do not
> rewrite other people's. Humans answer at each sync.

## Priorities

- **P0**: blocks a demo-critical ticket. Answer expected < 30 min.
- **P1**: blocks a non-critical ticket. Answer at the next sync.
- **P2**: curiosity / improvement. Answer if time allows.

## Queue

| Time | Prio | Agent / ticket | Question | Options | Recommendation | Human answer |
| --- | --- | --- | --- | --- | --- | --- |
| | | | | | | |
| 13:05 | P1 | 13-structure | `GitRepositoryFactory` and `SettingsStore` as an interface rather than an `expect` (§6)? | expect/actual with empty actual / interface injected via AppContainer | interface: an expect without actual does not compile, and the manual DI of §5 suffices | |

## To answer before H0

| # | Question | Options | Recommendation | Who |
| --- | --- | --- | --- | --- |
| Q-001 | Which stack? | Next.js / SvelteKit / Symfony+Turbo | the one all 3 master | H2 |
| Q-002 | Which sponsor challenge to aim for? | none / 1 / 2 | 1 main one, announced at the tech talks | H1 |
| Q-003 | Who owns the submission form? | H1 / H2 | H1, backup H2 | H1 |
| Q-004 | Paid usage after the Ultra weekly limit? | no / yes capped at 50 $ / yes 100 $ | no, unless an account runs dry before 23:00 | H2 |
| Q-005 | Public repo right now? | yes/no | yes, no secret in the history | H2 |
| Q-006 | App API key: Ultra credits or something else? | Ultra credits / personal key / sponsor | Ultra credits (`APP_ANTHROPIC_API_KEY`) | H2 |

## Discrepancies between docs (recorded on 21/11 at H0+1, to be settled)

While waiting for the answer, agents apply the order of authority of `AGENTS.md`:
the LauzHack rules, then `docs/CONTEXT.md`, then the rest.

| # | Discrepancy | Options | Recommendation | Who |
| --- | --- | --- | --- | --- |
| Q-007 | `CONTEXT.md` §13 plans two slides (shared vs specific, agentic process); the LauzHack rules forbid slides (backup images and videos allowed). | "About" screen in the app + output of the counting script / full-screen backup images / keep the slides | screen in the app + live script output; no slide | H1 |
| Q-008 | `CONTEXT.md` §2 talks about agents in "JetBrains Air"; the JetBrains talk talks about JetBrains AI and Junie (bonus ~10–15 %, to be verified); the team works with Claude Code. | Claude Code alone / Claude Code + at least one real Junie task logged / everything in Junie | Claude Code + one real Junie task in `AGENT_LOG.md`; confirm the name at the booth | H1 |
| Q-009 | Schedule: `CONTEXT.md` §10 (gates 13/17/21 h, sleep 02–07 h, ≥ 3 h each) versus `PLAYBOOK.md`/`STRATEGY.md` (skeleton deployed at 16:00, 4 h shifts at 22/02/06 h). | `CONTEXT.md` / `PLAYBOOK.md` / mix | gates and sleep window from `CONTEXT.md`; rotation to be set at sync | all |
| Q-010 | JGit 6.10.1 instead of the latest stable (`CONTEXT.md` §5): JGit 7 does not dex under API 34 (commit `df36830`). | approve / JGit 7 with minSdk 34 | approve, record it in `DECISIONS.md` | H2 |
| Q-011 | The wizard generates `shared/` + `desktopApp/` + `androidApp/`, not the `composeApp/` of §6; the source set is called `jvmSharedMain`. | approve and fix §6 / go back to `composeApp` | approve, fix §6, record it in `DECISIONS.md` | H2 |
| Q-012 | `STACK.md`, `ARCHITECTURE.md` (tree), `PRD.md`, the toolchain of `ONBOARDING.md`, `DRY-RUN.md` and `PRE-EVENT-CHECKLIST.md` still describe the TypeScript base. | "obsolete" banner at the top / move to `docs/archive/` / leave them | banner at the top (`AGENTS.md` already marks them as historical) | H2 |
| Q-013 | `DEMO.md` and `GLOSSARY.md` are still templates. | fill them from `CONTEXT.md` §13 and §1 / wait for the feature freeze | fill them now: DEMO by H1, glossary (nit, base, lane, anchor, suggestion) by H2 | H1, H2 |
| Q-014 | `CONTEXT.md` §5 plans an expect/actual `SettingsStore` (desktop file, Android SharedPreferences/DataStore); #18 ships a single JSON `FileSettingsStore` in `jvmSharedMain` (`java.io.File` exists on both targets). | approve and fix §5 / write the Android actual in SharedPreferences | approve: zero platform code, a single test; `DECISIONS.md` | H2 |
| Q-015 | `CONTEXT.md` §10 adds "Air installed + first agent logged" to the 12h–13h gate, already past; `AGENT_LOG.md` is empty and #39 sets the first Air screenshot at 17:30. | leave the 12h–13h gate failed / move the Air proof to 17:30 (#39) / put it at the 21:00 gate | 17:30 via #39, and fix the table in §10 | H2 |
| Q-016 | Slides: `LAUZHACK.md` and `DEMO.md` say "no slides"; `CONTEXT.md` §10 (09h30–11h) and §13 (points 6 and 7) and #39 ("add a slide") still plan some. Q-007 was not applied to the text. | About screen in the app / full-screen images (Air screenshots, output of the counting script) / keep the word "slide" | full-screen images + live script output; replace "slide" in §10, §13 and #39 | H1 |
| Q-017 | `CONTEXT.md` §11 asks agents to write in `AGENT_LOG.md` on each task; `AGENTS.md` and the header of `AGENT_LOG.md` say the opposite (agents → `progress/<ticket>.md`, a human compiles). With Air, who records the "Junie via Air" sessions? | agents write / human compiles at syncs / the human who launches the Air session records the line at the end | the human who launches the Air session, at the end of the session; align §11 | H2 |
| Q-018 | 21:00 gate (desktop review loop): #19 (`diffAgainst` still `TODO`), #20 (Koog: nothing written, no entry in `RESEARCH.md`, lib missing from the Gradle catalog) and #22 are all three on the same person at 16:45. | keep everything / hand #19 to H3 now / cut #34–#38 as WON'T tonight | #19 to H3 (brief in the issue), #20 then #22 by H2, #33 reduced to "missing key" + loading, #34–#38 after the Android gate | H2 |
| Q-019 | H1 (21/11): we aim for the maximum points, so the 5 bonus features. The Kotlin backend goes from WON'T to MUST (`CONTEXT.md` §7.1: Ktor review-sharing server, track D after the 21:00 gate). Who carries track D, and is the `SharedReview`/`ReviewShareClient` contract of §6 approved? | H1 via Air / H3 after the Android loop / cut if the 21:00 gate slips | H1 via Air (isolated module, little coupling); H2 approves the contract and records it in `DECISIONS.md`; update the tree in `AGENTS.md` | H2 |
| Q-020 | minSdk: #109 moved it from 26 to 33 because JGit 6.10 calls API 33 methods (`InputStream.readNBytes`, `transferTo`, `Collectors.toUnmodifiableList`, verified in the jar), but `CONTEXT.md` §14 still mentions minSdk 26 and only an API 36 emulator ran. Is the demo phone on Android 13 or newer (`adb shell getprop ro.build.version.sdk` ≥ 33)? Otherwise the app no longer installs. | keep 33 / go back to 26 with desugaring, to be proven on an API ≤ 32 emulator | keep 33; check the phone, fix §14 and record it in `DECISIONS.md` | H2 + H3 |
| Q-021 | JetBrains (22/11, booth answer): no API keys provided to the teams; Junie Lite is free with a JetBrains account until the end of the year. The app's Anthropic key remains ours (Q-006). | nothing to change / run the Junie proof of #39 through Junie Lite / say it in `DEMO.md` "reused" | Junie Lite for the #39 proof, one line in `DEMO.md`; scope unchanged | H1 |
| Q-022 | The team rules' status quotes 200 tests; `:shared:jvmTest` + `:server:test` on `main` (22/11, 10:05) run 218, 4 skipped without provider keys. | leave it / update at the next sync / drop the number and point to `check.sh` | update at the next sync, the README stays the source of truth | H2 |
| Q-023 | #227 reading B: should Settings offer "JetBrains AI (Junie)" as a review provider? No public Junie API is known and `CONTEXT.md` puts a runtime Junie integration in WON'T. | nothing in the app / a greyed "coming" entry with a tooltip / a real integration if an API appears | nothing in the app, idea parked in `IDEAS.md`; the Junie proof is the Air session of `docs/demo/junie-lite-brief.md` | H2 |

## Rules

- A question = a decision to make, not a topic for discussion.
- Always offer 2–3 options and **your** recommendation.
- If the answer does not change the outcome, choose and note the assumption in the commit.
- Never put a secret in a question.
