# Dry run — test the base under real conditions

> **Historical doc** (see `docs/README.md`): the TypeScript starter, `src/`, `tests/` and
> `scripts/bootstrap-dry-run.sh` were removed after the freeze. The protocol stays here as a
> trace of the rehearsal; the commands that name them are no longer usable.

> A fictional mini-project, **2 h 30**, in a **throwaway clone**. The goal is not to ship
> Pulse: it is to discover what breaks in the repo, the agents and the pace before the
> real hackathon.

## What it tests

- Onboarding: clone, hooks, `.env`, Claude Code, Ultra, skills.
- The kickoff: DEMO, PRD, modules, tickets.
- Worktrees, `check.sh`, `check.sh --demo`, the agents, the review, the merge.
- The 3-minute demo and the Q&A.
- Ultra capacity: 2 accounts, 8 sessions max, 3 writers.

## What it does not test

- The real stack (the starter is a throwaway Vite + React).
- Deployment, the database, the sponsor APIs.
- The night, sleep, the full 24 h.

## Rules

1. **Throwaway clone**: `git clone <repo> /tmp/lauzhack-dry-run`. Do not do this in the shared
   repo.
2. **No real secret**: no API key, no personal data.
3. **Never merge `dry-run/base` into `main`.**
4. **Timebox**: 2 h 30. If a step overruns, note it and move to the next one.
5. **Note the friction**: each blockage of more than 5 minutes goes into `dry-run/REVIEW.md`
   (bugs section) and into `dry-run/RETRO.md` (internal synthesis).

## Setup (10 minutes)

```bash
git clone git@github.com:zooldyyk/hackathon.git /tmp/lauzhack-dry-run
cd /tmp/lauzhack-dry-run
git config core.hooksPath .githooks
cp .env.example .env
unset ANTHROPIC_API_KEY

./scripts/bootstrap-dry-run.sh
pnpm exec playwright install chromium
./scripts/check.sh
./scripts/check.sh --demo
```

- `bootstrap-dry-run.sh` creates `dry-run/base`, copies the starter, installs the dependencies and
  commits.
- `playwright install chromium` is required for `--demo` (once).
- If `check.sh --demo` fails, note it: it may be a real bug in the base.
- The starter installs with `--ignore-scripts` (known, throwaway). If you still see an
  `Ignored build scripts: esbuild` warning, `pnpm rebuild esbuild` is enough; the build passes.
- If the Playwright browser installation fails (read-only cache), use
  `PLAYWRIGHT_BROWSERS_PATH="$PWD/.playwright-browsers" pnpm exec playwright install chromium`
  then the same variable before `./scripts/check.sh --demo`.

## Roles

| Human | Role | Agents | Account |
| --- | --- | --- | --- |
| H1 | Product & Pitch: DEMO, PRD, script, Q&A | `devils-advocate`, `pitch` | Pro / other |
| H2 | Architect & Integration: modules, merge, CI | `architect`, `reviewer`, `implementer` | Ultra |
| H3 | Builder & Demo: UI, e2e, demo | `implementer`, `qa-demo` | Ultra |

## Timeline

| Time | Who | Action | Gate |
| --- | --- | --- | --- |
| 0:00–0:10 | all | Onboarding: clone, hooks, `.env`, `doctor.sh` | A single failure: `no package.json` before bootstrap |
| 0:10–0:20 | all | `bootstrap-dry-run.sh`, `check.sh`, `check.sh --demo` | Check green |
| 0:20–0:35 | H1 | Read `dry-run/TICKETS.md`, write `docs/DEMO.md` (Pulse) and `docs/PRD.md` | Demo in 5 points |
| 0:20–0:35 | H2/H3 | Read the architecture, create the worktrees | 2 worktrees ready |
| 0:35–0:50 | H2 | `architect` on the PRD, then `implementer` ticket #01 | Module + tests |
| 0:35–0:50 | H3 | `qa-demo` on the baseline, prepare the e2e test | The demo journey is clear |
| 0:50–1:35 | H2/H3 | Ticket #02 UI, then #03 e2e/demo; `reviewer` on each diff | 3 tickets ready |
| 1:35–1:55 | H2 | Merge into `dry-run/base`, `check.sh --demo` | All green |
| 1:55–2:10 | all | Demo 3 min ×2 + Q&A 1 min | The demo fits in 2:45 |
| 2:10–2:30 | all | Fill in `dry-run/REVIEW.md` (15–20 min) + `dry-run/RETRO.md` (5 min), go/no-go | Complete review + 3 fixes |

