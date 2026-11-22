# Ticket 213 — Phone: gestures and rotation

- Status: done, to be checked on the emulator (closed at commit time)
- Human owner: @pepito2t (H3)
- Track: C android
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Allowed files: `androidApp/src/main/AndroidManifest.xml`, `ui/phone/{PhoneGraphScreen,PhoneReviewScreen}.kt`,
  `ui/components/NitBottomSheet.kt`

## Done

- Rotation: the manifest's portrait lock is removed. The phone flow is already chosen by
  `cloneRoot` (not by width), the ViewModels survive recreation and the screen states
  are in `rememberSaveable` (current screen, tab, open nit). The token entered in Connect stays
  deliberately out of the bundle.
- Pull-to-refresh on the commit list = Fetch (`PullToRefreshBox` Material3, indicator bound to
  `isFetching`, ignored during another sync).
- Horizontal swipe in a nit's sheet: to the left → next nit, to the right → previous
  (120 px threshold, a vertical scroll triggers nothing). The system back gesture already followed
  `PhoneScreen.parent`; the sheet closes on a downward swipe (Material3).

## Not done

- On-screen check in landscape (branch card, list, sheet, settings): emulator closed.

## Log

- 03:20 — worktree, manifest, pull-to-refresh, swipe, check green.
