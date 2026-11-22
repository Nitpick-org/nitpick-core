# Audit 2 — sharing, dry run and Ultra capacity

> Second review, on 16 November 2026, after adding `docs/ULTRA.md`, the dry run and the
> starter. Objective: verify that the repo can be shared with the other 2, tested in a
> real situation, and that the agents are configured for 2 Ultra accounts.

## Verdict

The base is now **shareable and testable**: onboarding, dry run, runnable starter,
Ultra capacity scripts, API key guardrails. The dry run was validated locally
(install, typecheck, 6 tests, build, 2 e2e tests).

Two things remain **not verifiable from here**: the GitHub settings (collaborators,
branch protection, Actions) and the real behaviour of the Claude Code agents. The dry run
is precisely there for that.

## What has been fixed since audit 1

| # | Finding | Fix |
| --- | --- | --- |
| 1 | Impossible to share the repo cleanly | `docs/ONBOARDING.md`: access, clone, hooks, `.env`, Ultra, skills, `doctor.sh`, troubleshooting |
| 2 | No "real situation" test | `docs/DRY-RUN.md` + Vite/React starter + `dry-run/TICKETS.md` + `dry-run/RETRO.md` |
| 3 | The starter was not runnable | `scripts/bootstrap-dry-run.sh`: throwaway branch, copy, install, commit |
| 4 | `new-worktree.sh` did not know how to start from a dry run branch | `WT_FROM` added (`WT_FROM=dry-run/base`) |
| 5 | The `implementer` model was described as "often Sonnet" | Fixed: Sonnet by default, Opus via explicit override |
| 6 | Ultra sessions were not integrated into concurrency | `Total Ultra` table in `AGENT-OPS` (8 sessions, 3 writers) |
| 7 | Roster not optimized for Ultra | "Roster optimization for Ultra" section: more readers, never more writers |
| 8 | `doctor.sh` did not check onboarding/dry run | Added the docs and the starter |
| 9 | README without a sharing section | "Share with the other 2" section + links |
| 10 | J-5 checklist said "public repo" and "3 members" | Fixed: private repo, 2 invitations, public before submission |
| 11 | No model routing: the doc could start on Opus | `docs/MODEL-ROUTING.md`, skill `/route-task`, `scribe` agent (Haiku docs) |

## Findings still open

| # | Priority | Finding | Action | Owner |
| --- | --- | --- | --- | --- |
| 1 | P0 | No `package.json` at the root: `check.sh` and CI fail | Choose the stack, bootstrap, deploy | H2, J-4 |
| 2 | P0 | CI red as long as `package.json` is missing, so `main` is blocked for non-admins | Choose the stack and make `check` pass | H2, J-4 |
| 3 | P0 | No deployment/preview | Deploy the hello world | H2, J-4 |
| 4 | P1 | Project board not created yet | Create it via the web UI (or `gh auth refresh -s project`) | H1 |
| 5 | P1 | Labels and milestone created, not used yet | Open the first tickets at H0 | H1 |
| 6 | P1 | The dry run has not yet been played by the 3 humans | Schedule it at J-3 and fill `dry-run/RETRO.md` | all |
| 7 | P1 | No real test of the 8 agents | The dry run is the test; note the friction | all |
| 8 | P2 | The starter uses `lint = tsc --noEmit` | Acceptable for the dry run; the real stack will have a real linter | H2 |
| 9 | P2 | The repo will have to be public for the submission | Make it public at J-1, or provide a public link | H2 |

### GitHub settings applied (16/11)

