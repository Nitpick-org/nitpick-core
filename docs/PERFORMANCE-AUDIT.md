# Performance audit — November 21, 2026

> Scope: desktop JVM and Android, shared core (`shared/`). Measurements taken on the
> development machine (macOS, JDK 22, warm JVM), before/after the fixes in this pass.
> Second pass (review + Android part): November 21, evening — see "Review" and
> "Android" below.

## Summary

The **local** work was the real bottleneck on real-sized repositories, not the UI:

- `log(500)` re-walked **the entire history** because of JGit's topological sort: **449 ms**
  on a repository with 100 000 commits, and `buildReviewContext` paid that cost again to
  display 10 commit messages.
- `diffAgainst` hashed **every tracked file** of the working tree on each refresh:
  **234 ms** on 3 000 files, even when only 4 files had changed.
- A diff refresh cancelled then restarted (clicking the Diff tab, applying a suggestion)
  stacked several non-cancellable JGit walks.

After the fix, on the same repositories:

| Operation | Before | After | Gain |
| --- | ---: | ---: | ---: |
| `log(500)`, 100 000 commits | 449 ms | **5 ms** | **90×** |
| `buildReviewContext`, 100 000 commits | 458 ms | **19 ms** | **24×** |
| Full open, 100 000 commits | 894 ms* | **31 ms*** | **~29×** |
| `diffAgainst`, 3 000 files, clean tree | 234 ms | **28 ms** | **8,4×** |
| `diffAgainst`, 3 000 files, 4 modified files | 234 ms | **107 ms** | **2,2×** |
| `diffAgainst`, this repository (147 commits) | 41 ms | 41 ms | — (`git status` floor) |

\* The "full open" probe called `log()` twice, before as well as after: the ratio
stays comparable, the real value is lower on both sides.

The remaining **review** latency is dominated by the LLM, not by local code. The default
model used to be the flagship model (Opus) while `CONTEXT.md` §14 asks for a "fast model
by default": this is fixed (Sonnet first), but an existing setting keeps Opus.

## Protocol

Four repositories, to separate history size from tree size:

| Repository | Commits | Tracked files | Use |
| --- | ---: | ---: | --- |
| this repository | 147 | ~150 | the team's real case |
| `/tmp/bigrepo` | 5 000 | 3 000 | wide tree, dirty working tree |
| clean clone of `bigrepo` | 5 000 | 3 000 | wide tree, clean working tree |
| `/tmp/hugerepo` | 100 000 | 2 000 | deep history |

Measurements: median of 3 runs after a warm-up, `System.nanoTime()`, warm JVM. The Compose
values come from a temporary `ui-test-junit4` test (1024×768 scene, 500 graph rows,
100-file diff): they are indicative, not GPU profiles.

## Fixes delivered

1. **`log()` bounded by the requested window** — `JGitRepository.kt:44`
   The walk switches to `COMMIT_TIME_DESC`, stops at `limit`, then an in-memory Kahn sort
   puts children back before parents (the lanes contract). Cost bounded by `limit`, not by
   the repository size. A test covers the reversed-clock case (a merge older than its
   parent).

2. **`diffAgainst()` filtered to the paths that actually changed** — `JGitRepository.kt:133`
   `git status()` returns the changed paths (index + working tree); the paths changed
   between the fork point and HEAD are added to them. The `DiffFormatter` now only visits
   those paths. Clean tree: tree→tree diff, no file hashed. The home-grown `TrackedFilter`
   goes away (untracked files are already excluded).

3. **Single-flight diff refresh** — `RepositoryViewModel.kt:211`
   One walk at a time; a request arriving during the walk remembers the most recent base
   and the job loops once more. No more concurrent diffs when clicking Diff or applying
   several suggestions.

4. **Precomputed line counters** — `Diff.kt:14`, `DiffPanel.kt:58`
   `FileDiff.addedLines/removedLines` are computed once at parse time. The panel no longer
   rescans every line on each recomposition (and neither does `selectReviewDiffs`).

5. **`stage()` in a single index write** — `JGitRepository.kt:209`
   One grouped `add()` and `rm()` instead of one `call()` per file. The cost no longer
   depends on the number of applied suggestions.

6. **Re-anchoring: one read per file** — `RepositoryViewModel.kt:570`
   Nits from the same file share the read instead of re-reading the file for each nit.

7. **No useless settings write** — `Settings.kt`, `SettingsViewModel.kt:34`
   Reopening the repository already at the top of the list no longer rewrites the file (and
   no longer re-emits the state).

8. **Stable theme tokens** — `NitpickTheme.kt:64`
   Typography, shapes, spacings and `ColorScheme` are `remember`ed. It was a subtree
   invalidation on every theme re-execution (theme toggle, previews); it was not the main
   cause of the slowness, but it was free to fix.

9. **Fast model by default** — `LlmModels.kt:10`
   Sonnet now comes before Opus for Anthropic, in line with the `CONTEXT.md` §14 fallback.
   An existing `settings.json` file keeps its model: it must be changed in the settings for
   an already-configured demo machine.

10. **UI micro-allocations** — `RefsSidebar` (memoized filter, hoisted sections),
    `DiffPanel` (memoized selection and bases), `CommitGraphLane` (passing lanes computed
    once per row), `PhoneReviewScreen` (memoized `groupBy`).

