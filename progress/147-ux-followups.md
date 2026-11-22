# Ticket 147-ux-followups

- Status: in progress
- Human owner: @pepito2t (H3)
- Track: C polish
- Agent: Claude Code, human session
- Model: Fable 5.1
- Escalations: none
- Allowed files: ui/components/NitpickReview.kt, ui/repository/{ReviewPanel,RepositoryViewModel}.kt, ui/phone/PhoneReviewScreen.kt, strings EN/FR, docs/demo/*.png

## Log

- 02:30 — state on `main`: points 2 (plurals `shared_row`, `review_files_with_nits`) and 3 (hardcoded ":") are already fixed by other commits; the per-severity counters already exist on desktop but not on the phone; the badge stays in the Info tone.
- 02:40 — point 4: `SeverityCounters(comments)` shared in `NitpickReview.kt` (bugs first), used by the desktop and phone summary card; "AI review" badge in the `Ai` tone, "Shared review" stays Info.
- 02:45 — point 1, the part that affects the demo: a model answer that is not a valid review (`ReviewResponseException`) becomes `ReviewFailure.InvalidAnswer`, shown with an EN/FR sentence instead of the exception text. The other messages (JGit, apply, clone) stay as they are: they are our own English sentences or the JGit detail, useful as is and out of budget for tonight.
- Test: `anInvalidModelAnswerIsShownAsItsOwnFailureNotRawText`.

## Uncertain

- Point 5 (desktop apply/commit/push screenshots, light + dark): done in this PR if the desktop launches; the gaps in `DEMO.md` (sentence, created vs reused) are pitch content for H1 (#163).
