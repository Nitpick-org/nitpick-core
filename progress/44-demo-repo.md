# Ticket 44-demo-repo

- Status: in progress (agent preparation done; human steps remaining)
- Human owner: @dovatgabriel
- Track: demo (#39)
- Agent: Claude Code (lead)
- Model: opus
- Escalations: none
- Allowed files: `docs/demo/*`, `docs/DEMO.md`, `progress/44-demo-repo.md`; the demo repo
  lives outside this repository (`../pocket-ledger`).

## Journal

- 19:08 — worktree created.
- 19:20 — `pocket-ledger` created locally: Kotlin/JVM CLI, SQLite via JDBC, `add`, `list`,
  `report <mois>`. 3 tests green, CLI checked by hand. A clean commit on `main`.
- 19:25 — Junie brief (`docs/demo/junie-brief.md`): 3 commands, one defect planted per commit
  (pagination off-by-one, SQL injection, unclosed reader). `DEMO.md` points to the repo and the
  branch.

## Decisions

- Kotlin rather than TypeScript: the JetBrains jury reads Kotlin, and the project looks like
  ours without being a copy of it.
- Off-by-one on the pagination offset: a defect visible on the first try (page 1
  shifted) and fixable in one line, therefore an applicable suggestion in Nitpick.
- Clean `main`, prepared queries everywhere: the injection of the branch stands out against
  the rest of the code, as in a real review.
- `slf4j-nop` at runtime: sqlite-jdbc logs through SLF4J and displayed 3 warning lines
  on each command, which would ruin the demo.

## Checks

- `./gradlew test`: 3 tests, 0 failures (report `build/test-results/test`).
- `ledger add` ×2, `list`, `report 2026-11`: correct output, no warning.

## Uncertain

- sqlite-jdbc 3.46.1.0 is a known and stable version, not verified as the latest.

## Blockers (human)

- Create the public repo `Nitpick-org/pocket-ledger` and push `main`.
- Run the brief in Air (Junie), review, push the branch.
- Replay the review 5 times; dogfooding screenshots in `docs/demo/`.

## Next

- Dogfooding: Nitpick on branches #19, #20, #21; keep 2–3 screenshots.
