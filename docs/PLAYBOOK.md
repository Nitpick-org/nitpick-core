# Playbook — 24 h with 3 humans + agents

> The operational runbook. The event facts are in `docs/LAUZHACK.md`,
> the strategy in `docs/STRATEGY.md`, the agents in `docs/AGENT-OPS.md`.

## The roles

| Role | Owns | Its agents | Decides alone on |
| --- | --- | --- | --- |
| **H1 — Product & Pitch** | PRD, DEMO.md, scope, challenge fit, e2e test | `researcher`, `devils-advocate`, `pitch` | Scope, ticket order, narrative |
| **H2 — Architect & Integration** (**Ultra**) | Interfaces, `main`, merge queue, CI, stack, DECISIONS | `architect`, `reviewer`, `implementer` | Code structure, what gets merged |
| **H3 — Builder & Demo** (**Ultra**) | UI, risky integrations, deployment, mocks, video | `implementer`, `qa-demo` | Visual rendering, demo plan B |

Everyone codes and reviews. The roles say **who decides**. H2/H3 hold the Ultra
accounts: they take the token-hungry tasks (research, QA, AFK), not H1's
decisions. `scribe` (Haiku) is available to everyone for docs. Details: `docs/ULTRA.md` and
`docs/MODEL-ROUTING.md`.

## Before H0 (D-5 → D-1)

| Day | Goal | Gate |
| --- | --- | --- |
| D-5 (Mon) | Read `LAUZHACK.md` and `STRATEGY.md`, 60 min team call, roles, private repo + invites | Roles + repo shared |
| D-4 (Tue) | Prepare hosting (Vercel/Netlify) and the Vite fallback; stack at H0 | Hosting account ready |
| D-3 (Wed) | Install skills, test agents/worktree/AFK, run the **dry run** (`docs/DRY-RUN.md`) | 8 agents visible, dry run passed |
| D-2 (Thu) | Fix the 3 frictions from the dry run + 3 ideas per human | The loop runs with no blocking friction |
| D-1 (Fri) | Shortlist of 3 ideas, API keys, quotas, bags, sleep | `doctor.sh` with no failure |

Details: `docs/PRE-EVENT-CHECKLIST.md`.

## The 24 h runbook

| Time | Phase | Humans | Agents | Exit gate |
| --- | --- | --- | --- | --- |
| 8:00–9:30 | Check-in | ID, table, Wi-Fi, power sockets | — | Set up before 9:30 |
| 10:20–11:40 | Tech talks | H1 takes notes on the challenges; H2/H3 prepare the repo | `researcher` on watch | Challenges understood |
| 11:40–12:00 | Team building | Pick the idea, the target, the novelty sentence, **and the stack** (Vite fallback) | — | Idea + stack + challenge chosen |
| **12:00–13:00** | **Kickoff** | DEMO.md, PRD, modules, tickets | `architect` proposes, `devils-advocate` challenges | Demo written in 5 points, 8–12 tickets |
| 13:00–16:00 | Skeleton | 2–3 builders, worktrees | `implementer` | **End-to-end demo on the preview, even if ugly** |
| 16:00–19:00 | Core | The aha first | `implementer`, `qa-demo` | The aha is visible in < 30 s |
| 19:00–22:00 | Second feature | Depth or a 2nd use case | `implementer`, `reviewer` | Complete demo |
| 22:00–23:00 | v0.1 freeze | Tag, backup video, draft submission | `qa-demo` | v0.1 recorded |
| 23:00–07:00 | Night | Sleep in 4 h shifts, 2 awake | Bounded AFK, low-risk only | Nobody breaks `main` |
| 07:00–09:30 | Integration | Fix, merge, repeat | `qa-demo`, `reviewer` | e2e green |
| **09:30–10:30** | **Final freeze** | No more features, bugs only | — | Code frozen |
| 10:30–11:30 | Submission | Form, README, video | `pitch` | **Submitted before 12:00** |
| 11:30–12:00 | Rehearsal | 5 runs of 3 min, Q&A | — | Everyone ready |
| 12:30–15:00 | Judging | 3 min demo + 1 min Q&A | — | Stay until closing |

> A gate missed on time → we **cut scope**, we do not push the next one back.
> If the riskiest integration is not demonstrable at **19:00 Saturday**, we cut it.

## H0 kickoff — minute by minute

1. **12:00–12:10** — Roles, repo, board, channels. Check that the **chosen stack** is
   installed, that `main` is reachable and that the preview responds.
2. **12:10–12:40** — 3 ideas per human, 2 min each, scoring
   (`docs/STRATEGY.md`). Keep 2, decide.
3. **12:40–13:00** — Write `docs/DEMO.md` in 5 points. If it cannot be written,
   the idea is not ready: go back to idea #2.
4. **13:00–13:20** — `/to-spec` → `docs/PRD.md` (or write it directly), then
   `devils-advocate` on the PRD. Cut what it flags.
5. **13:20–13:40** — `architect` → modules + tickets. H2 validates, completes
   `ARCHITECTURE.md` and `CLAUDE.md` (commands, state).
6. **13:40** — First worktrees: `./scripts/new-worktree.sh <ticket>`, then `claude`.
   Supervised mode (`ralph-once.sh`) until the prompt is reliable.

## AI Hero skills

Install once per repo:

```bash
claude plugin install mattpocock-skills@claude-plugins-official
# or, for editable files:
npx skills@latest add mattpocock/skills
```

Then, in a session: `/setup-matt-pocock-skills` (once per repo, configures
the issue tracker, the labels and the docs folder).

