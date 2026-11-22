# Ticket 236-submission-numbers

- Status: done
- Human owner: @pepito2t (H3)
- Track: submission packaging (docs only)
- Agent: Claude Code, Fable 5.1
- Escalations: none
- Allowed files: README.md, docs/DEMO.md, docs/QUESTIONS.md, progress/236-submission-numbers.md

## Done

- README status table: 190 strings + 7 plurals (was 179), counted in `values/strings.xml` and `values-fr/`.
- DEMO.md shared-lines reading refreshed from `./scripts/count-lines.sh` on `main` (98,6 %).
- DEMO.md plan B: where the backup shared review lives, the demo repo lost its planted bugs and how to restore them, the desktop replay of 10:15.
- QUESTIONS.md: the test count in the team rules (200) is 218 on `main`; human-owned file.

## Decisions

- No force push on `pepito2t/OpenTab` by the agent: the branch reset is the owner's command, written in DEMO.md.
- No feature work: the freeze passed at 09:30, the parked endpoint field stays in `docs/IDEAS.md`.

## Uncertain

- Apply → commit → push not replayed on desktop on this pass.

## Journal

- 10:16 — worktree created.
- 10:05 — `:shared:jvmTest` + `:server:test` on `main`: 218 tests, 4 skipped, 0 failures.
- 10:15 — desktop replay on `main` with the local test repo: open, graph, review, shared review, settings.
