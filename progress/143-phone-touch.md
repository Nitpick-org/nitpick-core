# Tickets 143 + 144 — Phone touch targets

- Status: 144 done; 143 closed "not reproducible" since #133 (full-height sheet, M3 insets)
- Human owner: @pepito2t (H3)
- Track: C android
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Allowed files: `ui/phone/{PhoneFlow,PhoneReviewScreen,ReposScreen,ConnectScreen}.kt`

## Done

- `PhoneTextAction`: `NitpickToolbarAction` with `heightIn(min = 44.dp)`; used for Back,
  Re-run and the `@login` account. Settings and Shared were already 44 dp icons (#133).
- Token Show / Hide toggle: 48 dp clickable box, centered text.

## Not done

- No test (UI). The external `heightIn(min)` wins over the desktop control's internal
  `height(28.dp)`; that is the Compose SizeNode contract, to check at a glance on the emulator.

## Log

- 02:25 — worktree, 143 closed with proof, 144 done, check green.
