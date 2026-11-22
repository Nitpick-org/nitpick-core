# Ticket 85 — Tweezers app icon, light and dark variants

- Status: in progress
- Human owner: @pepito2t (H3)
- Track: C android + polish (also touches desktopApp)
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Escalations: none
- Allowed files: `docs/design/icon/`, `shared/src/commonMain/composeResources/drawable/ic_app_*.png`,
  `shared/src/commonMain/kotlin/dev/nitpick/app/ui/NitpickAppIcon.kt`, `ui/theme/NitpickTokens.kt`
  (`isDark` field), `androidApp/src/main/res/{drawable,mipmap-*}`, `desktopApp/dock-icon.png`

## Done

- `docs/design/icon/` versioned: the 11 files provided by H3 (light tile `#0F766E`, dark tile
  `#1E1F22`), source of truth.
- In-app (desktop window, desktop and phone empty states): `ic_app_light.png` /
  `ic_app_dark.png`, selected by `NitpickTheme.colors.isDark`, therefore by the app's theme
  setting, not by the system.
- macOS dock: `dock-icon.png` regenerated from the light tile with the ~18% margin of 9f1c847.
- Android launcher: back to the vectors of #74 (`#0F766E` background, tweezers + dot in
  `ic_launcher_foreground.xml`), foreground PNGs removed; the legacy mipmaps (< API 26,
  never served with minSdk 26) regenerated from the light tile so that no magnifying glass is
  left.

## Decisions

- Replaces the magnifying glass of cfcb228 (H2, 22:23) at H3's explicit request: to be synced with H2.
- PNG rather than SVG for the in-app icon: `painterResource` does not render SVG on Android
  (official Compose resources documentation), and `ReposScreen` runs on the phone.
- Launcher and dock keep the light tile: neither Android nor the macOS dock change icon
  with the theme. Not done, to open if useful: `<monochrome>` (Android 13+ themed icon),
  `nativeDistributions.iconFile` for packaged builds.

## Evidence

- `check.sh` green; `NitpickAppIconTest` run (1 test, jvm + androidHostTest).
- APK installed on the `nitpick` emulator: app drawer with the new tile.
- Not verified on screen: the in-app empty state (the emulator already has repositories) and the desktop window.

## Journal

- 22:59 — worktree created.
- 23:00 — assets added, launcher vectors, `isDark`, test; check green; emulator screenshot.
