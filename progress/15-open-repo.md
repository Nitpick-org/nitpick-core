# Ticket 15-open-repo

- Status: done, to review then merge (issue Nitpick-org/nitpick-core#15)
- Human owner: @dovatgabriel
- Track: B git+desktop
- Agent: Claude Code, main session
- Model: opus
- Escalations: none
- Allowed files: shared/src/jvmSharedMain, shared/src/commonMain/kotlin/dev/nitpick/app/{ui,AppContainer.kt}, shared/src/commonMain/composeResources, shared/src/{jvmTest,commonTest}, desktopApp, androidApp (MainActivity), gradle/libs.versions.toml, shared/build.gradle.kts

## Log

- 14:16 — worktree created.
- 14:20 — `JGitRepository` (jvmSharedMain): log, refs, currentBranch, status, readFile,
  listFiles; writes, network and `diffAgainst` throw `NotImplementedError`
  ("… is not implemented yet"). `JGitRepositoryFactory.open`; `clone` in TODO. Spike removed.
- 14:35 — 12 `jvmTest` tests on temporary repositories created with the git CLI (no JGit
  import in the tests). Trap: `fun test() = runBlocking { … }` that ends on
  `assertFailsWith` does not return `Unit`, JUnit rejects the class → `runBlocking<Unit>`.
- 14:50 — UI: `AppContainer` (manual DI), `RepositoryViewModel` (StateFlow, immutable state),
  `RepositoryScreen` 3 panels (refs | commits | details placeholder) with empty,
  loading and error states, EN + FR strings, native folder picker in `desktopApp`.
  3 ViewModel tests in `commonTest` with `FakeGitRepository`, green on JVM and Android host.
- 15:00 — `check.sh` green (21 tests executed). Desktop app launched: a real repository opened via
  the picker displays its commits, branches, remote and tag (screenshot taken).

- 15:40 — review requested by the human (ponytail-review), 4 points fixed:
  linked worktree refused with a clear message (JGit 6.10 cannot open it), "choose a repository"
  hint hidden without a picker, detached HEAD displayed as a short id, fake
  repository reduced to what the tests use.
- 15:45 — **incident**: the `pre-commit` hook exports `GIT_DIR` and `GIT_INDEX_FILE`; the
  git commands of `TestGitRepo` inherited them and wrote into the real repository (8 test
  commits on the ticket branch, a `feature` branch and its worktree, index overwritten,
  `core.bare=true`). Nothing pushed, `main` intact. Restored (`core.bare=false`, branch
  brought back to `d263555` with `reset --mixed`, stray branch and worktree deleted).
  Cause fixed: `TestGitRepo` strips any inherited `GIT_*`; proven by running the tests with
  a non-existent `GIT_DIR` (13 failures before, 13 passes after).

## Decisions

- `log` sorts in topological order then by date: each child comes before its parents,
  which is what the lanes (#16) need. TOPO walks the whole history before handing back
  control (`shortcut:` comment in the code).
- The dates are the author's, like `git log`.
- `readFile` refuses absolute paths, those that leave the repository (including via
  symlink) and those that target `.git` (the config may contain a token): these paths
  will come from the LLM.
- `listFiles` reads the index: tracked files only, so no `build/` and no ignored files.
- `open` accepts a subdirectory of a repository (`findGitDir` walks up) and refuses a bare repository.
- `formatCommitTime`: `expect` in commonMain, a single `actual` in jvmSharedMain
  (java.time exists on desktop and on Android from API 26), localised by the system locale.
- The macOS picker is the native `FileDialog`; Windows and Linux go through `JFileChooser`.
- Wizard sample code removed (Greeting, Platform, drawable): nothing used it any more.
- Dependencies: `kotlinx-coroutines-core` declared explicitly (already present transitively,
  used for `Dispatchers.IO`) and `kotlinx-coroutines-test` for `Dispatchers.setMain`.

## Uncertain

- The JGit test on ART (spike) is removed with the spike; `assembleDebug` still dexes
  JGit, but running on Android comes back with the clone (#21).
- `GitRepository` has no `close()`: the JGit repository stays open as long as the app runs
  (benign on macOS/Linux, may lock files on Windows). Changing the contract =
  H2's approval.
- Windows/Linux picker not tested (macOS only).

## Blockers

- None. Watch out for the merge with #18 (track C): both tickets create
  `composeResources/values*/strings.xml`.

## Lesson to add to AGENTS.md (H2)

- A test that runs `git` must strip inherited `GIT_*` variables: the `pre-commit`
  hook exports them, and the test then writes into the real repository.

## Next

- #19 (diff vs base) can build on `RepositoryViewModel`; #16 consumes `Commit.parentIds`.
