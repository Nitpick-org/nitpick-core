# Architecture audit (issue #127)

Commit audited: b95f8bf (= origin/main 33e4f4f). Read-only; no code touched.

## 1. Boundaries (checked, nothing found)

| Check | Command | Result |
| --- | --- | --- |
| JGit outside `jvmSharedMain` | `grep -rl "org.eclipse.jgit" --include='*.kt' --include='*.kts' . \| grep -v jvmSharedMain` | empty |
| AWT/Swing outside jvm/desktop | `grep -rlE "^import (java\.awt\|javax\.swing)" --include='*.kt' . \| grep -vE "jvmMain\|desktopApp"` | empty |
| `android.` outside android | `grep -rlE "^import android\." --include='*.kt' . \| grep -vE "androidMain\|androidApp"` | empty |
| `commonMain` purity | `grep -rnE "^import (java\.\|javax\.\|android\.\|org\.eclipse)" shared/src/commonMain` | empty |
| Server imports | `grep -rhE "^import" server/src` | only `dev.nitpick.app.domain.*` (ReviewComment, ReviewResult, Severity, SharedReview) + Ktor |

Note: the repo has no `shared/src/jvmMain` nor `shared/src/androidMain` (`ls shared/src` = commonMain, commonTest, jvmSharedMain, jvmTest). All platform code lives in `desktopApp/src/main` and `androidApp/src/main`. The architecture tree in the team rules file mentions the missing dirs (stale, harmless).

## 2. Findings

