# Ticket 87-android-vectors

- Status: ready for PR
- Human owner: @zooldyyk
- Track: C (Android)
- Agent: lead
- Files: `shared/src/commonMain/composeResources/drawable/ic_*.xml`,
  removal of the `ic_*.svg`

## Log

- 23:20 — #87 reproduced: Compose Resources does not decode SVG on Android
  (`Android platform doesn't support SVG format`). Conversion of the 15 UI icons to
  `VectorDrawable` XML 24 dp, same `pathData`, 1.8 stroke, round caps.
- The `ic_app_light/dark.png` app icons stay PNG (desktop/dock), outside the
  Android path.

## Decisions

- A single format for UI icons: vector XML, decoded by Compose on desktop and
  Android. No SVG left in `composeResources`.
- No code change: `painterResource` already accepts VectorDrawables.

## Checks

- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` green.
- `:androidApp:assembleDebug` not run (no SDK); Branches validation to do on
  the emulator.

## Uncertain

- Exact rendering of the paths (color/stroke) to check on Android; the tint is applied by
  `NitpickIcon`.
