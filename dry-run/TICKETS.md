# Dry run — tickets for the "Pulse" mini-project

> Fictional, throwaway project, to test the base and the agents in real conditions.
> **Do not merge this branch into `main`.** Guide: `docs/DRY-RUN.md`.

## The scenario

**Pulse**: an on-call engineer pastes a CSV of metrics and immediately sees the
outliers, with their z-score. No account, no database, no external API. The "aha"
must be visible in under 20 seconds.

**Fictional sponsor challenge — Acme Telemetry**: "Show an explainable alert, not a
black box." Criterion: the z-score and the offending line are visible on screen.

## Constraints

- 2 h 30 maximum, 3 humans, 2 Ultra accounts.
- No new dependency beyond the starter.
- A single integration: none (everything is local).
- The demo must fit in 3 minutes, without slides.

## Tickets

### #01 — The analysis module

- **Visible behaviour**: from a `timestamp,value` CSV, the module returns the
  lines with a z-score and an `isAnomaly` flag.
- **Acceptance criteria**:
  - [ ] `parseCsv` accepts a valid CSV and rejects missing columns.
  - [ ] `parseCsv` rejects a non-numeric value with a clear message.
  - [ ] `detectAnomalies` flags the outlier with a threshold of 1.5.
  - [ ] A zero standard deviation produces no anomaly.
  - [ ] Tests in `src/modules/anomaly/index.test.ts`.
- **Allowed files**: `src/modules/anomaly/**`.
- **Dependencies**: none.
- **Risk**: low.
- **Demo-critical**: yes.

### #02 — The demo screen

- **Visible behaviour**: the user pastes a CSV, clicks "Analyze" and sees the
  number of anomalies + a table with the highlighted lines.
- **Acceptance criteria**:
  - [ ] The sample is preloaded.
  - [ ] "Analyze" displays "2 anomalies" on the sample.
  - [ ] Both outlier lines are highlighted.
  - [ ] "Reset sample" resets the screen.
  - [ ] An invalid CSV displays a readable error.
- **Allowed files**: `src/App.tsx`, `src/styles.css`.
- **Dependencies**: #01.
- **Risk**: low.
- **Demo-critical**: yes.

### #03 — The demo test and the script

- **Visible behaviour**: the `docs/DEMO.md` walkthrough is played by Playwright; the
  3-minute script is written and timed.
- **Acceptance criteria**:
  - [ ] `pnpm e2e` passes on the sample.
  - [ ] An invalid CSV displays the error in the test.
  - [ ] `docs/DEMO.md` contains the 2:45 version and the novelty line.
  - [ ] The demo has been rehearsed 2 times.
- **Allowed files**: `e2e/**`, `docs/DEMO.md`, `playwright.config.ts`.
- **Dependencies**: #02.
- **Risk**: low.
- **Demo-critical**: yes.

## Recommended order

1. H2: `WT_FROM=dry-run/base ./scripts/new-worktree.sh 01-anomaly` → `implementer` agent.
2. H3: waits for #01, then `WT_FROM=dry-run/base ./scripts/new-worktree.sh 02-ui` → agents
   `implementer` + `qa-demo`.
3. H1: writes `docs/DEMO.md` in parallel, then #03 with H3.
4. H2: `reviewer` on each diff, merge after `./scripts/check.sh --demo`.
5. Everyone: 3-minute demo, then `dry-run/REVIEW.md` (full feedback) and
   `dry-run/RETRO.md` (internal retro).

## Agent brief (to paste)

```text
You are implementer. Read CLAUDE.md, docs/PRD.md, docs/DEMO.md, docs/ARCHITECTURE.md
and progress/<ticket>.md.
Ticket: #01 — analysis module.
Goal: parse a timestamp,value CSV and flag anomalies by z-score.
Allowed files: src/modules/anomaly/**.
Acceptance criteria: see dry-run/TICKETS.md.
Done = check.sh green + test + progress/<ticket>.md + atomic commit.
```