### F1 (P1, pitch) The server classpath still carries all of `shared`
- `server/build.gradle.kts:8`: `implementation(project(":shared"))`; the comment on line 7 claims "the server never touches Compose, JGit or a LLM", but the dependency pulls them in transitively.
- `./gradlew :server:dependencies --configuration runtimeClasspath -q --offline` resolves about 357 unique coordinates (KMP `-jvm` plus metadata variants counted separately, so the real jar count is lower; the 188 jars of #107 was not re-measured on a distribution). Of those: 73 `ai.koog:*`, 1 `org.eclipse.jgit`, 86 Compose/androidx/JetBrains-androidx coordinates.
- Impact: a pitch claim of a slim server would be false. Not a demo breaker.
- Fix: post-freeze, `:domain` module (>1h). Pre-freeze: reword the comment at `server/build.gradle.kts:7` (15m) or do not claim it.

### F2 (P2) `GitRepository` contract smells
- Default bodies: `GitRepository.kt:30` `trackingStatus() = null`, `:33` `publishBranch() = throw UnsupportedOperationException`.
- `status()` has no production caller: `grep -rn "\.status()" --include='*.kt' .` hits only `JGitRepositoryTest.kt` (148, 162, 318, 623) and the fake. Still true.
- JGit `Git` never closed: `JGitRepositoryFactory.kt:24` (`Git.wrap`) and `:41` (clone) give it to `JGitRepository`, which is not `AutoCloseable` (`grep -rn "Closeable" shared/src` empty); only the bare-repo error path calls `repository.close()` (`:25`). Opening many repos in one session leaks file handles. Harmless for a short demo.
- `ReviewShareClient.get` has no production caller: `RepositoryViewModel.kt:382` uses `share`, `:413` uses `list`; only `KtorReviewShareClientTest.kt:70` calls `get`.

### F3 (P2) ViewModel / UDF
- `RepositoryViewModel.kt` is 671 lines with 21 functions; `RepositoryUiState` (`:78-120`) has 46 `val` properties (counted in the class header block).
- Callbacks: `grep -cE "on[A-Z][A-Za-z]*: " RepositoryScreen.kt PhoneFlow.kt` = 74 lambda parameters across the two files (#107 says "about twenty"). Evidence only.
- `AppContainer.kt` (902 B) is manual DI as the team rules ask; the ViewModel takes `shareClient: ReviewShareClient? = null` (`RepositoryViewModel.kt:159`).
- `git.refs()` / `git.trackingStatus()` still run inside `_state.update { }` at `RepositoryViewModel.kt:489-490` (fetch) and `:515-516` (push); the commit path was fixed (`:347-348`). `update` can replay its block, so a read-only git call may run twice. No corruption.

### F4 (P2) Dead code and unused resources
- `LocalReviewAgent`: used only as a default argument at `RepositoryViewModel.kt:158` and in `LocalReviewAgentTest`. Still true (`grep -rn LocalReviewAgent --include='*.kt' .`).
- `RefBadge` (`ui/components/NitpickReview.kt:161`): only used by `DesignSystemCatalog.kt:253-255`. Unused in product UI.
- Unused strings (no `Res.string.<key>` in any `.kt`; loop over `values/strings.xml`): `details_title`, `status_commits`, `commit_later`, `diff_files`, `review_nits`, `review_changed_files`, `review_phrases`, `commit_bar_applied`, `publish_in_progress` (9; #107 listed 3). Check `values-fr` when removing.
- Unused drawables (`grep -rE "Res\.drawable\.<name>\b"`): `ic_clock`, `ic_folder`, `ic_key`, `ic_wifi_off`, `ic_x` (5; #107 said nine).
- Theme tokens: `NitpickTokens.kt` defines 76 `val`s; per-token usage not computed (coverage gap).

### F5 (P2) Duplication
- `ShareResponse` defined twice: `server/.../ReviewRoutes.kt:28` (public) and `KtorReviewShareClient.kt:22` (private).
- `pushBranch` (`RepositoryViewModel.kt:530`) and `publishBranch` (`:504`) are near copies.
- `severityTone`: single definition (`NitpickReview.kt:175`), used at `:56` and `PhoneReviewScreen.kt:232`. Not duplicated.
- `selectedDiff`: derived property at `RepositoryViewModel.kt:147` (used by `DiffPanel.kt:59`), re-derived inline at `PhoneFlow.kt:187` without the fallback to the first file. Small desktop/phone behavior difference.
- Date label: `grep -rnE "fun (formatTime|relativeTime|.*[dD]ateLabel|.*Ago)"` finds nothing; not verified further.

### F6 (P1, submission) `ui-tooling` ships in the release APK
- `shared/build.gradle.kts:47-48`: `androidMain.dependencies { implementation(libs.compose.uiToolingPreview); implementation(libs.compose.uiTooling) }`. `androidApp/build.gradle.kts:19` already uses `debugImplementation`. Fix: keep it debug-only, run `./scripts/check.sh` (15m). Not run here.

### F7 (P2) Settings store
- `FileSettingsStore.kt:35-37`: `tmp.writeText(...)` before `setReadable(false,false)` / `setReadable(true,true)`, so the API key sits with default permissions for an instant. Fix: create the file owner-only first (15m).
- `SettingsViewModel.kt:36`: `store.save(next)` is not wrapped in try/catch.

## 3. #107 triage

Rule: pre-freeze only if < 30 min and it touches the submission. H2: triage only, no refactor.

| # | Item | Status on b95f8bf | Evidence | Pre-freeze? |
| --- | --- | --- | --- | --- |
| 1 | ViewModel does four jobs, 37-field state, ~20 callbacks | still true (46 fields, 74 callback params) | F3 | no |
| 2 | Logic in `ConnectScreen`; `ForgeClient` in `settings/` | still true | `ui/phone/ConnectScreen.kt:92` `rememberCoroutineScope`; `settings/ForgeClient.kt` | no |
| 3 | Server ships all of `shared` | still true | F1 | reword comment only (optional) |
| 4a | `GitRepository` default bodies | still true | `GitRepository.kt:30,33` | no |
| 4b | `status()` never called in production | still true | F2 | no |
| 4c | JGit `Git` never closed | still true | F2 | no |
| 5 | `LocalReviewAgent` dead in production | still true | F4 | no |
| 6 | `afk-ralph.sh` always exits | still true | `scripts/afk-ralph.sh:46` `[ -f package.json ]` | won't fix (AFK not in submission) |
| 7 | `format-edited.sh` never formats Kotlin | still true | extension `case` is TS/JS/MD/JSON only, plus `package.json` guard | won't fix |
| 8 | CI workflow commented out | still true | `.github/workflows/ci.yml` starts `# name: CI` | won't fix; do not claim CI in the pitch |
| 9 | `RealReviewCallTest` paid call | not re-verified | gap | n/a |
| 10 | `check.sh --quiet` hides count; on-device tests not compiled | not re-verified | gap | n/a |
| 11 | Apply on CRLF writes LF | still true | `ReviewAnchoring.kt:17,22-24` split/join on `\n` | no |
| 12 | `ShareResponse` twice | still true | F5 | no |
| 13 | `ui-tooling` in release APK | still true | F6 | optional, 15m |
| 14 | Unused strings/tokens/icons | still true (strings 9, icons 5) | F4 | no |
| 15 | Back on phone Connect closes the app | likely fixed, not run on device | `PhoneFlow.kt:103` `BackHandler(enabled = screen != PhoneScreen.Repos) { screen = screen.parent }` | QA check only |
| 16 | `SettingsViewModel.update` no catch | still true | F7 | no |
| 17 | `LlmReviewAgent` prompt/parse on caller dispatcher | still true | `grep -n "withContext\|Dispatchers" review/LlmReviewAgent.kt` empty | no |
| 18 | Nits are cards, not inline in diff | not re-verified (product decision) | gap | pitch wording |
| 19 | `refs()`/`trackingStatus()` inside `update` | partly fixed (commit path); fetch and push still true | F3 | no |
| 20 | `pushBranch`/`publishBranch` copies | still true | F5 | no |
| 21 | `merge.ff=only` diverged answers "Aborted" | not re-verified | gap | n/a |
| 22 | Apply/Commit enabled during pull/checkout | not re-verified | gap | n/a |
| 23 | `FileSettingsStore.save` permission window | still true | F7 | optional, 15m |

## 4. Pitch numbers

Method: `.kt` files only, main source sets (tests, `commonTest`, `jvmTest`, `androidTest`, `server`, build scripts and resources excluded), raw lines including blanks and comments, via `find <dir> -name '*.kt' | xargs cat | wc -l`.

| Source set | Lines |
| --- | --- |
| `shared/src/commonMain` | 8,181 |
| `shared/src/jvmSharedMain` | 506 |
| **Shared (commonMain + jvmSharedMain)** | **8,687** |
| `desktopApp/src/main` | 64 |
| `androidApp/src/main` | 29 |
| **Platform-only (desktop + android apps)** | **93** |
| Total | 8,780 |
| **Shared %** | **98.9%** (8,687 / 8,780) |

The previous 99.0% (7,960 / 8,044) recomputes to 98.9%. `jvmSharedMain` (JGit) is shared by desktop and Android but is JVM-only, not Kotlin Multiplatform common code: commonMain alone is 93.2% (8,181 / 8,780). Say "98.9% shared across both targets" or "93% common", not "99% multiplatform". There is no `jvmMain`/`androidMain` in `shared`. Server: about 357 resolved runtime coordinates, not a jar count; the 188 jars figure was not reproduced.

## 5. Coverage gaps
- Server size measured as resolved coordinates, not distribution jars (no `installDist`, timebox).
- Theme token usage, date-label duplication, triage items 9, 10, 21, 22 not checked.
- No device run for Back on the Connect screen.
- Unused-strings check matches only static `Res.string.<key>` references.

```json
{"lane":"architecture","findings":[{"severity":"P1","title":"Server runtime classpath still carries Koog, JGit and Compose via project(:shared); build comment claims otherwise","evidence":"server/build.gradle.kts:7-8; ./gradlew :server:dependencies --configuration runtimeClasspath: ~357 coordinates incl. 73 ai.koog, 1 jgit, 86 compose/androidx","impact":"pitch","fix":"Do not claim a slim server in the pitch; reword comment at server/build.gradle.kts:7; :domain module post-freeze","cost":">1h","confidence":"high"},{"severity":"P1","title":"ui-tooling is an implementation dependency of shared androidMain, so it ships in the release APK","evidence":"shared/build.gradle.kts:47-48 (androidApp/build.gradle.kts:19 already uses debugImplementation)","impact":"submission","fix":"Keep ui-tooling debug-only, run check.sh","cost":"15m","confidence":"medium"},{"severity":"P2","title":"Dead contract surface: GitRepository.status(), ReviewShareClient.get, LocalReviewAgent default, 9 unused strings, 5 unused icons","evidence":"grep '\\.status()' only in JGitRepositoryTest; KtorReviewShareClientTest.kt:70 only caller of get; RepositoryViewModel.kt:158; strings.xml keys without Res.string refs","impact":"score","fix":"Remove post-freeze","cost":"1h","confidence":"high"},{"severity":"P2","title":"JGit Git object never closed (JGitRepository not Closeable)","evidence":"JGitRepositoryFactory.kt:24,41; no Closeable in shared/src","impact":"score","fix":"Make JGitRepository AutoCloseable and close on repo switch","cost":">1h","confidence":"medium"},{"severity":"P2","title":"RepositoryViewModel 671 lines, 46-field state, 74 callback params; git calls still inside _state.update in fetch and push","evidence":"RepositoryViewModel.kt:78-120, 489-490, 515-516; grep on RepositoryScreen.kt and PhoneFlow.kt","impact":"score","fix":"Split post-freeze; hoist git calls out of update","cost":">1h","confidence":"high"},{"severity":"P2","title":"ShareResponse duplicated on client and server; push/publish near copies; selectedDiff re-derived inline on phone without fallback","evidence":"ReviewRoutes.kt:28 vs KtorReviewShareClient.kt:22; RepositoryViewModel.kt:504,530; PhoneFlow.kt:187 vs RepositoryViewModel.kt:147","impact":"score","fix":"Post-freeze dedupe","cost":"1h","confidence":"high"},{"severity":"P2","title":"FileSettingsStore writes API key with default permissions before restricting; SettingsViewModel.update does not catch save failure","evidence":"FileSettingsStore.kt:35-37; SettingsViewModel.kt:36","impact":"score","fix":"Create tmp file owner-only first; wrap save in try/catch","cost":"15m","confidence":"high"}],"coverage_gaps":["Server size measured as resolved coordinates, not distribution jars","Theme token usage, date-label duplication, triage items 9, 10, 21, 22 not checked","Phone Connect Back behavior not run on a device","Unused-strings check only matches static Res.string.<key> references"]}
```
