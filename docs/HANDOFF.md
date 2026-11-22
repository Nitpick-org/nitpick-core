# Handoff — project status at 21/11/2026 16:45

> For the review with 3. Objective: understand **what changed**, **why**, **what
> remains open**, and **what needs to be decided** before starting again.
>
> Product reference: [`docs/CONTEXT.md`](CONTEXT.md). Challenge reference:
> [`docs/JETBRAINS-CHALLENGE.md`](JETBRAINS-CHALLENGE.md).

## 0. Snapshot

| Field | Value |
| --- | --- |
| Branch | `main` |
| Last commit | `b58832b` |
| State | no open PR; `main` unprotected; work by worktree/issue |
| Phase | H0 + ~6 h; gates 21:00 (desktop review), 02:00 (Android) |
| Demo | 3 min, no slides; desktop + Android; demo repo to prepare |
| Challenge | JetBrains / KMP — see `docs/JETBRAINS-CHALLENGE.md` |

## 1. Why these changes

Two things have moved since the first spec:

1. **The design**: pepito produced a JetBrains New UI design system and then a UI handoff
   (PR #32); the accent moved from blue to **teal**, the desktop shell is in place.
2. **The official challenge**: the brief asks to **build with JetBrains Air** for the
   *Agentic Development Process* bonus. Air is an orchestra of agents in the IDE, not
   an API/provider. The spec therefore had to distinguish **Air/Junie = process** and
   **Koog = product AI**.

## 2. Spec changes

Commit `175121a`, `5d63ce5`, `fdaab6c`:

| Section | Change |
| --- | --- |
| §1 Identity | JetBrains IDE New UI DA, teal accent, JetBrains Mono |
| §2 Jury | Air/Junie = development process, **not** a runtime provider; multi-agent bonus |
| §3 Team | each human installs Air + an agent (Junie Lite/Codex/Gemini/Copilot/Claude); logs/screenshots |
| §5 Stack | **product AI = Koog**; **development = Air + agents** |
| §7 Review | the review engine stays Koog; Air/Junie are not called by the app |
| §8 WON'T | no Air/Junie runtime integration (provider/CLI in the app) |
| §9 Process | each track driven in Air, fallback Claude Code/Codex CLI |
| §9.1 | ownership of the H1/H2/H3 issues |
| §10 | Air installed + first agent logged in within the first hour |
| §11 | Air/Junie rules + fallback |
| §13 | the demo shows Air screenshots + the `REVIEW_LOG` |
| §14 | risks Air/Junie unavailable, trust/permissions |
| §15 | Air, Junie, Koog links |

**Key decision**: we do **not** wire Air/Junie into Nitpick. The product keeps Koog and
the LLM providers; Air/Junie serve to build and to prove the process.

## 2.1 Audit of 21/11 — findings and follow-up

The audit confirmed that the real review loop was launched from the desktop (2 nits), then
found four problems fixed or to be fixed:

| Finding | Status |
| --- | --- |
| The diff compared the tip of `main`, not the **fork point**; main files appeared as deleted | fixed by #53 (merge-base + working tree, untracked excluded) |
| The review took the first 20 **alphabetical** files; two Godot `.import` sidecars were re-read instead of the code | fixed by #54/#55 (sort by changed lines, assets ignored) |
| Koog rejected `claude-opus-5-5` ("does not support tools") | fixed by #52 (model capabilities) |
| The UI shows `:0` for a whole-file nit | **remains**: to handle with #33 (review states/display) |
| The full loop has not yet been replayed by hand with a key | **remains**: #22 |

## 3. Design / UI changes

| Commit | What |
| --- | --- |
| `1c7e2d1` | JetBrains New UI design system (tokens, components, catalogue, docs) |
| `1ede34c` | **teal** accent (`#0F766E` / `#5EEAD4`), blue in lane 5 |
| `a60b12b` | desktop shell: toolbar, "Nitpick branch" CTA, branch, tabbed inspector, provider in the status bar |
| `b10184e` | merge of the UI handoff #32 |

To read:
- [`docs/DESIGN-SYSTEM.md`](DESIGN-SYSTEM.md) — tokens, components, rules;
- [`docs/design/UI-HANDOFF.md`](design/UI-HANDOFF.md) — screens, states, copy;
- `docs/design/mockups/` — reference HTML mockups (not code to port).

## 4. Code status

Already there:

- KMP project `shared` + `desktopApp` + `androidApp`;
- contracts from §6 (`domain`, `git`, `review`, `settings`);
- commit graph with lanes (`graph`);
- unified diff parser (`diff`), fork-point diff (#53);
- opening a local repo + branches + commits + details (desktop);
- light/dark theme + persisted settings;
- design system and desktop shell;
- real Koog agent (#51), fixed capabilities (#52), real review launched (2 nits);
- Diff/Review/Apply/Commit UI loop (#50) and sorted review context (#54/#55).

Not done yet:

| Issue | Owner | What |
| --- | --- | --- |
| #22 | H2 | **remains**: manual pass of the desktop loop with a key, screenshot, closing |
| #21 | H3 | Android clone + Repos/Graph (in progress) |
| #23 | H3 | Android review loop |
| #33 | H1 | review states + `whole file` display (the `:0` from the audit) |
| #34/#35 | H3 | desktop/Android settings per handoff |
| #39/#44 | H1 | demo, submission, demo repo, Air evidence |
| #40 | H1 | shared line count |
| #46/#48 | H1 | share backend, after 21:00 |
| #36–#38/#42/#43/#45 | — | P2 blocked, after the critical path |

## 5. Ownership after inversion

| Role | GitHub | Issues | Gate |
| --- | --- | --- | --- |
| **H1 — Product & Pitch** | `@dovatgabriel` | #39 demo/submission/Air, #33 states | 17:30 first Air evidence; 09:30 freeze; 11:30 submission |
| **H2 — Architecture & Integration** | `@zooldyyk` | #19, #20, #22 | **21:00** desktop review |
| **H3 — Builder & Demo** | `@pepito2t` | #21, #23, #34, #35 | **02:00** Android review |

P2 #36–#38: unassigned, not to be started before the critical path.

## 6. Expected Air evidence

For the bonus, **artifacts** are needed, not just a declaration:

- 1 real Air session per owner (Junie/Codex/Gemini/Copilot/Claude);
- 1 **two-agent** comparison on the same task (e.g. Junie vs Claude/DeepSeek);
- screenshots: agents side by side, diff/result, human correction;
- `docs/AGENT_LOG.md`: `Junie via Air`, `Claude via Air`, track, task, decisions;
- `docs/REVIEW_LOG.md`: what was rejected/fixed and why;
- a "Agentic Development Process" demo slide.

## 7. Review with 3 — 20 min checklist

1. **Challenge**: read `docs/JETBRAINS-CHALLENGE.md` (Air = process, Koog = product).
2. **Spec**: read the modified sections of `docs/CONTEXT.md` (§2, §3, §5, §7, §8, §9.1,
   §10, §11, §13, §14).
3. **Design**: read `docs/DESIGN-SYSTEM.md` + `docs/design/UI-HANDOFF.md`; check that
   the shell/teal is consistent.
4. **Issues**: check the H1/H2/H3 mapping and the order #19 → #20 → #22 / #21 → #23.
5. **Code**: `git pull`, `./gradlew :desktopApp:run`, open a repo, check the shell.
6. **Air**: confirm that the 3 have access to Air/Junie Lite and choose the second agent
   (Claude, Codex, Gemini, DeepSeek if ACP/BYOK).
7. **Decisions**: confirm that Junie CLI in the product is **out of scope** (stretch
   after 21:00 only if the loop is green).

## 8. Questions to settle in review

- [ ] Is the mapping H1 = dovat, H2 = zooldyyk, H3 = pepito correct?
- [ ] Do the 3 laptops have Air + a connected agent? Who uses Junie Lite?
- [ ] Which second agent for the comparison? Claude, Codex, Gemini, DeepSeek?
- [ ] Do we confirm **no Air/Junie runtime integration** in the app?
- [ ] Is the 21:00 gate achievable with #19/#20/#22? If not, what do we cut?
- [ ] Is the UI handoff (teal, shell) validated as the final DA?

## 9. Useful commands

```bash
git pull origin main
git log --oneline -12
gh issue list --repo Nitpick-org/nitpick-core --state open
./gradlew :desktopApp:run
GRADLE_USER_HOME=/tmp/nitpick-gradle ANDROID_USER_HOME=/tmp/nitpick-android \
  ./gradlew :shared:jvmTest :desktopApp:compileKotlin
```

The full `./scripts/check.sh` check requires the Android SDK; on a machine without the SDK,
validate at least `:shared:jvmTest` + `:desktopApp:compileKotlin` and note it.

## 10. Next action

- **H1**: install Air, launch the first Junie review + second agent, screenshot, `#39`.
- **H2**: worktree #19, then #20, then #22; Air log and human review.
- **H3**: worktree #21, then #23; Air log and Android screenshot.
- **All**: 17:30 sync with an Air screenshot and the status of the current issue.
