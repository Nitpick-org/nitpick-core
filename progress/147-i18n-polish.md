# Ticket 147 (part) — i18n polish: plurals and label/detail format

- Status: items 2 and 3 done; 1, 4, 5 remain post-freeze
- Human owner: @pepito2t (H3)
- Track: A core (strings) + UI
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Allowed files: EN/FR strings, `ui/repository/{SyncStatus,ReviewPanel,ShareReview}.kt`,
  `ui/phone/PhoneReviewScreen.kt`, `CountStringsTest.kt`

## Done

- `review_files_with_nits` and `shared_row` become `<plurals>` ("1 nit", "1 file
  with nits"); `CountStringsTest` covers them in both languages.
- `label_detail` ("%1$s: %2$s" in EN, "%1$s : %2$s" in FR) replaces the four `+ ": " +`
  joins in Kotlin (sync status, phone push error, tools used).

## Remaining (post-freeze)

- Raw exception text in the UI (1 h), severity counters on the summary (1 h), light/dark
  desktop screenshots (15 min, to do with the desktop app open).

## Log

- 02:35 — worktree, plurals, label/detail format, check green.
