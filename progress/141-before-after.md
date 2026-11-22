# Tickets 141 + 142 — Before/after in the nit card, line number never truncated

- Status: done (not seen on screen: emulator closed)
- Human owner: @pepito2t (H3)
- Track: B desktop + C android (same component)
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Allowed files: `ui/components/NitpickReview.kt` (`ReviewCommentCard`), its callers
  `ui/repository/ReviewPanel.kt` and `ui/components/NitBottomSheet.kt`

## Done

- `ReviewCommentCard` takes `original` (the nit's `originalSnippet`) and renders the "Suggested
  change" block as a diff: `-` lines on `diffRemovedBg`, `+` lines on `diffAddedBg`, code
  font. Desktop and phone pass `comment.originalSnippet`.
- The anchor: the path and `:line` are two texts; the path shrinks with an ellipsis, the line
  number always stays visible (#142).

## Not done

- No test: pure Compose component, no UI test infra in the repo. To check on
  the emulator or the desktop before merge.

## Log

- 02:15 — worktree, before/after card, check green.
