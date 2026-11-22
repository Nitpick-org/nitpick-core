# Ticket 89-android-back

- Status: ready for PR
- Human owner: @zooldyyk
- Track: C Android
- Agent: lead
- Files: `ui/phone/PhoneFlow.kt`, `ui/App.kt`, `gradle/libs.versions.toml`,
  `shared/build.gradle.kts`

## Log

- 23:50 — added Compose Multiplatform `BackHandler` (`ui-backhandler` 1.12.2):
  Clone/Graph → Repos, Review/Shared → Graph, Diff → Review; from Repos, the app closes.
- 23:52 — Settings (phone): Back closes Settings before quitting.
- `org.jetbrains.compose.ui:ui-backhandler` dependency added to `commonMain`.

## Checks

- `:shared:jvmTest` + `:desktopApp:compileKotlin` green.
- Android runtime not tested (no SDK): to check on the emulator.

## Uncertain

- `BackHandler` is deprecated in favor of `NavigationEventHandler`; the current stable API
  stays in use, migration to plan.
