# Model routing — spending tokens in the right place

> Goal: over 24 h, with 2 Ultra accounts, use **the cheapest model that can
> finish the task**. We escalate only on evidence. We never lower
> quality on a high-stakes decision.

## The 4 tiers

| Tier | Model | For | Avoid |
| --- | --- | --- | --- |
| **T0** | Haiku | docs, progress, summaries, changelog, test data, commit messages | architecture, complex code, pitch |
| **T1** | Sonnet | standard code, tests, UI, research, QA, nuanced docs | risky refactor, auth/security, final review |
| **T2** | Opus | architecture, review, critical code, pitch, devil's advocate, blocking debug | docs-only, formatting, renaming |
| **T3** | Fable | **manual only**: idea, modules, final pitch, bug > 30 min | everything else, and never in AFK |

> Fable has a separate weekly limit and consumes faster. It must never be
> used by an agent automatically.

## Routing table

| Task | Agent | Model | maxTurns |
| --- | --- | --- | --- |
| Factual doc, README, progress, summary | `scribe` | Haiku | 10–15 |
| Nuanced public doc (onboarding, guide) | `scribe` | Sonnet | 15–20 |
| Small code, test, simple UI | `implementer` | Sonnet | 20–30 |
| Complex/cross-module code, auth/security, or demo-critical with risk ≥ medium | `implementer` | Opus | 40–50 |
| Standard research | `researcher` | Sonnet | 20–30 |
| Ambiguous / structural research | `researcher` | Opus | 30 |
| Code review | `reviewer` | Opus | 25 |
| Docs-only review | `scribe` / human | Haiku | 10 |
| Standard QA | `qa-demo` | Sonnet | 25–30 |
| Blocking / flaky bug | `qa-demo` | Opus | 30 |
| Architecture / tickets | `architect` | Opus | 20 |
| Challenge the PRD | `devils-advocate` | Opus | 20 |
| Pitch / PRD | `pitch` (H1 decides) | Opus | 20 |

## Escalation: when to go up

One tier up if:

- 2 failed attempts on the same point;
- `./scripts/check.sh` red after 2 fixes;
- ticket `demo-critical` **and** risk `medium`/`high` (a demo-critical ticket at low risk stays Sonnet);
- cross-module change, auth, security, data, API contract;
- the agent says "blocked" or "uncertain" twice.

**Haiku → Sonnet**: the doc becomes a public guide, or the summary must be exact.
**Sonnet → Opus**: the code touches several modules, the demo, auth, or the tests
fail without explanation.
**Opus → Fable**: never automatic. A human decides, outside AFK.

## De-escalation: when to go down

- docs-only, formatting, renaming, test data, summary;
- diff < 50 lines, a single file, already covered by a test;
- repetitive task (10th ticket of the same type) with a known pattern.

## How to override in Claude Code

The model is chosen **at the moment of invoking the agent**, not in its prompt:

```text
Use implementer with the sonnet model for ticket #12.
Use scribe to update README.md.
Use researcher with the opus model to settle the sponsor API.
Use reviewer on the diff.
```

- `haiku`, `sonnet`, `opus`, `fable` are accepted aliases.
- The agent's default model stays in its frontmatter; the one-off override does not
  change it.
- An agent **cannot change its own model mid-run**. If it asks for an
  escalation, the orchestrator relaunches it with the higher model (new invocation,
  same ticket).

## Indicative budgets

| Tier | Tokens per task | Warning signal |
| --- | --- | --- |
| T0 | < 20 k | a doc that takes 50 k = bad prompt |
| T1 | < 100 k | 2 failures = escalation, not 5 tries |
| T2 | < 250 k | beyond that, split the ticket |
| T3 | manual | never in AFK |

The weekly budget of the Ultra accounts is in `docs/ULTRA.md`. Check `/usage` at
every sync.

### AFK

`ralph-once.sh` and `afk-ralph.sh` force an explicit model: `RALPH_MODEL` /
`AFK_MODEL`, default **Sonnet**. Fable is refused. Without this guardrail, a `claude -p` without
`--model` takes the machine's default model — and an AFK can burn the budget in
a few seconds.

## Logging

In `progress/<ticket>.md`:

```md
- Model: sonnet
- Escalations: sonnet → opus (2 failures on CSV parsing)
- Why: the ticket is demo-critical and the e2e test fails
```

At the retro, if a doc consumed Opus, that is a routing bug.

## Automation

- **`/route-task` skill**: classifies the task and returns a decision
  `agent / model / maxTurns / escalade`. Claude can invoke it alone when the description
  matches.
- **CLAUDE.md**: rule "classify before delegating, start low, escalate on evidence".
- **`scribe`**: docs-only agent on Haiku. It is the default for README, progress, summaries.
- **`Explore` / `Plan`**: read-only codebase reconnaissance, not a custom agent.
- **`Stop` + `check.sh`**: prevent declaring "done" too early.

## Examples

| Request | Route |
| --- | --- |
| "Update the README with the dry run" | `scribe` / Haiku / 10 |
| "Rewrite the onboarding so that it is crystal clear" | `scribe` / Sonnet / 20 |
| "Add the CSV parser + tests" | `implementer` / Sonnet / 25; Opus if 2 failures |
| "Why is the e2e test flaky?" | `qa-demo` / Sonnet / 20; Opus if not reproducible |
| "Review the auth PR" | `reviewer` / Opus / 25 |
| "Find the pricing of the sponsor API" | `researcher` / Sonnet / 20 |
| "Split the PRD into modules" | `architect` / Opus / 20 |
| "Write the 2:45 pitch" | `pitch` / Opus / 20; manual Fable if blocked |

## Anti-patterns

| Anti-pattern | Cost | Fix |
| --- | --- | --- |
| Opus for a README | 5–10× the cost | `scribe` / Haiku |
| Sonnet for a security review | risk | `reviewer` / Opus |
| Fable by default | separate limit drained | manual, key decisions |
| 5 Sonnet tries instead of an escalation | time + tokens | 2 failures → Opus |
| Re-reading the whole repo at every turn | saturated context | target the ticket + `progress/` |
| Sub-agent for a product decision | drift | human |
| Agent that assigns itself Opus | no control | orchestrator decides |

## Sources

- [Claude Code — subagents](https://code.claude.com/docs/en/sub-agents): model per agent, `model: inherit`, override per invocation.
- [Claude Code — models](https://code.claude.com/docs/en/model-config): aliases and effort.
- [Claude Code — costs](https://code.claude.com/docs/en/costs): multi-agent ≈ 15× a chat.
- [AI Hero — Ralph](https://www.aihero.dev/tips-for-ai-coding-with-ralph-wiggum): small steps, feedback loops.
