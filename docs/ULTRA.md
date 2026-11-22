# Ultra — making the most of the 2 Claude Ultra subscriptions

> Two team members have a **Claude Ultra** subscription. This document explains how to
> turn that capacity into useful work **without** breaking the rules of `AGENT-OPS.md`:
> Ultra lifts the token constraint, not the **human review** constraint.

## The facts (verified on 16/11/2026)

| Fact | Consequence for us |
| --- | --- |
| Ultra = **10× Pro's weekly usage**, **$200/month**. | Ultra accounts carry the heavy squads. |
| A **verified** account = **no 5-hour limit**. An unverified account has a temporary one. | Verify **before** D-1 (see below), otherwise we get stuck in the middle of the night. |
| A **weekly limit** covers all models, with a **separate limit for Fable**. Fable can use up to **50%** of the weekly limit and burns through it faster. | Fable is reserved for high-stakes decisions (see "Models"). |
| Claude Code **shares** its limits with claude.ai. | No long discussions on claude.ai during the 24 h: they eat the coding budget. |
| Limits reset **once a week** (date visible in Settings > Usage). | Reach H0 with maximum headroom; a reset during the hack is a bonus. |
| If `ANTHROPIC_API_KEY` is set in the shell, Claude Code uses **the API key** instead of the subscription. In `-p` mode (AFK/Ralph), it is used **systematically** if it exists. | API billing by mistake. The variable is renamed `APP_ANTHROPIC_API_KEY`; `doctor.sh`, `ralph-once.sh` and `afk-ralph.sh` refuse to run if it is present. |
| Ultra includes **$200/month of API credits** (Console). | Use them for the **application's** AI calls (key in `.env` as `APP_ANTHROPIC_API_KEY`) or for the Agent SDK with an API key, **not** for interactive Claude Code. |

## Who drives what

| Human | Plan | Role (`AGENT-OPS.md`) | Agent load |
| --- | --- | --- | --- |
| **H1** | Pro / other | Product & Pitch | 1 main session + 1 scout. Few tokens, many decisions. |
| **H2** | **Ultra** | Architect & Integration | 1 main + 2 parallel (builder in a worktree + `reviewer`). AFK during their sleep shift. |
| **H3** | **Ultra** | Builder & Demo | 1 main + 2–3 parallel (builders in worktrees + `qa-demo`). AFK during their sleep shift. |

> Assumption: H1 is not on Ultra. If that is wrong, rebalance: H1 takes the
> `researcher` / `devils-advocate` in parallel plus one night AFK.

**Golden rule:** **token-hungry** tasks (broad research, big refactor, AFK,
repeated QA) go to an Ultra account. **Judgment-hungry** tasks (PRD, scope,
pitch) stay with the human concerned, whatever their plan.

The agent-by-agent split is in `docs/AGENT-OPS.md` → **Roster optimization for
Ultra**: more readers, never more than one writer per worktree.

## Concurrency: what changes, what does not

- **Does not change:** a human seriously reviews **one** significant diff at a time.
  The writer cap stays that of `AGENT-OPS.md`.
- **Changes:** we can add **readers** (read-only scouts, QA, research) without fearing
  the cost. The total cap goes from 6 to **8 sessions**, at most 3 of them writers.

| Phase | Writers | Readers / QA | Total max | On Ultra |
| --- | --- | --- | --- | --- |
| Kickoff | 1 | 3 | 4 | `researcher` ×2 in parallel (competitors, API) |
| Skeleton | 2 | 2 | 4 | both builders |
| Core | 3 | 4–5 | 8 | builders + `reviewer` + `qa-demo` continuously |
| Night | AFK ×2 | 0–1 | 3 | 1 AFK per Ultra account, during their sleep shift |
| Hardening | 1–2 | 3 | 5 | `qa-demo` in a loop over `DEMO.md` |
| Freeze | 0 | 1–2 | 2 | `reviewer` + `qa-demo` |

**Agent teams:** still disabled. Ultra removes the cost argument, not the
isolation one (no worktree per teammate) nor the review one.

## Weekly budget (per Ultra account)

Goal: **never** hit the limit before Sunday 12:00. Read the % used with
`/usage` in Claude Code (or Settings > Usage) **at every 3 h sync**.

