---
name: Ticket (vertical slice)
about: A visible end-to-end behaviour, doable by a single agent in a worktree
labels: ticket
---

## Visible behaviour

<What the user can do once this ticket is done. One sentence.>

## Acceptance criteria

- [ ] …
- [ ] …
- [ ] …

## Track

A shared core / B git + desktop / C Android + polish (`docs/CONTEXT.md` §9)

## Allowed files

- `shared/src/<sourceSet>/kotlin/dev/nitpick/app/<package>/...`
- `shared/src/<sourceSet>Test/...` or `androidApp/src/androidTest/...`

## Do not touch

- The files reserved for humans ("Shared files" table of `AGENTS.md`), `.env`
- The contracts of §6, unless H2 agrees
- Other packages: …

## Dependencies

Blocked by: #

## Risk

high / medium / low

## Demo-critical

yes / no — if yes, `./scripts/check.sh --demo` passes and the walkthrough is replayed by hand
on desktop and Android.

## Sponsor challenge

none / <sponsor> — sponsor criterion: …

## Human owner

@

## Agent brief (8 lines)

```text
You are <role>. Read AGENTS.md, docs/CONTEXT.md, docs/DECISIONS.md and progress/<ticket>.md.
Ticket: #<n> — <title>. Track: <A|B|C>.
Goal: <visible behaviour>.
Allowed files: <...>.
Acceptance criteria: <...>.
Done = green check.sh + test + progress/<ticket>.md + atomic commit.
```