- Collaborators: `pepito2t`, `dovatgabriel` (push).
- `main` **without protection** since 21/11 (at the team's request): direct merge
  allowed. PR and `check` remain recommended, but no longer block.
- Merge: squash + merge commit, rebase disabled, auto-merge, delete branch after merge.
- Features: issues/projects on, wiki/discussions off, description and topics.
- Labels: `ticket`, `bug`, `demo`, `P0`, `P1`, `P2`, `blocked`, `review`.
- Milestone: `LauzHack 2026` (22/11).
- Dependabot alerts: enabled.
- Project board: not created yet (`project` scope missing from the `gh` token).

## Agents audit (Ultra)

### What is good

- 8 agents, a clear role, no duplicate; `scribe` (Haiku) takes the docs off the expensive models.
- `maxTurns` on each agent, restrictive tools for readers, `disallowedTools` on
  `reviewer`.
- Routed model: Opus for decisions, Sonnet for volume, Haiku for docs,
  `implementer` in Sonnet, Opus via explicit override.
- `Stop` and `PreToolUse` hooks: no "done" without `check.sh`, no editing of the pivot
  docs by a sub-agent.
- `ANTHROPIC_API_KEY` guardrail in `doctor.sh`, `ralph-once.sh`, `afk-ralph.sh`.
- AFK bounded by `--max-turns` and `--max-budget-usd`, with validated environment variables.
- Documented routing (`docs/MODEL-ROUTING.md`) + skill `/route-task`: escalation after
  2 failures, manual Fable, never Opus for a README.

### What not to do

- **Add code-writing agents.** The bottleneck is the human review; 8 sessions,
  3 code writers max. `scribe` is docs-only and does not count in it. Ultra gives more
  readers, not more mergers.
- **Enable agent teams.** Experimental, no worktree isolation per teammate.
- **Set Fable by default.** Separate limit, fast consumption, reserved for high-stakes
  decisions.
- **Use `ANTHROPIC_API_KEY`.** In `-p`, Claude Code takes it systematically; the
  scripts refuse, by design.
- **Create a homemade `scout` agent.** The built-in Explore/Plan agents already do the
  read-only reconnaissance.

### Recommended optimizations

1. **Two readers in parallel per Ultra human** at kickoff: `researcher` (competitors,
   API) + Explore/Plan (codebase). H1 stays on the PRD.
2. **Continuous QA** on the H3 account: `qa-demo` after each merge, not only at H18.
3. **Preloaded skills** (`tdd`, `code-review`, `research`, `codebase-design`) only
   after installation and testing; do not hardcode them before.
4. **Background subagents** for long QA/research if the version supports them;
   never for writing.
5. **One AFK per Ultra account**, on its owner's shift, with a low-risk ticket
   and a deterministic oracle.
6. **Route before delegating**: `scribe`/Haiku for docs, Sonnet for the standard,
   Opus for the critical; escalate after 2 failures, never Fable automatically.

## Dry run audit

### Validated locally

- `pnpm install`: OK.
- `pnpm typecheck`: OK.
- `pnpm test`: 6 tests, OK.
- `pnpm build`: OK.
- `pnpm e2e`: 2 tests, OK (Chromium installed in a local cache for the test).
- The sample does produce **2 anomalies**; the invalid CSV shows an error.

### Known limits

- The starter is a **throwaway Vite/React**, not the hackathon stack.
- `lint = tsc --noEmit`: this is not a real linter, it is a placeholder for the test.
- `pnpm` may show `Ignored build scripts: esbuild`; `pnpm rebuild esbuild` is enough.
- Playwright must install Chromium once (`pnpm exec playwright install chromium`).
- The dry run tests neither deployment, nor the DB, nor the sponsor APIs, nor the night.

### What the dry run must produce

- A filled `dry-run/RETRO.md`.
- 3 concrete frictions, with a fix and an owner.
- 3 tickets in the real repo, fixed before H0.
- A 3-minute demo that fits in 2:45.

## The 5 remaining actions

1. **Share**: invite the other 2, protect `main`, check the Actions.
2. **Stack**: choose, bootstrap, deploy, `check.sh` + CI green.
3. **Dry run**: play it with 3, fill the retro, fix the 3 frictions.
4. **Agents**: test the 8 agents for real, check the hooks and the
   `ANTHROPIC_API_KEY` refusal.
5. **Event**: keys, receipts, public repo, `doctor.sh` with no failure, sleep.

## Review method

- Full reading of the docs and scripts.
- Running `doctor.sh`, `check.sh`, the worktree scripts and the API key guardrails.
- Installing and testing the starter in a temporary folder (typecheck, tests, build, e2e).
- Checking the Ultra facts against the official pages (plan, API credits, Fable,
  Claude Code environment variables).
- Limits: no access to the GitHub settings (private repo, `gh` not authenticated), no
  real Claude Code session for the 8 agents; the dry run covers this point.
