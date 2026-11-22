# progress/ — per-ticket logs (no conflicts)

## Why this folder

Several agents work in parallel in different worktrees. If they all write to `progress.txt`,
every merge creates a conflict. One file per ticket removes the problem: two agents never
touch the same file.

## Rule

- Write entries in English (the repository is English end to end).
- An agent writes **only** to `progress/<ticket>.md` (created by `scripts/new-worktree.sh`).
- `progress.txt` is the **summary log**: only humans (ideally the integrator) write it,
  at every sync, in 3 to 5 lines.
- `docs/DECISIONS.md`, `docs/PRD.md` and `CLAUDE.md` are never edited by an agent
  without an explicit human request.
- `docs/QUESTIONS.md` is append-only: each agent adds one line, does not rewrite
  other people's lines.

## Entry format

```md
## 14:32 — ticket 12-graph-lanes (track A)

- Done: lane algorithm + 6 tests (merge, orphan branch, octopus).
- Decisions: one lane per parent not yet placed, reused as soon as it frees up.
- Model: sonnet.
- Escalations: sonnet → opus (2 failures on validation).
- Files: shared/src/commonMain/kotlin/dev/nitpick/app/graph/*, shared/src/commonTest/.../graph/*.
- Uncertain: lane colours not tested in dark theme.
- Blocker: none.
- Next: plug into the real `GitRepository.log()` (track B).
```

This per-ticket log **is** the agent log of §11 of `docs/CONTEXT.md`: the agent, track, task,
files, decisions and uncertain fields appear in it. `docs/AGENT_LOG.md` is only the
compilation of it, done by a human at syncs.

## At the end

- Merge the useful information into `progress.txt` (summary) and `docs/DECISIONS.md`
  (durable decisions).
- Delete `progress/` and `progress.txt` after the event: they are session artefacts.
