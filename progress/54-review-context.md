# Ticket 54-review-context

- Status: ready for PR
- Human owner: @zooldyyk
- Track: A shared core
- Agent: 21/11 audit → lead
- Model: main session
- Escalations: none
- Files: `shared/src/commonMain/.../review/ReviewContext.kt`, `commonTest/.../review/ReviewContextTest.kt`

## Log

- 18:05 — audit: the review took the first 20 files alphabetically; two Godot `.import`
  sidecars came before the source code.
- 18:10 — `selectReviewDiffs` sorts by number of added/removed lines, then by path
  (deterministic); `buildReviewContext` uses it. Assets/imports are filtered by
  `isReviewable`.
- 18:15 — tests: 25 diffs including 5 large files at the end of the alphabet; the 5 are kept;
  `hero.png.import` and `logo.png` are not reviewable.

## Decisions

- The sort is **descending on changed lines**, not on file size, to
  stay aligned with the prompt (diff + full content).
- Assets are not sent to the model: the review must see the code, not the sidecars.

## Verifications

- `:shared:jvmTest --rerun-tasks`: **74 tests, 0 failure**.
- `:desktopApp:compileKotlin` not re-run after this change (pure commonMain); JVM tests OK.

## Uncertain

- Sorting the modified files does not change the displayed diff, only the model context.
- `.import` files may contain useful metadata; we ignore them on purpose
  for the prompt budget.

## Next

- Replay the real review on the demo repo (2 sidecars must no longer show up).
