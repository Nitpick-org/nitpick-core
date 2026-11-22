@AGENTS.md

# Claude Code — in addition to AGENTS.md

The team rules live in `AGENTS.md`, imported above. This file covers only
sub-agents, model routing, hooks, AFK and Ultra capacity.

## Sub-agents

| Need | Agent | Default model |
| --- | --- | --- |
| Factual docs, README, progress, summary | `scribe` | Haiku |
| Implement a ticket in TDD | `implementer` | Sonnet (Opus if demo-critical with risk ≥ medium) |
| Sourced research (Koog, JGit, Compose, jury) | `researcher` | Sonnet |
| Replay the demo, find what breaks | `qa-demo` | Sonnet |
| Review a diff before merge | `reviewer` | Opus |
| Split into packages and tickets | `architect` | Opus |
| Challenge the scope or a plan | `devils-advocate` | Opus |
| Demo script, submission README, Q&A | `pitch` | Opus |

- **Route before delegating**: `/route-task`, details in `docs/MODEL-ROUTING.md`. Start
  low; two failures on the same point → relaunch the same agent with the model one step up.
  Fable is picked by hand (`/model`), for a high-stakes decision.
- Code reconnaissance goes through the built-in `Explore` and `Plan` agents (read-only).
- Sub-agents absorb the noise (reading, research, review, QA); code writing
  happens in one worktree per ticket. Cap: 8 sessions, 3 of them code writers
  (`docs/AGENT-OPS.md`, `docs/ULTRA.md`). Agent teams stay disabled.
- A sub-agent report is a proposal: the domain human decides what we keep,
  and every rejection or correction goes into `docs/REVIEW_LOG.md`.

## Hooks

- **`Stop`** (`scripts/stop-check.sh`): as long as `check.sh` is red on a modified tree,
  the session cannot stop. Deliberately dirty exploration:
  `HACKATHON_SKIP_STOP_CHECK=1 claude`.
- **`PreToolUse` Edit/Write** (`scripts/guard-protected.sh`): a sub-agent or a headless
  session does not edit `AGENTS.md`, `CLAUDE.md`, `docs/CONTEXT.md`, `docs/REVIEW_LOG.md`,
  `docs/DECISIONS.md`, `docs/PRD.md`, `docs/STRATEGY.md`, `docs/AGENT-OPS.md`,
  `docs/ULTRA.md`, `docs/MODEL-ROUTING.md`, `progress.txt`, `.env`. Explicit override:
  `HACKATHON_ALLOW_PROTECTED=1`.
- **`PreToolUse` Bash** (`scripts/guard-bash.sh`): blocks all access to `.env` and any
  write command (`>`, `sed -i`, `mv`, `python`…) that names a protected file, **even
  in the human session**. To read them, use Read; to change them, use Edit or Write.
  A commit message that contains `>` and one of these names is blocked too: write it to
  a file and pass it with `git commit -F`.
- **`pre-commit`** (`.githooks/pre-commit`): runs `check.sh` and rejects a staged `.env`.

## AFK

`AFK_OK=1 ./scripts/afk-ralph.sh 4` in a ticket worktree, only if the task has a
deterministic oracle (`check.sh`, a test), is low-risk (tests, docs, local refactor,
no product decision) and the worktree is clean. Open `claude` once in the worktree
first (trust dialog). Sonnet model by default; the scripts refuse Fable.
Details: `docs/AGENT-OPS.md`, `docs/ULTRA.md`.

## Capacity and budget

- H2 and H3 are on Ultra: broad research, repeated QA and AFK go to them;
  decisions stay with their owner.
- `/usage` at every sync, compared against the thresholds in `docs/ULTRA.md`.
- Claude Code runs on the subscription: `doctor.sh`, `ralph-once.sh` and `afk-ralph.sh`
  refuse to run if `ANTHROPIC_API_KEY` is set.

## Agent skills

### Issue tracker

GitHub Issues on `zooldyyk/hackathon`, via `gh`. See `docs/agents/issue-tracker.md`.

### Triage labels

The five default roles (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `docs/GLOSSARY.md` + `docs/DECISIONS.md`. See `docs/agents/domain.md`.
