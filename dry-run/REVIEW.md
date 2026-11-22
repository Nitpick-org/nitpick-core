# Base review — <your first name>

> To fill in after the dry run. **20 minutes max.** Be direct, give evidence, do not
> rewrite the docs: propose. The goal is to fix the base before H0, not to choose the idea.
>
> If you have 5 minutes: sections **1**, **6** and **8**. If you have 20 minutes: everything.

## How to fill it in

1. **Verdict** (2 min): score, GO/NO-GO, top 3.
2. **Evidence** (12 min): time, bugs, agents, docs.
3. **Recommendations** (6 min): P0 / P1 / P2.

Rules:

- Hard on the substance, not on the people.
- One claim = one piece of evidence: command, error message, timestamp, screenshot.
- Distinguish **bug**, **friction**, **preference**.
- If you did not test it, write "not tested". Do not guess.
- No product idea here: the review covers the base, the docs, the agents and the pace.

---

## 0. Context

| Field | Value |
| --- | --- |
| Reviewed by | <first name> |
| Date | |
| Commit tested | `...` |
| OS / Node / pnpm / Claude Code | |
| Plan | Ultra / Pro / other |
| Total time spent | |
| Dry run done in | disposable clone / other |
| Branch | `dry-run/base` |
| Final `./scripts/check.sh` | ✅ / ❌ |
| Final `./scripts/check.sh --demo` | ✅ / ❌ |
| 3-min demo held | ✅ / ❌ |

## 1. Verdict in 30 seconds

| Dimension | Weight | Score /5 | Points |
| --- | --- | --- | --- |
| Onboarding | 4 | | |
| Dry run | 5 | | |
| Docs | 3 | | |
| Agents / models | 4 | | |
| Confidence for H0 | 4 | | |
| **Total** | **20** | | **/20** |

- **GO / NO-GO for H0**:
- **The 3 things to fix before H0**:
  1.
  2.
  3.
- **The thing that would save you the most time**:
- **The most solid thing in the base**:

## 2. Onboarding

| Step | Time | Status | Blocker / comment |
| --- | --- | --- | --- |
| Invitation + clone | | ✅ / ⚠️ / ❌ | |
| Hooks + `.env` | | | |
| Claude / Ultra login | | | |
| Skills + `/setup-matt-pocock-skills` | | | |
| `doctor.sh` | | | |
| First agent launched | | | |
| First worktree | | | |
| First green `check.sh` | | | |

- Time to the first green `check.sh`:
- What was clear:
- What was ambiguous:
- What was missing:
- **Concrete suggestion**:

### Weak signals

- I hesitated to:
- I assumed that:
- I did not understand:
- I searched without finding:

## 3. Dry run

| Metric | Value |
| --- | --- |
| Time to the first green `check.sh` | |
| Time to the first agent commit | |
| Time to the first merge | |
| Time to the first demo | |
| Tickets completed | /3 |
| Merge conflicts | |
| `check.sh` green on the first try | ✅ / ❌ |
| `check.sh --demo` green | ✅ / ❌ |
| 3-min demo | ✅ / ❌ |
| Number of agents used | |

- What broke:
- What was slower than expected:
- What was faster than expected:
- What you had to do by hand when it was supposed to be automatic:
- **The moment you almost gave up**:

### Top 3 time sinks

| # | Moment | Minutes lost | Cause | Fix |
| --- | --- | --- | --- | --- |
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |

## 4. Docs and repo

| Dimension | Score /5 | Comment |
| --- | --- | --- |
| Clarity | | |
| Accuracy | | |
| Completeness | | |
| FR / EN consistency | | |
| Length / navigability | | |

- Most useful doc:
- Most confusing doc:
- Dead link / wrong command / contradiction:
- What can be deleted:
- What must be added:
- Would a new collaborator understand the repo in < 30 min? ✅ / ❌

## 5. Agents and models

| Agent | Tested | Model | Verdict | Problem / setting |
| --- | --- | --- | --- | --- |
| `researcher` | | | | |
| `devils-advocate` | | | | |
| `architect` | | | | |
| `implementer` | | | | |
| `reviewer` | | | | |
| `qa-demo` | | | | |
| `pitch` | | | | |
| `scribe` | | | | |

- `/route-task` used? ✅ / ❌ — useful?
- `scribe` used for the docs? ✅ / ❌ — quality?
- Escalation triggered? ✅ / ❌ — how many, why?
- Most useful model:
- Most oversized model:
- `/usage` before / after (or % consumed):
- Where the tokens burned:
- Agent briefs: clear enough? ✅ / ❌
- `Stop` / `PreToolUse` hooks: useful? false alarms?
- What is missing in the agents:
- What can be deleted:

## 6. Bugs and frictions

| # | Severity | Where | Repro | Expected | Got | Proposed fix |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | P0 / P1 / P2 | | | | | |
| 2 | | | | | | |
| 3 | | | | | | |

> P0 = blocks the demo or onboarding. P1 = really slows down. P2 = polish.

## 7. Risks for the real hackathon

| Risk | Probability | Impact | Mitigation |
| --- | --- | --- | --- |
| Demo | | | |
| Timing | | | |
| Coordination of 3 humans | | | |
| Ultra quota | | | |
| Stack / deployment | | | |

## 8. Recommendations

### To do before H0 (P0)

1.
2.
3.

### Quick wins (P1)

1.
2.

### To ignore / later (P2)

1.
2.

## 9. Questions / decisions

| Question | Options | Your recommendation |
| --- | --- | --- |
| | | |
| | | |

## 10. Evidence

- Commands run:
- Logs / screenshots:
- Branch / commit:
- Time per step:
- What you did not test:

---

## The message to send back

```text
Review by <first name> — commit <sha>

Score: <x>/20 — GO / NO-GO

Top 3 to fix:
1.
2.
3.

Biggest time saver:
- ...

P0 bugs:
- ...

What the agents lack:
- ...

What I did not test:
- ...
```
