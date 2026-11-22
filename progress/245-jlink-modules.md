# Ticket 245-jlink-modules

- Status: done (PR #247)
- Human owner: @H2
- Track: B desktop
- Agent: Claude Code (Fable 5.1, interactive session)
- Allowed files: `desktopApp/build.gradle.kts`,
  `shared/src/commonMain/kotlin/dev/nitpick/app/ui/repository/RepositoryViewModel.kt`

## Done

- `includeAllModules = true` for the Compose distributable: the jlink runtime shipped
  `java.base` only, and JGit needs `java.management` the first time it reads a pack file. Seen
  as an endless loader on `medlinx` from the bundle launched with `runDistributable` (#241).
- `RepositoryViewModel.load` catches `Throwable` after rethrowing cancellation: an `Error`
  shows its message instead of a loader that never stops.

## Decisions

- Every module rather than the one missing: a bigger bundle beats a second surprise on stage.

## Uncertain

- Bundle size after the change: not measured.

## Journal

- 22/11 — found in the app output (`NoClassDefFoundError: javax/management/...`), fixed, the
  runtime's `release` file lists `java.management`; H2 replays the open.
