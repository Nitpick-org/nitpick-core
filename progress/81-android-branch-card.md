# Ticket 81-android-branch-card

- Status: in progress
- Human owner: @pepito2t (H3)
- Track: C android+polish
- Agent: Claude Code, human session
- Model: Fable 5.1
- Escalations: none
- Allowed files: shared/src/commonMain/.../ui/phone/PhoneGraphScreen.kt, composeResources/values{,-fr}/strings.xml

## Log

- 23:37 — worktree created on `fb13976`.
- 00:05 — `BranchCard` under the Graph bar: branch name (mono), upstream or "No upstream branch", `↑n ↓m`, commit count, then three 44 dp equal-width Secondary `NitpickButton`s: Fetch / Pull n / Push n (Publish without upstream, nothing without `origin`). The three actions leave the top bar, which keeps Shared and Settings. The sync status line ("Pulled new commits", errors) stays under the list.

## Decisions

- The commit counter stays in the card (it was in the replaced header); the handoff does not ask for it but removing it would lose information.
- No `branche ▾` selector in the card: the Branches tab already does the checkout (#42).

- 00:20 — measured on the emulator (420 dpi, uiautomator): buttons 48 dp high (Material touch minimum around the requested 44 dp), 91 dp wide each. Light/dark screenshots in `docs/demo/81-branch-card-{light,dark}.png` (a `captures/` folder is ignored by `.gitignore`, first attempt lost).
- 00:35 — ponytail review: a failed push from the Graph displayed nothing (`syncStatusMessage` ignored `pushError`, only the Review screen showed it); the status line now shows it, on the phone as in the desktop status bar.

## Uncertain

- Nothing.
