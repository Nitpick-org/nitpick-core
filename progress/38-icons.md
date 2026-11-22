# Ticket 38-icons

- Status: ready for PR
- Human owner: @zooldyyk
- Track: C (design system)
- Agent: lead
- Model: main session
- Escalations: none
- Files: `shared/src/commonMain/composeResources/drawable/*.svg`,
  `ui/components/{NitpickIcon,NitpickIdeShell}.kt`, `ui/repository/{RepositoryScreen,DiffPanel}.kt`,
  `ui/NitpickAppIcon.kt`, `desktopApp/.../main.kt`, `androidApp/src/main/res/**`

## Log

- 21:05 — 15 SVG 24 px, stroke 1,8, taken from the `path` entries of the `ic` block of the mockup:
  branch, cloud, tag, chevron, gear, folder, key, wifi-off, clock, check, x, tweezers,
  fetch, pull, push.
- 21:10 — `NitpickIcon` (16 dp, context tint); section chevrons and branch/cloud/tag icons
  in the tree; tweezers on the CTA; gear on Settings; chevron
  on the base selector.
- 21:15 — teal tweezers app icon: Android foreground/background + legacy mipmaps;
  desktop through `nitpickAppIcon()` (shared Compose resource, no deprecated
  painterResource).

## Decisions

- The icons are Compose Resources SVGs, not Kotlin ImageVector: one file
  per icon, tint at render time, no Material Icons dependency.
- Icons next to text have `contentDescription = null` (the text carries the meaning);
  `NitpickIcon` makes the parameter mandatory to avoid silent standalone icons.

## Verifications

- `:shared:jvmTest --rerun-tasks`: **139 tests, 0 failure**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` green.
- `:androidApp:assembleDebug` blocked by the missing Android SDK in this environment.

## Uncertain

- Visual rendering at 1×/2× and the macOS dock not verified on screen.
- The legacy Android PNGs are generated, but only the `anydpi-v26` mipmaps use the
  vector foreground; to check on an emulator.