| Block | Hours | Max cumulative budget | If exceeded |
| --- | --- | --- | --- |
| Before H0 (D-5 → D-1) | — | **≤ 25%** of the week | No more agent tests until H0 |
| Kickoff + skeleton | 12:00–16:00 | ≤ 40% | Move the scouts to Sonnet |
| Core + 2nd feature | 16:00–23:00 | ≤ 65% | No more parallel scouts, 1 builder max |
| Night (AFK) | 00:00–07:00 | ≤ 80% | AFK limited to 2 iterations |
| Integration + freeze | 07:00–12:00 | ≤ 95% | Blocking fixes only |

If one Ultra account is nearly dry, the other takes over its tickets. If both are,
we switch **the sessions** to H1's account, never to an API key in the shell.

> After the weekly limit, Claude can continue on **paid usage** if that is enabled
> on the account. Decide on D-1 whether you allow it, and with what cap (see Q-004).

## Models

| Usage | Model | Why |
| --- | --- | --- |
| Main H2/H3 sessions | Opus | Ultra allows it; better code quality and fewer round trips |
| `researcher`, `qa-demo` | Sonnet | volume, reading, test execution |
| `scribe` | Haiku | docs, README, progress, summaries |
| `architect`, `devils-advocate`, `reviewer`, `pitch` | Opus | unchanged (see the agents) |
| **Fable** | by hand, via `/model` | **only**: idea choice at H0, module split, blocking bug > 30 min, final pitch review. Separate limit and fast consumption: never make it the default, never put it in an AFK. |

Full routing, escalation and budgets: `docs/MODEL-ROUTING.md` and the `/route-task` skill.

## Night AFK with Ultra

The account that launches `afk-ralph.sh` is **the one logged in on the machine** that launches it.

1. Before their sleep shift, the Ultra human picks 1 **low-risk** ticket with an oracle
   (`AGENT-OPS.md` → "When to use AFK").
2. `./scripts/new-worktree.sh <ticket>` then, in the worktree:
   `AFK_OK=1 ./scripts/afk-ralph.sh 6`.
3. Machine plugged in, sleep disabled (`caffeinate -dimsu` on macOS), stable Wi-Fi.
4. On waking: `git log`, `progress/<ticket>.md`, `check.sh --demo`, then a PR.

Default script caps (overridable): `AFK_MAX_TURNS=60`,
`AFK_BUDGET_USD=4`, `AFK_MODEL=sonnet`; `RALPH_MAX_TURNS=40`, `RALPH_BUDGET_USD=3`,
`RALPH_MODEL=sonnet` for `ralph-once.sh`. **Fable is refused** by both scripts: it must
never be the default model of an AFK. The $ budget is an **estimate** on the subscription side:
it is a guardrail against the runaway loop, not an invoice.

## To do before H0 (each Ultra account)

- [ ] **Verified account:** Settings > Usage. No "Current session" bar = verified.
- [ ] **Day and time of the weekly reset** noted in `docs/DECISIONS.md`.
- [ ] `/usage` on the evening of D-1: **≥ 75%** of the week available.
- [ ] `echo $ANTHROPIC_API_KEY` empty in the shell (and not in `~/.zshrc`).
- [ ] `claude` logged in with the Ultra account (`/status`).
- [ ] Decision on paid usage after the limit (yes/no, cap).
- [ ] Ultra API credits: create a key for the app if we need one, put it in `.env`
      as `APP_ANTHROPIC_API_KEY`.

## Sources

- [Claude Help Center — Ultra plan](https://support.claude.com/en/articles/17728952)
- [Claude Help Center — monthly API credits](https://support.claude.com/en/articles/17154008-monthly-api-credits-for-max-ultra-and-team-plans)
- [Claude Help Center — Fable on your plan](https://support.claude.com/en/articles/15424964-claude-fable-models-on-your-plan)
- [Claude Help Center — Claude Code with a plan](https://support.claude.com/en/articles/11145838-use-claude-code-with-your-pro-max-or-ultra-plan)
- [Claude Monitor — Ultra limits](https://claude-monitor.com/claude-ultra-plan-usage-limits)
- [Claude Code — environment variables](https://code.claude.com/docs/en/env-vars)
- [Claude Code — costs](https://code.claude.com/docs/en/costs)
