# Ticket 13-structure

- Status: done (green build locally), to merge on main
- Human owner: @thomas
- Track: cross-cutting (§6 contracts, before tracks A/B/C)
- Agent: Claude Fable 5.1 (interactive session)
- Model: fable
- Escalations: none
- Allowed files: shared/src/commonMain/kotlin/dev/nitpick/app/{domain,git,review,settings,ui}, shared/src/commonTest, gradle/libs.versions.toml, shared/build.gradle.kts, desktopApp/.../main.kt and androidApp/.../MainActivity.kt (App import only)

## Journal

- 12:50 — branch `ticket/13-structure` created in the main checkout (the worktree
  would land outside the session working directory).
- 13:05 — §6 contracts written in `commonMain`: `domain` (Commit, Ref, WorkingTreeStatus,
  FileDiff/Hunk/DiffLine, ReviewResult/ReviewComment/Severity, Credentials, PullResult),
  `git` (GitRepository, GitRepositoryFactory), `review` (ReviewAgent), `settings` (Settings,
  SettingsStore). `App.kt` moved into `ui/`. One commonTest test: JSON round-trip of
  `ReviewResult`.
- Decisions:
  - `GitRepositoryFactory` and `SettingsStore` are interfaces, not `expect`: an
    `expect` without `actual` does not compile, and the implementation arrives in the JGit and
    settings tickets. They will be injected via `AppContainer` (§5, manual DI). Proposed in
    `docs/QUESTIONS.md`.
  - `Branch` and `Suggestion` (§6) are not separate classes: `Ref` carries a `RefType`
    (LOCAL_BRANCH, REMOTE_BRANCH, TAG) and the suggestion is the `suggestion` field of
    `ReviewComment`, as the §6 contract requires.
  - No `graph` or `diff` package for now: git does not version an empty directory and
    there is no cross-track contract to put there yet. Their first ticket creates them.
  - kotlinx.serialization 1.13.0 (latest Maven Central release) added: `@Serializable`
    on `ReviewResult` is part of the contract (Koog structured output).
- Uncertain: nothing on the contracts side. Gradle warning "SDK XML version 4" (cmdline-tools
  newer than AGP): benign.
- Blocker: none.
- Next: merge `ticket/13-structure` into main, then start #15, #16, #17 and #18 (the
  blocker-free frontier) in four worktrees.
- 13:40 — spec published (#14) and 9 tickets created as sub-issues of #13 with native
  GitHub dependencies: #15 desktop open repo (B) · #16 lanes (A, multi-agent) · #17 diff
  parser (A) · #18 theme + EN/FR + settings (C) · #19 diff vs base desktop (B, ← #15 #17) ·
  #20 Koog agent (A, ← #17) · #21 Android clone + screens (C, ← #15) · #22 Nitpick this branch
  desktop (B+A, ← #19 #20, demo-critical) · #23 Android review loop + push (C, ← #22 #21,
  demo-critical). Batch 2 (at the 17h sync): fetch/pull/checkout, readFile/listFiles tools for
  the agent, shared line count script.
- 13:45 — toolchain installed locally without sudo (approved by Thomas): `brew install
  openjdk@21`, `brew install --cask android-commandlinetools`, then `sdkmanager
  platform-tools platforms;android-37.0 build-tools;37.0.0`. `local.properties` (git-ignored)
  points to `/opt/homebrew/share/android-commandlinetools`. `JAVA_HOME` must be exported
  for `gradlew`: `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`.
- 14:20 — `./scripts/check.sh` green (253 s on the first run): `:shared:allTests` (JVM + Android
  host, including `ReviewResultSerializationTest` on both), `:desktopApp:assemble`,
  `:androidApp:assembleDebug`. AGP 9.1.1 also requires `build-tools;36.0.0`, installed.
  `brew link --force openjdk@21` makes `java` visible to the git hooks and Claude.