| Moment | Skill | Use |
| --- | --- | --- |
| Idea | `/grill-with-docs` (or `/grill-me` if in a hurry) | Get the questions asked, write the glossary/ADR |
| Spec | `/to-spec` | Turn the conversation into a PRD |
| Tickets | `/to-tickets` | Split into slices with dependencies |
| Implementation | `/implement` + `/tdd` | TDD, one slice at a time |
| Review | `/code-review` | Standards + spec, in parallel |
| Debug | `/diagnosing-bugs` | Diagnosis loop |
| Retro | `/retro` | Improve the repo after a session |

Repo-local skill: **`/route-task`** (in `.claude/skills/`) — picks the agent and the
model (Haiku/Sonnet/Opus) before delegating, with escalation after 2 failures.

> The old names `/to-prd` and `/to-issues` are no longer the current names. Use
> `/to-spec` and `/to-tickets`.

### AI Hero mapping → this repo

| AI Hero phase | Artifact | Where |
| --- | --- | --- |
| 1. Idea | 3 ideas, scoring, novelty sentence | `docs/STRATEGY.md`, `docs/IDEAS.md` |
| 2. Research | sourced cache, API, pricing | `docs/RESEARCH.md`, `docs/RESEARCH-HACKATHON.md` |
| 3. Prototype | throwaway route to settle the UX | `src/app/prototype/` (to delete) |
| 4. PRD | destination, user stories, out of scope | `docs/PRD.md` |
| 5. Kanban | tickets, dependencies, demo-critical | GitHub Issues, `.github/ISSUE_TEMPLATE/ticket.md` |
| 6. Execution | worktrees, Ralph, check.sh | `scripts/`, `.claude/agents/` |
| 7. QA | e2e, demo QA, review | `docs/DEMO.md`, `scripts/demo-check.sh`, `qa-demo` |

The AI Hero principle that counts most here: **deep modules** (a lot of logic
behind a simple interface) and **tickets as vertical slices**. See
`docs/ARCHITECTURE.md`.

## Rituals

- **Sync every 3 h** (10 min, standing): merged / blocked / what we cut / `/usage`
  of the Ultra accounts. At 15:00, 18:00, 21:00, 00:00, 07:00.
- **Demo review at 16:00, 22:00 and 09:00**: we play the demo on the preview.
  Aim for **2:45**, not 3:00; record a 2 min backup video.
- **Before a nap** (5 min): state in `progress.txt`, then
  `AFK_OK=1 ./scripts/afk-ralph.sh 4`. One AFK per Ultra account, started by its owner.
- **Disagreement**: one sentence each, the domain owner decides,
  one line in `DECISIONS.md`.
- **After each agent correction**: one line in `CLAUDE.md` ("Lessons learned")
  or `/retro` if the problem is structural.

## Sleep

3 people, **4 h of sleep each**, always 2 awake. No all-nighter: the
final demo is won on clear-headedness, not on heroism.

| Slot | Asleep | Awake |
| --- | --- | --- |
| 22:00–02:00 | H3 | H1, H2 |
| 02:00–06:00 | H2 | H1, H3 |
| 06:00–10:00 | H1 | H2, H3 |

- H3 sleeps during the v0.1 freeze: H1/H2 record the backup video.
- H2 sleeps 02:00–06:00: H1/H3 do the night integration; H2 takes the merge back at 06:00.
- H1 sleeps 06:00–10:00: H2/H3 do the morning integration; H1 is back for the rehearsal.
- 20 min micro-naps are allowed for those awake.
- One AFK per Ultra account, started by H2/H3 before their shift; no AFK on H1's account.
- Nobody drives the final demo on less than 4 h of sleep.
- The sleep area is reachable by shuttle; allow 20 min of travel.

## Communication

- One channel (Discord/Slack), three threads: `#decisions`, `#bloquants`, `#demo`.
- Statuses live in GitHub Issues/Projects, not in the chat.
- `progress.txt` = summary; `progress/<ticket>.md` = detail.
- An unanswered question → `docs/QUESTIONS.md`, priority P0/P1/P2.

## Agent brief

See `docs/AGENT-OPS.md` (8 lines + guardrails). Summary:

```text
You are <role>. Read CLAUDE.md, docs/PRD.md, docs/DECISIONS.md, docs/ARCHITECTURE.md
and progress/<ticket>.md. Ticket #<n>. Allowed files: <...>.
Acceptance criteria: <...>. Done = check.sh green + test + journal + commit.
```

## Pitfalls

| Pitfall | Guardrail |
| --- | --- |
| Scope too wide | Skeleton at 16:00, cut at 19:00 |
| Too many agents | 6 sessions max (8 with Ultra, readers on top), 1 main + 1–2 subagents per human (2–3 on Ultra) |
| Agents stepping on each other | 1 ticket = 1 worktree = 1 file zone |
| Saturated context | Short sessions, brief, `progress/<ticket>.md` |
| Victory declared too early | `check.sh` + `Stop` hook + reviewer |
| Code you cannot explain to the jury | Every human understands the differentiating core |
| Fragile demo | 09:30 freeze, mocks, video, seeded data |
| Quota exhausted | `/usage` at every sync, budget from `docs/ULTRA.md`, bounded AFK, Fable by hand |
| Incomplete submission | Form filled at 23:00, submitted before 12:00 |
| Slides | Banned: backup images/videos only |

## Sources

- [LauzHack — details](https://lauzhack.com/details)
- [LauzHack — rules](https://lauzhack.com/pdf/rules.pdf)
- [AI Hero — 7 phases](https://aihero.dev/my-7-phases-of-ai-development)
- [AI Hero — skills](https://www.aihero.dev/skills)
- [Claude Code — subagents](https://code.claude.com/docs/en/sub-agents)
