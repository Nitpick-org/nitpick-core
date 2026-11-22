# Spec compliance audit — `origin/main` @ 54c37a2

Audited commit: `54c37a2` (Merge PR #121). Read via a detached worktree at `/tmp/nitpick-main`.
**Not** the working tree (`audit/106-cleanup` @ 68a8273, 39 commits behind).
Read-only audit; every line number below refers to `54c37a2`.

Method: static read of `docs/CONTEXT.md` §2/§6/§7/§7.1/§8/§12/§13, `docs/JETBRAINS-CHALLENGE.md`
§3/§5, `docs/LAUZHACK.md`, `docs/DEMO.md`, then each claim traced to code, test names and
screenshots. Tests were **not executed** (read-only + timebox): status is inferred from test
names and code paths, not from a run. See `coverage_gaps`.

---

## 1. Matrix — §8 MUST / SHOULD / WON'T, bonus features, process

### MUST (§8)

| # | Requirement | Status | Evidence |
| --- | --- | --- | --- |
| M1 | Open a local repo (desktop) or clone HTTPS + token (Android **and** desktop) | **PASS** | `shared/src/commonMain/kotlin/dev/nitpick/app/ui/App.kt:23-24` (folder picker seam), `desktopApp/src/main/kotlin/dev/nitpick/app/DirectoryPicker.kt`, `shared/.../ui/repository/CloneDialog.kt:1-156`, `shared/src/jvmSharedMain/.../git/JGitRepositoryFactory.kt`, phone entry `shared/.../ui/phone/CloneScreen.kt:99`; tests `CloneCredentialsTest`, `CloneFolderNameTest`, `JGitRepositoryTest` (`clonesALocalRepositoryAndReadsItsLog`); screenshot `docs/demo/android-02-clone-form.png` |
| M2 | Commit graph with lanes + colours, branches and tags shown, commit selection with details | **PASS** | Lanes `shared/.../graph/CommitGraph.kt` + `ui/repository/CommitGraph.kt:94` (row text), branches/tags listed in the Refs sidebar `ui/repository/RepositoryScreen.kt:280` (`RefsSidebar`), not drawn on graph rows (the `RefBadge` component is only used by the internal design catalog), details `ui/repository/CommitGraph.kt:172-186`; tests `CommitGraphTest`, `CommitGraphDrawingTest`; screenshot `docs/demo/android-08-pushed-graph.png` |
| M3 | Diff vs base with add/remove colouring | **PASS** | `ui/repository/DiffPanel.kt:218-231` (`@@` header, `+n`/`−n` counts), `domain/Diff.kt`, `diff/UnifiedDiffParser.kt`; tests `UnifiedDiffParserTest` |
| M4 | **Review → inline comments → apply suggestion → commit** (the loop that wins the demo) | **PARTIAL** | Loop works: `RepositoryViewModel.kt:262-290` (`applySuggestion`), `:330-360` (`commitAppliedSuggestions`), UI `ui/repository/ReviewPanel.kt:142-167`, `ui/components/NitpickReview.kt:96-102`. **Not inline**: nits are a separate list in the Review tab (`ReviewPanel.kt:111-158`); `DiffPanel.kt` contains **zero** reference to comments/nits (grep), so the diff never shows a nit on its line. §7 step 6 and §13 step 3 claim inline nits; `README.md:31` also says "inline nits". `docs/DEMO.md` itself does not use the word "inline". |
| M5 | Light/dark theme + EN/FR localisation | **PASS** | `settings/Settings.kt:8` (`ThemeMode`), `ui/theme/NitpickTheme.kt:40-48`, selector `ui/settings/SettingsScreen.kt:58,71`, applied `ui/App.kt:82`; strings `composeResources/values/strings.xml` 177 `<string>` + 5 `<plurals>`, `values-fr/strings.xml` **same counts**; `CountStringsTest`; hardcoded literals are non-translatable only (`"Nitpick"`, `"›"`, `"@@"`, `+n`/`−n`) |
| M6 | Runs on desktop **and** Android | **PASS** | `desktopApp/.../main.kt:11-25`, `androidApp/src/main/kotlin/dev/nitpick/app/MainActivity.kt`, ART tests `JGitOnDeviceTest` (2 tests), `KoogClientOnDeviceTest`, `ReviewShareOnDeviceTest`; screenshots `docs/demo/android-01..10-*.png` |
| M7 | Ktor backend: share from desktop, open on Android (§7.1) | **PARTIAL** | Server: `server/.../ReviewRoutes.kt:31-55` (`POST /reviews`, `GET /reviews?repoUrl`, `GET /reviews/{id}`, `/health`), `ReviewStore.kt` (memory + JSON file), 9 tests in `ReviewServerTest`. Client `share/KtorReviewShareClient.kt`, UI `ui/repository/ShareReview.kt:40-55` + list `:61-125`, phone screen `ui/phone/PhoneSharedReviewsScreen.kt`. **Missing end-to-end proof**: no screenshot or test shows a review shared on desktop and *opened* on Android; `ReviewShareOnDeviceTest.aStoppedServerIsACleanError` only proves the offline error path. |

### SHOULD (§8)

| # | Requirement | Status | Evidence |
| --- | --- | --- | --- |
| S1 | fetch, pull, push, checkout | **PASS** | `RepositoryViewModel.kt:476-620` (`fetchBranch`, `publishBranch`, `pushBranch`, `pullBranch`, `checkout`), `JGitRepository.kt:234-330`; tests `JGitRepositoryTest` (`aDirtyTreeIsNotResetWhenThePullDiverged`, rebase config test at `:506-507`); UI `ui/repository/RepositoryScreen.kt:227-262` |
| S2 | `readFile`/`listFiles` tools for the agent | **PASS** | `review/ReviewToolbox.kt:59,74,91,94` (path-jailed: `isReviewablePath`, no `..`/absolute), `KoogLlmClient.kt:42` implements `ToolCallingLlmClient`, prompt advertises tools only when available `review/ReviewPrompt.kt:47-56,59-60`; tests `ReviewToolboxTest` |
| S3 | Global summary and score **or per-severity count** | **PARTIAL** | Summary shown `ReviewPanel.kt:123`; total nits + changed files `:126` (`plurals review_nits`, `review_changed_files`). **No per-severity count and no score**: grep `score` in `shared/src/commonMain` returns nothing; no severity aggregation in UI. The `"N nits · M bug"` format (CONTEXT §1 identity) is produced only by `review/LocalReviewAgent.kt:35`, which is **not wired in production** (`AppContainer.kt:19` uses `LlmReviewAgent(KoogLlmClient)`). |
| S4 | Streaming **or** progress indicator during review | **PASS** | Indeterminate loader `ReviewPanel.kt:81,175-185` (`NitpickReviewLoader`), 90 s timeout with Retry `RepositoryViewModel.kt:44,274-281`. No streaming, no percentage — allowed by the "ou". |
| S5 | Adaptive layout: side-by-side desktop, screen navigation Android | **PASS** | 3-panel desktop `ui/repository/RepositoryScreen.kt:172` (+ inspector width), tabs `:547-610`; Android screen stack `ui/phone/PhoneFlow.kt:37-49,99-167` |

### WON'T (§8) — must be untouched

| # | Requirement | Status | Evidence |
| --- | --- | --- | --- |
| W1 | No interactive conflict resolution, no interactive rebase, no stash UI, no submodules, no LFS, no SSH | **PASS** (nothing started) | grep `submodule\|stash\|rebase\|ssh` over `shared/src`, `server/src`: only a *test* asserting Nitpick always merges (`JGitRepositoryTest.kt:506-507`) and the unrelated `isSharing` field. `PullResult` reports a conflict, never merges (`RepositoryViewModel.kt:556` comment + `JGitRepository.pull`). |
| W2 | No iOS / web | **PASS** | Targets in `shared/build.gradle.kts`: android + jvm only; no `iosMain`/`jsMain` source set |
| W3 | No accounts/auth/DB/cloud, no server-side review, no OAuth | **PARTIAL** | Server: in-memory list + one JSON file, no auth route (`ReviewRoutes.kt:33-54`) — PASS. But the Android app ships a GitHub/GitLab OAuth device flow (`settings/ForgeClient.kt`, `ui/phone/ConnectScreen.kt`, `progress/64-device-flow.md`), while §8 lists "Authentification OAuth GitHub" as WON'T. Scope addition, not a server feature; document it or remove it from the pitch. |
| W4 | No runtime Air/Junie integration | **PASS** | No `ai.jetbrains.air`/junie dependency in `gradle/libs.versions.toml`; only Koog (`:21,48,50`) |

### Bonus features (§2, `JETBRAINS-CHALLENGE.md` §3) — all five declared "obligatoires pour nous"

| # | Bonus | Status | Evidence |
| --- | --- | --- | --- |
| B1 | Dark/light | **PASS** | `ThemeMode` SYSTEM/LIGHT/DARK + manual toggle, see M5 |
| B2 | Localisation | **PASS** | 177 strings + 5 plurals, EN=FR counts, `CountStringsTest` |
| B3 | Compose Multiplatform | **PASS** | UI entirely in `commonMain`; `./scripts/count-lines.sh` at 54c37a2: shared 7960 / 8044 production lines = **99.0 %**, platform-specific 84 |
| B4 | Koog shared AI logic | **PASS** | `gradle/libs.versions.toml:21` (`koog = "1.3.0"`), `shared/build.gradle.kts:64-65`, `review/KoogLlmClient.kt:42`, providers Anthropic/OpenAI/Google/Ollama (`KoogLlmClient.kt:8-17,147-151`), settings `settings/Settings.kt:6,29-31` + `settings/LlmModels.kt` |
| B5 | Kotlin/Ktor backend | **PASS** | `server/` module, `server/src/main/kotlin/dev/nitpick/server/Main.kt`, `ReviewRoutes.kt`, `ReviewStore.kt`, `ReviewServerTest` (9 tests) |
| B6 | Process: JetBrains Air + multi-agent (§2, §9, challenge §5) | **PARTIAL** | `docs/AGENT_LOG.md` and `docs/REVIEW_LOG.md` are still empty templates (10 and 8 lines, 0 rows), and there is no Air capture in `docs/demo/` (15 files, all product captures + dogfooding). **But a real multi-agent comparison exists** in `progress/16-graph.md:37-52`: two subagents, a comparison table, and the decision to keep approach A. Dogfooding is also real (`docs/demo/dogfooding.md`, 2 nits, 1 true bug, capture). The gap is that neither is surfaced in the process logs or the pitch, and Air itself is unproven. |

---

## 2. §6 contracts

### `GitRepository` — `shared/src/commonMain/kotlin/dev/nitpick/app/git/GitRepository.kt`

| Member | In §6 spec | Implemented in `JGitRepository` | Used in production | Note |
| --- | --- | --- | --- | --- |
| `log(limit=500)` | yes | `:49` | `RepositoryViewModel.kt:206`, `review/ReviewContext.kt:90` | used |
| `refs()` | yes | `:96` | `RepositoryViewModel.kt:209,347,489,515,538,567` | used |
| `currentBranch()` | yes | `:103` | `RepositoryViewModel.kt:212,611`, `ReviewContext.kt:85` | used |
| `remoteUrl()` | **no — added** | `:108` | `RepositoryViewModel.kt:467` | necessary for §7.1 keying; contract drift not recorded in CONTEXT |
| `status()` | yes | `:112` | **none** | **implemented, never called** outside `JGitRepository`'s own internals; `WorkingTreeStatus` never reaches the UI. Dead contract surface. |
| `diffAgainst(base)` | yes | `:138` | `RepositoryViewModel.kt:252`, `LocalReviewAgent.kt:23`, `ReviewContext.kt:75` | used |
| `readFile(path)` | yes | `:122` | `RepositoryViewModel.kt:297,649`, `ReviewToolbox.kt:94`, `ReviewContext.kt:78,82` | used |
| `listFiles()` | yes | `:127` | `ReviewToolbox.kt:59,91`, `ReviewContext.kt:80` | used |
| `writeFile(path,content)` | yes | `:207` | `RepositoryViewModel.kt:299` | used |
| `stage(paths)` | yes | `:214` | `RepositoryViewModel.kt:342` | used |
| `commit(msg)` | yes | `:226` | `RepositoryViewModel.kt:343` | used |
| `fetch(creds)` | yes | `:234` | `RepositoryViewModel.kt:484` | used |
| `pull(creds)` | yes | `:240` | `RepositoryViewModel.kt:555` | used |
| `push(creds)` | yes | `:272` | `RepositoryViewModel.kt:536` | used |
| `checkout(branch)` | yes | `:311` | `RepositoryViewModel.kt:604` | used |
| `trackingStatus()` | **no — added, default body `= null`** (`:30`) | `:301` | `RepositoryViewModel.kt:217,348,490,516,538,568` | default body hides a missing impl instead of failing; overridden by JGit |
| `publishBranch(creds)` | **no — added, default body `throws UnsupportedOperationException`** (`:33`) | `:278` | `RepositoryViewModel.kt:510` | fails at runtime rather than at compile time for any other impl |

Verdict: 15/15 spec methods implemented and used except **`status()` (unused)**; 3 additions beyond
the frozen contract (`remoteUrl`, `trackingStatus`, `publishBranch`), 2 of them with default bodies.
`FakeGitRepository` (`shared/src/commonTest/.../git/FakeGitRepository.kt`) exists for tests.

### Review models — `shared/src/commonMain/kotlin/dev/nitpick/app/domain/Review.kt`

| Field | Used | Evidence |
| --- | --- | --- |
| `ReviewResult.summary` | yes | `ReviewPanel.kt:123` |
| `ReviewResult.comments` | yes | `ReviewPanel.kt:126,142` |
| `ReviewResult.toolUses` (**added**) | yes | `ReviewPanel.kt:130-133`; filled `LlmReviewAgent.kt:47` |
| `ReviewComment.id` | yes | `ReviewPanel.kt:142-143`, `RepositoryViewModel.kt:281,335` |
| `.filePath`, `.startLine` | yes | `NitpickReview.kt:61`, `ReviewPanel.kt:149-150` |
| `.endLine` | yes | `ReviewAnchoring.kt:32,41` |
| `.severity` | yes | `NitpickReview.kt:56,176-187`, sorting `LlmReviewAgent.kt:46` |
| `.message` | yes | `NitpickReview.kt:77` |
| `.originalSnippet` | **partially** | used for anchoring (`ReviewAnchoring.kt:18,29,39`) and apply (`:20`), **never displayed** → no "before" half of the §12 before/after block |
| `.suggestion` | yes | `NitpickReview.kt:78-102`, `ReviewAnchoring.apply` `:14-25` |
| `.category` (**in spec**) | **no display** | only copied in `ReviewResponseParser.kt:55`; no UI consumer (`grep .category` → parser only) |
| `.title` (**added**) | yes | `ui/repository/CommitMessage.kt:13` |
| `isFileLevel` (**added**) | yes | `ReviewPanel.kt:154` |

### `SharedReview` / `ReviewShareClient`

`SharedReview` (`domain/SharedReview.kt:6-14`) has all 6 spec fields; `id` defaults to `""` for the
POST. Use: `id` (store/route), `repoUrl` (server key + `normalizeRepoUrl`), `branch` (switch before
anchoring, `RepositoryViewModel.kt:437`), `result` (rendered). **`baseRef` and `headCommit` are
written (`RepositoryViewModel.kt:394-396`) and stored but never read** by app or server — carried
data, no consumer.

`ReviewShareClient` (`share/ReviewShareClient.kt:6-10`): `share()` used (`RepositoryViewModel.kt:389`),
`list()` used (`:414`), **`get(id)` implemented (`KtorReviewShareClient.kt:54`) but never called by
the product**; only the server route has a test (`ReviewServerTest.anUnknownReviewIsNotFound`).
Credential scrubbing before upload is a real safeguard: `withoutCredentials` `ReviewShareClient.kt:15-18`.

---

## 3. §7 flow, step by step

| Step | Status | Evidence |
| --- | --- | --- |
| 1. Branch + base choice (main default) | **PASS** | `RepositoryUiState.baseRef = "main"` (`RepositoryViewModel.kt:86`), `defaultBaseRef(refs)` `:216`, `selectBase` `:232` |
| 2. Compute `diffAgainst(base)` | **PASS** | `refreshDiff()` `:242-265`, coalesced single job (`pendingDiffBase`) |
| 3. Build repo context (tree truncated, full changed files, README, 10 commits, language) | **PASS** | `review/ReviewContext.kt:74-93`, limits `:6-11` (`MAX_FILES=20`, tree 200, README 2000), lockfiles/binaries/generated + `.env`/keys filtered `:13-27`, language `:61-63`; test `ReviewContextTest` |
| 4. Koog agent → structured `ReviewResult` with retries/repair | **PASS (with repair)** | `review/KoogLlmClient.kt` (Koog 1.3.0, 4 providers, tool rounds), parse `ReviewResponseParser.kt`, one prose-repair retry `LlmReviewAgent.kt:30-41`; 90 s timeout + Retry `RepositoryViewModel.kt:274-281`; tests `KoogLlmClientTest`, `ReviewResponseParserTest`, `LlmReviewAgentTest`, on-device `KoogClientOnDeviceTest` |
| 5. Anchor validation: snippet ↔ lines start..end, relocate, else file-level, never crash | **PASS** | `ReviewAnchoring.kt:36-45` (`anchorOrFileLevel`), `:51-57` (nearest match), `:62` (file-level fallback), applied at `LlmReviewAgent.kt:42-46`; tests `ReviewAnchoringTest`; UI renders `startLine==0` as "whole file" `NitpickReview.kt:61,82` |
| 6. Diff + inline comments, sorted by severity | **PARTIAL** | Sorted by severity `LlmReviewAgent.kt:46`; comments rendered **in a separate Review tab list**, not inline in the diff (`ReviewPanel.kt:111-158`; `DiffPanel.kt` has no comment code) |
| 7. Apply: re-verify lines, replace, write file, refresh diff, offer stage+commit; **Dismiss** and **"Apply all for this file"** | **PARTIAL** | Apply `RepositoryViewModel.kt:283-305` (`ReviewAnchoring.apply` throws if the snippet moved → refuses rather than corrupts), write+refresh `:302`, stage+commit bar `ReviewPanel.kt:160-167`; Dismiss `RepositoryViewModel.kt:307-311` (`review_dismiss`). **"Apply all for this file" does not exist**: `grep -i "applyall\|apply_all\|apply all"` over `shared/`, `server/`, `desktopApp/`, `androidApp/` → 0 hits, and no resource string. Commit message is auto-derived from applied nits (`CommitMessage.kt:11-13`). |

### §7.1 backend

| Requirement | Status | Evidence |
| --- | --- | --- |
| `POST /reviews`, `GET /reviews?repoUrl=…`, `GET /reviews/{id}`, JSON + ContentNegotiation | **PASS** | `ReviewRoutes.kt:32,36-53` |
| Store in memory + JSON file (survives restart) | **PASS** | `ReviewStore.kt:20-40` (atomic-ish write before memory commit), test `reviewsSurviveARestart` |
| "Share review" button after a review | **PASS** | `ReviewPanel.kt:139` → `ShareReview.kt:40-55`; desktop `App.kt:105`, Android `PhoneReviewScreen.kt:169` |
| Shared reviews screen (branch, commit, nit count), both targets | **PASS** | list+row `ShareReview.kt:61-125` (`shared_row` = `%1$s · %2$s · %3$d nits`), desktop in Review tab `ReviewPanel.kt:94`, Android `PhoneFlow.kt:133,161` + `PhoneSharedReviewsScreen.kt` |
| Opening a shared review re-anchors, no LLM call | **PASS** | `openSharedReview` `RepositoryViewModel.kt:426-457` (checkout → `reanchor` → render), shared badge `ReviewPanel.kt:120` |
| Server URL in settings, `http://localhost:8080` default | **PASS** | `settings/Settings.kt:41` (`DEFAULT_SERVER_URL`), `ui/settings/SettingsScreen.kt` server field, client built from it `AppContainer.kt:20` |
| **Works without a server**: sharing greyed out + clear error | **PARTIAL** | Clear error: typed `ReviewShareException` → `ShareFailure.Unreachable` → `share_unreachable` string (`strings.xml:182`, FR `values-fr/strings.xml:182`), never crashes (`RepositoryViewModel.kt:463-476`); `ReviewShareOnDeviceTest.aStoppedServerIsACleanError`. **Not greyed out**: the button stays enabled (`ShareReview.kt:48`, `enabled = !state.isSharing`); the failure only surfaces after a click. No disabled state, no pre-flight reachability. |

---

## 4. Demo claims vs code

| Claim (source) | Verdict | Evidence |
| --- | --- | --- |
| "les nits apparaissent **inline**, dont un vrai bug" (CONTEXT §7 step 6 / §13 step 3; README:31) | **Overstated** | Nits live in the Review inspector tab, not on diff lines: `ReviewPanel.kt:111-158` vs `DiffPanel.kt` (no comment code). Demo must say "in the review panel, with file:line", not "inline in the diff". |
| "7 nits · 1 bug" nit/bug counter (CONTEXT §1 identity) | **Not in production** | Only `LocalReviewAgent.kt:35` (not wired: `AppContainer.kt:19`). Production shows summary sentence + "N nits on N changed files" (`ReviewPanel.kt:123-129`). No per-severity count, no score. |
| Apply-all | **Absent** | 0 code/string hits (see §7 step 7). Only per-nit Apply. |
| Before/after suggestion block (§12) | **Half** | `NitpickReview.kt:78-104` shows "SUGGESTED CHANGE" (`strings.xml:208`) + after code; the "before" (`originalSnippet`) is never rendered. |
| Keyboard shortcuts (§12 "fetch, review, appliquer la suggestion") | **Absent** | No `onPreviewKeyEvent`/`onKeyEvent` anywhere in `shared/`, `desktopApp/`, `androidApp/`. The only shortcut artefact is a **decorative tooltip** `"⌘⇧R"` in the internal design-system catalog (`ui/designsystem/DesignSystemCatalog.kt:151`); real toolbar tooltips pass no `shortcut` (`RepositoryScreen.kt:227,233,244,259`). A visible `⌘⇧R` hint with no handler is a live demo risk — do not point at it. |
| Share end-to-end (desktop → server → Android) | **Unproven** | Code paths + server tests exist; no screenshot/test of the Android side *opening* a desktop-shared review (`ReviewShareOnDeviceTest` covers the offline error only). §7.1 plan B for LLM latency is therefore **undemonstrated**. |
| Provider support (§7 "Fournisseurs") | **PASS** | 4 providers `KoogLlmClient.kt:8-17,147-151`, model lists `settings/LlmModels.kt`, key storage expect/actual `jvmSharedMain/.../settings/FileSettingsStore.kt` + android actual; missing-key UX `ReviewPanel.kt:194-200` |
| Demo screenshots exist and are current | **Followable, not verified** | `docs/demo/` has 13 PNGs + `android-01..10` path matching `DEMO.md`; commit `cb55041`/`f91a419` referenced in `DEMO.md` are **not** in this repo's history (they are in the demo repo, presumably) — unverifiable here. Images not opened (see gaps). |
| §13 step 6 "shared vs specific" | **PASS** | `scripts/count-lines.sh` runs: 7960/8044 = 99.0 % shared, 84 platform lines (fresh number; `DEMO.md` quotes the stale 98.2 % / 4 466/4 546) |
| §13 step 7 Air captures + multi-agent comparison | **PARTIAL** | No Air capture and empty `AGENT_LOG.md`/`REVIEW_LOG.md`, but a real two-agent comparison exists in `progress/16-graph.md:37-52`; the gap is surfacing it, not its absence. |

---

## 5. Top 10 gaps

1. **No keyboard shortcuts at all** (§12) while a `⌘⇧R` hint is displayed in the catalog component — a judge could ask and a demo could fail on stage.
2. **Nits are not inline in the diff** (§7 step 6, §8 MUST "commentaires inline", demo line 3) — they are a side list in the Review tab.
3. **"Apply all for this file" missing** (§7 step 7) — only per-nit Apply.
4. **Process criterion is partial**: `AGENT_LOG.md`/`REVIEW_LOG.md` are empty and there is no Air capture, but a real two-agent comparison exists in `progress/16-graph.md:37-52`, and dogfooding is captured. The gap is surfacing both in the logs and the pitch; it is not zero.
5. **Share end-to-end is unproven on Android** — the §7.1 plan B (open a desktop review on the phone without the LLM) has no capture/test; only the offline-error path is device-tested.
6. **No-severity count / no score** (§8 SHOULD) and the advertised "N nits · 1 bug" format is produced by the *unwired* `LocalReviewAgent`, not production.
7. **No before/after block** (§12) — `originalSnippet` is never shown, only the suggested replacement.
8. **Share button not greyed without a server** (§7.1) — error only after a click; no disabled/pre-flight state.
9. **Dead/unused contract surface**: `GitRepository.status()` never called; `SharedReview.baseRef`/`headCommit` never read; `ReviewShareClient.get(id)` never called; `ReviewComment.category` never displayed. Also 3 members beyond the frozen §6 contract, 2 with default bodies (`trackingStatus`, `publishBranch`).
10. **Stale/overstated demo doc**: `DEMO.md` quotes 98.2 % shared (actual 99.0 %); `README.md:31` and CONTEXT §7/§13 promise "inline" nits, apply-all and before/after that the code does not have — fix the doc or the code *before* the run-through, not during the pitch.

---

## 6. Requirements with no evidence

- **B6 / §2 process criterion (Air, multi-agent)**: no entry in `AGENT_LOG.md`/`REVIEW_LOG.md` and no Air capture. A multi-agent comparison *does* exist (`progress/16-graph.md:37-52`) and dogfooding is captured (`docs/demo/dogfooding.md`); they are not surfaced in the process logs or the pitch.
- **M7 acceptance**: "desktop shares → Android opens the shared review" — no test, no screenshot, no run report.
- **S3 per-severity counter / score**: no code path produces it.
- **§12 keyboard shortcuts**: no code at all.
- **§7 step 7 "apply all for this file"**: no code at all.
- **§12 before/after**: "before" half has no UI.
- **Nothing about the demo path itself was executed** in this audit (no build, no app launch), so all PASS verdicts are static-code verdicts; screenshots were listed but not visually inspected.

## 7. Coverage gaps

- Tests **not executed** (read-only, 10-minute box): 191 `@Test` across `shared/src`, `server/src`, `androidApp/src` counted, none run; no `check.sh` output used. Every "PASS on tests" claim is name/code-based.
- Screenshots in `docs/demo/` were **not opened** — their content is unverified; only existence/filenames checked.
- `docs/design/UI-HANDOFF.md` §12 mapping was **not read** (file read skipped for time); §12 verdicts come from `CONTEXT.md` only.
- `docs/LAUZHACK.md` was grepped for submission constraints (3-min demo, no slides, public repo + README, submit before 12:00) but not read end-to-end; no check that the README/repo meets the public-link rule, and no check of the submission form fields.
- No device/emulator run; Android verdicts are static (plus existing screenshots).
- Only `origin/main` @ 54c37a2 audited; the `audit/106-cleanup` tree (39 ahead) may already fix some of these — re-check before acting.

---

```json
{
  "lane": "spec",
  "findings": [
    {
      "severity": "P1",
      "title": "No keyboard shortcuts, yet a ⌘⇧R hint is displayed",
      "evidence": "grep onPreviewKeyEvent/onKeyEvent in shared/, desktopApp/, androidApp/ = 0 hits; decorative hint ui/designsystem/DesignSystemCatalog.kt:151; real toolbar tooltips pass no shortcut (RepositoryScreen.kt:227,233,244,259)",
      "impact": "demo",
      "fix": "Add desktop onPreviewKeyEvent: ⌘⇧R review, ⌘F fetch, ⌘⏎ apply top suggestion; or delete the ⌘⇧R hint so no docs/UI promise it",
      "cost": "1h",
      "confidence": "high"
    },
    {
      "severity": "P1",
      "title": "Review nits are not inline in the diff (separate Review tab list)",
      "evidence": "ReviewPanel.kt:111-158 renders comments in a LazyColumn; DiffPanel.kt has no comment/nit reference; README.md:31 and CONTEXT §7 step 6 / §13 step 3 claim inline (DEMO.md does not use the word).",
      "impact": "demo",
      "fix": "Either render nit badges on DiffLineRow (comment.startLine match) or reword the demo to 'nits in the review panel, anchored at file:line'",
      "cost": ">1h",
      "confidence": "high"
    },
    {
      "severity": "P1",
      "title": "Process criterion is partial — comparison exists, logs and Air do not",
      "evidence": "AGENT_LOG.md and REVIEW_LOG.md are empty; no Air capture in docs/demo/ (15 files). But progress/16-graph.md:37-52 documents a real two-agent comparison (table, decision), and docs/demo/dogfooding.md + dogfooding-104-review.png prove dogfooding.",
      "impact": "score",
      "fix": "Fill AGENT_LOG/REVIEW_LOG from progress/*.md, add the comparison and dogfooding to the pitch, and add an Air capture only if a real Air session exists; do not claim Air otherwise.",
      "cost": "1h",
      "confidence": "high"
    },
    {
      "severity": "P1",
      "title": "Share end-to-end (desktop → Ktor → Android) is unproven",
      "evidence": "code + 9 ReviewServerTest tests exist (ReviewRoutes.kt:31-55, ReviewStore.kt); ReviewShareOnDeviceTest only asserts aStoppedServerIsACleanError; no screenshot of a shared review opened on the phone",
      "impact": "demo",
      "fix": "Replay and capture: share on desktop, `adb reverse tcp:8080 tcp:8080`, open Shared reviews on Android, screenshot both screens",
      "cost": "1h",
      "confidence": "high"
    },
    {
      "severity": "P2",
      "title": "\"Apply all for this file\" is absent",
      "evidence": "grep -i 'applyall|apply_all|apply all' over shared/, server/, desktopApp/, androidApp/ = 0 hits; no resource string; only per-nit Apply (NitpickReview.kt:96-102)",
      "impact": "demo",
      "fix": "Add an Apply-all button per file group calling applySuggestion for each non-file-level nit with a suggestion, sequentially under writeLock",
      "cost": "1h",
      "confidence": "high"
    },
    {
      "severity": "P2",
      "title": "No per-severity count and no score; \"N nits · M bug\" only comes from the unwired LocalReviewAgent",
      "evidence": "grep score in shared/src/commonMain = 0; ReviewPanel.kt:126 shows total nits + changed files only; LocalReviewAgent.kt:35 unused because AppContainer.kt:19 wires LlmReviewAgent(KoogLlmClient)",
      "impact": "pitch",
      "fix": "Compute counts by Severity in the ReviewPanel header from the anchored comments (pure function + commonTest)",
      "cost": "15m",
      "confidence": "high"
    },
    {
      "severity": "P2",
      "title": "No before/after block: originalSnippet is never displayed",
      "evidence": "NitpickReview.kt:78-104 shows only review_suggested_change (strings.xml:208) + suggestion; originalSnippet used only by ReviewAnchoring.kt:18,29,39",
      "impact": "demo",
      "fix": "Render the original snippet (dimmed, minus gutter) above the suggestion in ReviewCommentCard",
      "cost": "1h",
      "confidence": "high"
    },
    {
      "severity": "P2",
      "title": "Share button is not greyed out without a server",
      "evidence": "ShareReview.kt:48 enabled = !state.isSharing; failure only after click (RepositoryViewModel.kt:463-476, strings.xml:182) while CONTEXT §7.1 promises 'le partage est grisé'",
      "impact": "demo",
      "fix": "Disable the button when no server URL is configured, or on the last Unreachable failure, with the existing message as the reason",
      "cost": "15m",
      "confidence": "high"
    },
    {
      "severity": "P2",
      "title": "Unused contract surface and 3 members beyond the frozen §6 contract",
      "evidence": "GitRepository.status() never called outside JGitRepository; SharedReview.baseRef/headCommit written (RepositoryViewModel.kt:394-396) never read; ReviewShareClient.get(id) implemented (KtorReviewShareClient.kt:54) never called; ReviewComment.category never displayed; trackingStatus (default = null, GitRepository.kt:30) and publishBranch (default throws, :33) are additions",
      "impact": "submission",
      "fix": "Prune or document: either drop status()/get() or add the consumer; record the three contract additions in docs/QUESTIONS.md",
      "cost": "1h",
      "confidence": "high"
    },
    {
      "severity": "P2",
      "title": "README/CONTEXT overstate the product; DEMO.md quotes a stale shared-code figure",
      "evidence": "README.md:31 says 'inline nits'; CONTEXT §7 step 6 / §13 step 3 claim inline; apply-all and before/after are absent; DEMO.md quotes 98.2% / 4,466 of 4,546 while ./scripts/count-lines.sh at 54c37a2 gives 7,960 / 8,044 = 99.0%, 84 platform lines.",
      "impact": "submission",
      "fix": "Update DEMO.md numbers; replace 'inline' in README/CONTEXT/demo with the real behaviour.",
      "cost": "15m",
      "confidence": "high"
    },
    {
      "severity": "P2",
      "title": "OAuth device flow shipped although §8 lists it as WON'T",
      "evidence": "settings/ForgeClient.kt + ui/phone/ConnectScreen.kt implement a GitHub/GitLab device flow (#64); CONTEXT §8 WON'T says a personal access token is enough.",
      "impact": "submission",
      "fix": "Document it as a deliberate scope addition or remove it from the pitch; do not present it as a requirement.",
      "cost": "15m",
      "confidence": "high"
    }
  ],
  "coverage_gaps": [
    "Tests not executed (read-only, 10 min): 191 @Test counted, 0 run; PASS-on-tests verdicts are name/code-based",
    "docs/demo/*.png listed but never opened; screenshot content unverified",
    "docs/design/UI-HANDOFF.md §12 mapping not read",
    "docs/LAUZHACK.md only grepped for constraints (3-min demo, no slides, public repo+README, <12:00); README/submission-form compliance not checked",
    "No build, app launch, emulator or device run performed",
    "Only origin/main @ 54c37a2 audited; audit/106-cleanup (39 commits ahead) may fix some findings"
  ]
}
```