## Review (second pass)

The four priority points of the handoff (`progress/perf-audit.md`), re-read against the code:

1. **`diffAgainst` + `changedPathsBetween`: compliant**, with one edge case inherited from
   removing `TrackedFilter`. Rename (both paths enter the filter), deletion, staged file
   and clean tree are correct and tested. The edge case: a file **deleted between the fork
   point and HEAD** but recreated **untracked** in the working tree. Its path is in the
   filter, and `FileTreeIterator` sees the file on disk: the deletion shows up as a
   modification (or disappears if the content is identical), and the untracked content is
   sent to the LLM even though the doc promises the opposite. Niche (a file regenerated by
   a tool at a path that a branch deletes); not fixed, to be decided by H2 — the clean fix
   goes back through filtering on the index.
2. **`log` + `childrenBeforeParents`: compliant.** Kahn is correct and deterministic, and
   so is the guard on a truncated window. Accepted limitation: with a heavily reversed
   clock, the 500-commit `COMMIT_TIME_DESC` window may contain a different set of commits
   than a TOPO would have given — the order itself remains children-before-parents (tested).
3. **`refreshDiff` single-flight: compliant.** All mutations happen on the main dispatcher;
   `load()` cancels the job and clears `pendingDiffBase`, and the result of a closed
   repository is dropped (`repository !== git`).
4. **`LlmModels`: compliant with §14.** Product choice confirmed, to be validated by H1; an
   existing `settings.json` keeps its model.

## What is still slow, and why

- **The LLM review (tens of seconds; 90 s timeout).** The prompt can reach 120 000
  characters of file content on top of the diff, with up to 4 tool rounds, without
  streaming. Levers, in order of safety:
  1. fast model (done for new installs);
  2. `MAX_PROMPT_TOTAL_CHARS` budget at 60 000 (fewer input tokens, hence less latency;
     context loss to be validated on quality);
  3. streaming or showing nits as they arrive (large UI change);
  4. caching one review for the demo repository (already planned by `CONTEXT.md` §14).
- **`git status()` on every diff (~20–30 ms).** This is the floor of the filtered diff. A
  cache keyed on `HEAD` + index mtime would avoid most tab refreshes, at the cost of a risk
  of a stale diff after an external edit. Not done: the gain was not worth the correctness
  risk.
- **Desktop startup (~1,4 s to first composition).** Mostly Compose/Skiko and resource
  loading; no simple lever without degrading the first screen.
- **Graph.** 500 rows, `LazyColumn`, lanes off the UI thread: already compliant with the
  `CONTEXT.md` fallback. The drawing still allocates one `Path` per curve and per frame;
  negligible compared with the rest.

## Android

No measurements on ART yet: no device or emulator connected during the second pass
(`adb devices` empty). What is established without a device:

- **The shared fixes are the Android gains.** The phone goes through the same
  `RepositoryViewModel` and the same `JGitRepository`: the bounded `log()` and the filtered
  diff count **more** on ART than on desktop — a clone lives in `filesDir/repos` on flash
  storage, where hashing every tracked file on each refresh was the most expensive. All
  JGit is on `Dispatchers.IO`, the lanes layout on `Default`: nothing blocks the UI thread
  (checked in the code, to be confirmed visually on device).
- **`MainActivity.onCreate` is thin**: building the container (pure constructors) and
  `setContent`; reading `settings.json` runs on IO and the UI waits for `settingsLoaded`
  without blocking. No cold start lever in application code.
- **The demo runs in debug** (`installDebug`): debuggable build, hence no AOT and a
  noticeably slower Compose than in release. The cheapest lever for the demo: install the
  release variant — `isMinifyEnabled = false` means no R8 rule is involved, only a
  `signingConfig` (debug) is missing on the release build type, one line. Not done: H3 demo
  decision.
- **After the freeze** (unchanged since the first pass): R8 + baseline profile, with rules
  tested on ART (Compose, Koog, JGit).
- **To replay on device** (handoff): clone of a real repository, graph, review, apply —
  compare the feel before/after `a15abff` and note the cold start
  (`am start -W dev.nitpick.app/.MainActivity`).

## Verification

- First pass: `./gradlew :shared:jvmTest :server:test :desktopApp:assemble` **green**, with
  the new tests (topological sort under a reversed clock, diff on a clean tree, grouped
  `stage()`, diff counters, settings with no useless write). `check.sh` had not been able to
  finish: Android SDK missing on that machine.
- Second pass, machine with the SDK: `./scripts/check.sh` **green** (shared tests, desktop
  build and `:androidApp:assembleDebug`). Counts re-read in the reports: `jvmTest` **147
  tests**, `testAndroidHostTest` **107 tests**, 0 failures, 0 skipped. Still nothing on ART
  (no device): the Android proof remains `androidApp/src/androidTest` to be replayed with a
  device connected.

## Work note

The uncommitted diff already present at the start of the pass (`AppContainer`,
`LlmReviewAgent`, `ReviewToolbox`: tool call tracing) is **not** included in this commit: it
stayed in the working tree. The measurements and this pass did not touch the `GitRepository`
contract or the shared review format.
