# Ticket 227-junie-evidence

- Status: prepared, waiting for a human (label `ready-for-human` kept)
- Human owner: @H1 (reading A), @H2 (decision on B)
- Track: process evidence / submission
- Agent: Claude Code (Fable 5.1, interactive session)
- Allowed files: `docs/demo/junie-lite-brief.md`, `docs/IDEAS.md`, `docs/QUESTIONS.md`, this file

## Done

- `docs/demo/junie-lite-brief.md`: the Junie Lite session a human runs in Air on this repo
  (two small candidate tickets, the prompt, the three captures, the review step, the README and
  DEMO sentences to paste afterwards).
- `docs/IDEAS.md`: reading B parked as an idea (greyed "JetBrains AI (Junie)" provider, no fake
  call).
- `docs/QUESTIONS.md` Q-023: the decision on B, with the recommendation to park it, owner H2.

## Not done, on purpose

- No Junie Lite session: an agent cannot drive Air. The checkbox "one ticket visibly done with
  Junie Lite" stays with a human.
- `README.md` and `docs/DEMO.md` untouched: #169 forbids a Junie or Air claim without a real
  capture. The sentences are ready in the brief.
- No change to the app: reading B is a WON'T in `docs/CONTEXT.md` until H2 says otherwise.

## Decisions

- Candidate A (provider hints on desktop) is recommended over a test-only ticket because the
  jury sees it; both are 1 file and `check.sh` is the oracle.
- The provider marks requested by H2 the same morning went to their own ticket (#240), done with
  Claude Code, so the Junie ticket stays small and distinct.

## Uncertain

- Whether Junie Lite is enabled on the team's JetBrains accounts (Q-021 says yes until year end).

## Next

- H1 or H3: run the brief, drop the captures in `docs/demo/`, paste the two sentences.
- H2: answer Q-023, then close #227 with the capture links.

## Journal

- 22/11 10:26 — worktree created.
- 22/11 — brief, IDEAS entry and Q-023 written; README/DEMO left for the capture.
