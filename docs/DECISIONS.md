# Decisions

> One line per durable decision: what, why, who. Agents propose; H2 (or the
> domain owner) decides and writes. Never a debate in this file.

## To decide before H0

| # | Decision | Options | Recommendation | Who | Deadline |
| --- | --- | --- | --- | --- | --- |
| D-001 | Stack | Next.js / SvelteKit / Symfony+Turbo | the one all 3 can debug | H2 | J-4 |
| D-002 | Idea | shortlist of 3 | `STRATEGY.md` scoring | H1 | H0 |
| D-003 | Sponsor challenge | 0 / 1 / 2 | 1 main | H1 | 11:40 Sat |
| D-004 | Roles | H1/H2/H3 | product / archi / builder | all | J-5 |
| D-005 | Sleep | 3 h shifts | 00/03/06 | all | J-1 |
| D-006 | Public repo | yes/no | yes (submission link) | H2 | J-1 |
| D-007 | API budget | cap | to define | H2 | J-1 |
| D-008 | Who submits | H1/H2 | H1, with H2 as backup | all | H0 |

## Decisions taken

| Time | Decision | Why | Who |
| --- | --- | --- | --- |
| 12:28 | `shared/` + `desktopApp/` + `androidApp/` project (wizard), no `composeApp/`; intermediate source set `jvmSharedMain` | This is what the wizard generates; a single JGit implementation for desktop and Android (Q-011) | H2 |
| 12:33 | JGit 6.10.1, not 7 | JGit 7 uses Java records that the dexer refuses under API 34 (Q-010) | H2 |
| 15:37 | A single JSON `FileSettingsStore` in `jvmSharedMain`, no expect/actual nor SharedPreferences | `java.io.File` exists on both targets: zero platform code, a single test (Q-014, #18) | H2 |
| 16:09 | JetBrains New UI art direction (`DESIGN-SYSTEM.md`), single teal accent `#0F766E` / `#5EEAD4`, the old blue moves to lane 5 | Dev tool aligned with the challenge IDE; teal distinguishes Nitpick (#32) | H2, H3 |
| 16:28 | JetBrains Air + Junie/Codex/Claude as development process; Koog stays the product engine, no runtime integration of Air | Challenge "agentic process" bonus without a dependency in the app (Q-008) | H2 |
| 16:50 | A PR branch rebases on `main` and is pushed with `--force-with-lease`; force push stays forbidden on `main` | No more `-rebased` branches or duplicated PRs (#28) | H2 |
| 17:55 | Kotlin backend: Ktor `server/` for **sharing** reviews (§7.1, `SharedReview` / `ReviewShareClient` contract), local, without an account or LLM on the server side; MUST after the 21:00 gate, track D | The jury's 5 bonus features without touching the "local review" pitch or moving Koog out of `commonMain` (Q-019; owner of track D to be confirmed) | H1, H2 |

## Format of a line

```md
| 14:32 | Session cookie instead of JWT | Simpler for the demo, no refresh | H2 |
```

If a decision deserves more than one line, write a short paragraph under the table and
link it to the ticket concerned. Agents do not modify this file.
