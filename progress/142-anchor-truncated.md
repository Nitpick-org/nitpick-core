# Ticket 142-anchor-truncated

- Status: in progress
- Human owner: @pepito2t (H3)
- Track: C polish
- Agent: Claude Code, human session
- Model: Fable 5.1
- Escalations: none
- Allowed files: shared/src/commonMain/.../ui/components/NitpickReview.kt

## Log

- 02:05 — found by the audit UX lane (#130): `"$filePath:$line"` on one line with end ellipsis, the `dogfooding-104-review.png` screenshot shows "JGitRepository.kt:2…".
- 02:10 — `TextOverflow.StartEllipsis`: the ellipsis eats the start of the path (the folders), never the file name or the line number. One line, desktop and phone (same card).

## Uncertain

- Nothing.