## Useful commands

```bash
# Worktree for one ticket, based on the dry-run branch
WT_BASE="$PWD/.worktrees" WT_FROM=dry-run/base ./scripts/new-worktree.sh 01-anomaly
cd .worktrees/wt-01-anomaly
pnpm install --ignore-scripts
claude   # accept the trust dialog once, then quit for AFK

# Check the diff against the dry-run base
git diff dry-run/base...HEAD

# After merging in the main clone
./scripts/check.sh --demo

# Nettoyage
WT_BASE="$PWD/.worktrees" ./scripts/rm-worktree.sh 01-anomaly --force
git switch main
git branch -D dry-run/base
rm -rf .worktrees
```

## What we observe

| Question | Why |
| --- | --- |
| How long until the first green `check.sh`? | Is the base really ready? |
| Is a fresh agent productive in < 5 min? | Are the brief and the docs enough? |
| How many merge conflicts? | Are the file zones respected? |
| Did an agent wrongly declare "done"? | Do the `Stop` hook and the tests work? |
| Does the reviewer find a real problem? | Is the review useful or theatrical? |
| Does the demo fit in 2:45 without slides? | Is the format understood? |
| Where are the 10 lost minutes? | That is the real gain to fix. |

## Routing (to test)

Before each delegation, use `/route-task` and note the decision:

- doc → `scribe` / Haiku (Sonnet if the text needs nuance);
- standard code → `implementer` / Sonnet;
- critical code → `implementer` / Opus;
- code review → `reviewer` / Opus;
- pitch/PRD → `pitch` / Opus.

If a ticket fails 2 times, rerun the **same agent** with the higher model and note
`Escalations:` in `progress/<ticket>.md`. Fable stays manual.

## Ultra

- H2 and H3 work on the Ultra accounts.
- **8 sessions max, 3 writers max.** The others are readers/QA.
- `/usage` at the start and at the end. Note the percentage consumed.
- **No Fable** for this dry run, unless blocked > 30 min. If you use it, note it.
- If you have time, test **one** AFK of one iteration on a low-risk ticket:
  `AFK_OK=1 ./scripts/afk-ralph.sh 1` in a worktree.

## Success criteria

- [ ] `./scripts/check.sh` green at the root and in the worktrees.
- [ ] `./scripts/check.sh --demo` green.
- [ ] The 3 tickets are merged into `dry-run/base`.
- [ ] At least 4 agents used (`architect`, `implementer`, `reviewer`, `qa-demo`).
- [ ] A doc was written by `scribe` (Haiku), not by Opus.
- [ ] An escalation was logged if a ticket failed 2 times.
- [ ] The 3-minute demo passes 2 times, without slides.
- [ ] `dry-run/REVIEW.md` is filled in (the full feedback to send).
- [ ] `dry-run/RETRO.md` contains 3 concrete fixes (internal retro).
- [ ] No secret was committed.

## After the dry run

1. Fill in `dry-run/REVIEW.md` (full feedback, 20 min) and send it.
2. Fill in `dry-run/RETRO.md` while it is hot (internal retro, 5 min).
3. Create 3 tickets in the real repo (GitHub Issues, label `ticket`) for the 3 fixes.
4. Fix them before H0, with `check.sh` green.
5. Delete the throwaway clone.

> If the dry run fails, it is a success: you just found a problem before the
> hackathon, not during the demo.
