---
name: route-task
description: Picks the cheapest agent and model for a task, with escalation on evidence. Use before delegating to a subagent, when a task is stuck, or to settle between Haiku, Sonnet, Opus and Fable.
---
# Routing a task

You receive a task (ticket, doc, bug, research, review, QA). You pick **the cheapest
model that can finish it**, not the most powerful one.

## Expected output

```text
Route: <agent> / <model> / maxTurns <n>
Why: <1 sentence>
Escalate if: <2 concrete conditions>
Budget: <tokens or $ indicative>
```

## Routing table

| Task | Agent | Model | maxTurns |
| --- | --- | --- | --- |
| Doc, README, progress, summary, changelog | `scribe` | `haiku` | 10–15 |
| Small code, test, simple UI, local fix | `implementer` | `sonnet` | 20–30 |
| Complex/cross-module code, auth/security, or demo-critical **with risk ≥ medium** | `implementer` | `opus` | 40–50 |
| Standard research (API, pricing, competitor) | `researcher` | `sonnet` | 20–30 |
| Ambiguous research, structural decision | `researcher` | `opus` | 30 |
| Code review | `reviewer` | `opus` | 25 |
| Docs-only review / trivial diff | `scribe` or human | `haiku` | 10 |
| Standard QA, demo walkthrough | `qa-demo` | `sonnet` | 25–30 |
| Blocking bug, not reproducible, flaky | `qa-demo` | `opus` | 30 |
| Architecture, interfaces, tickets | `architect` | `opus` | 20 |
| Challenge of the PRD / the plan | `devils-advocate` | `opus` | 20 |
| PRD, pitch, submission README | `pitch` (H1 decides) | `opus` | 20 |
| Very high-stakes decision (idea, modules, final pitch) | human | manual `fable` | — |

## Escalation

Escalate one tier (Haiku → Sonnet → Opus) if:

- 2 attempts failed on the same point;
- `./scripts/check.sh` red after 2 fixes;
- `demo-critical` ticket **and** `medium`/`high` risk (a demo-critical ticket at low risk stays Sonnet);
- cross-module change, auth, security, data, API contract;
- the agent explicitly says "blocked" or "uncertain" twice.

Log in `progress/<ticket>.md`: `Model:`, `Escalations:`, `Why:`.

## De-escalation

Go back down if:

- the task is docs-only, formatting, a rename, test data, a summary;
- the diff is < 50 lines, a single file, already covered by a test.

## Hard rules

- **Fable is never automatic.** A human picks it, outside AFK.
- **An agent does not change its own model mid-run.** The orchestrator
  relaunches it with a higher model.
- **No subagent for a product decision.** Ideas come from humans.
- **No re-reading of the whole repo.** Target the ticket's files and `progress/<ticket>.md`.
- **No infinite loop.** If the budget is exceeded, stop and ask.
