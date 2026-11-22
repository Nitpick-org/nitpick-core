# Ticket 21-android-clone

- Status: done, to review then merge (issue #21)
- Human owner: @pepito2t (H3)
- Track: C android+finitions
- Agent: Claude Code, main session
- Model: fable
- Escalations: none
- Allowed files: shared/src/jvmSharedMain/git, shared/src/commonMain/kotlin/dev/nitpick/app/{ui,settings/Settings.kt}, shared/src/commonMain/composeResources, shared/src/{jvmTest,commonTest}, androidApp
- Out of bounds (worktree #20 in progress): `AppContainer.kt`, `domain/Review.kt`, `FakeGitRepository.kt`, `shared/build.gradle.kts`, `gradle/libs.versions.toml`, `docs/RESEARCH.md`

## Journal

- 17:26 — worktree created. #21 chosen: the only non-blocked P0 that nothing depends on; #23 waits for #20 and #21.
- 17:30 — `JGitRepositoryFactory.clone` (`Git.cloneRepository` + `UsernamePasswordCredentialsProvider`)
  and `JGitRepository.fetch`. 3 `jvmTest` tests on a local repo over `file://`: clone (log,
  branch, `origin/main`), failed clone with no leftover folder, fetch that advances `origin/main` without
  moving the local branch. 16 JGit tests green.
- 17:33 — `RepositoryViewModel.cloneRepository(url, path, credentials)` shares `load()` with
  `openRepository`. 2 tests with a local factory in the test (no change to the shared fake).
  `Settings.withRecentRepository` (head of the list, no duplicates, 10 max) + 2 tests.
- 17:38 — phone flow in `ui/phone/`: `PhoneFlow` (Repos → Clone → Graph), `ReposScreen`
  (recent repos + Clone button), `CloneScreen` (URL, token masked with Show/Hide, folder
  name inferred from the URL, error displayed in place), `PhoneGraphScreen` (Commits /
  Branches tabs, reuses `CommitGraphList` and `RefsSidebar`). `App` picks this flow under 600 dp
  wide and pushes each opened repo into `recentRepositories`. EN + FR strings.
  `MainActivity` passes `filesDir/repos` as the clone root; `INTERNET` permission added.
- 17:40 — `check.sh` green (57 JVM tests executed). ART test `JGitOnDeviceTest` (opens a `.git`
  zipped as an asset, clones it over `file://`, reads the log).
- 17:50 — `connectedDebugAndroidTest` on the `nitpick` emulator (API 36): **2 tests executed, 0 failures**
  (`opensARepositoryAndReadsItsLog`, `clonesALocalRepositoryAndReadsItsLog`). JGit opens and clones on ART.

- 17:55 — manual proof on the emulator: Repos (empty state) → Clone → public HTTPS clone
  `octocat/Hello-World` without a token → Graph "on master · 3 commits" with lanes; after
  `force-stop` and relaunch, Hello-World is listed in Repos (screenshots in the session
  scratchpad). Centering of the label in `NitpickButton` (the full-width button displayed it on the left).

- 18:00 — ponytail review, 5 points fixed in the same commit: `Password` keyboard for the token
  and `Uri` for the URL (`NitpickTextField` gains `keyboardOptions`); phone flow on `cloneRoot`
  instead of the width (landscape checked on the emulator); `SettingsViewModel.update` waits for the
  file read (test `SettingsViewModelTest`, otherwise an early update overwrote the API key);
  return from Settings to Graph when a repo is open (checked); folder name `trim()`.
  `check.sh` green, 58 JVM tests.
- 18:10 — rebase on `origin/main` (#20 and #22 merged): 5 "keep both" conflicts (strings,
  `App.kt`, `RepositoryViewModel.load` which now keeps the repo open, JGit tests).
  `check.sh` green, 82 JVM tests, 2 ART tests. Trap: `connectedDebugAndroidTest` uninstalls the app
  after the tests and wipes its data (settings, clones); re-clone before a demo.
- 18:20 — second ponytail review, 3 points fixed: `Password` keyboard also on the API key of
  the Settings screen, unused `repository_loading` string removed, `PhoneScreen` private.
- 18:30 — third review: the activity is locked in portrait (the token, deliberately out of
  the saved state, disappeared on rotation).

## Decisions

- The phone flow triggers when `App` receives a `cloneRoot` (Android), not on width:
  the first version (< 600 dp) displayed the desktop layout, with no cloning possible, as soon as the phone
  went landscape. No `expect`/`actual`.
- The clone root arrives through a parameter of `App`, like `pickRepositoryPath`, so as not to
  touch `AppContainer` while #20 edits it.
- The token: never in the ViewModel state, never in `rememberSaveable`, never persisted; it
  lives in `Credentials` for the duration of the clone. Fixed username `x-access-token` (GitHub accepts
  any name with a PAT; `shortcut:` noted for GitLab).
- The Clone screen stays displayed during the clone: a wrong token is fixed in place, without retyping
  the URL. Navigation to Graph happens when the state is loaded without error.
- The folder name rejects `/`, `\`, `.` and `..`: the clone stays under `filesDir/repos`.
- ART fixture = zip of a `.git` (no git CLI on the device, no JGit import in `androidApp`).
  Dependency added to `androidApp` only: `kotlinx-coroutines-core` in `androidTestImplementation`
  (for `runBlocking`), alias already in the catalog.
- No JGit section in `docs/RESEARCH.md` (file opened by #20): API verified in the JGit 6.10
  javadoc (`CloneCommand.setURI/setDirectory/setCredentialsProvider`, cleanup of the folder created on
  failure; `FetchCommand.setCredentialsProvider`). The "failed clone with no leftover folder" test
  proves it.

## Uncertain

- Android system Back button: not handled (the screens have their own Back button); the
  cross-platform `BackHandler` is experimental, to add if the demo requires it.
- Top bar at 40 dp (in-house `NitpickToolbar` component) instead of the 56 dp of handoff §4.
- No "Nitpick branch" FAB on the Graph screen: that is #23.
- The rows of `CommitGraphList` (#16, desktop layout) are cramped on 393 dp: the date overlaps
  the subject. Polish to do in the component (60 dp compact row of handoff §4), not in this ticket.
- JGit error message displayed raw (e.g. `https://…: not authorized`); it does not contain the token.

## Blockers

- None.

## To do by a human (acceptance criterion)

- Clone a private GitHub repo with a token on a phone or the emulator, check commits and
  branches, relaunch the app and find the repo again in Repos.

## Next

- #23: review loop on Android (push with `Credentials`, bottom sheet), on these screens.
