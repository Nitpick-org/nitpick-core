# Ticket 48-share-review

- Status: ready for PR (demo to replay by hand with the server running)
- Human owner: @dovatgabriel
- Track: D (review sharing)
- Agent: Claude Code (lead)
- Model: opus
- Escalations: none
- Files: `commonMain/share/*` (new), `git/GitRepository.kt` + `JGitRepository.kt`
  (`remoteUrl`), `settings/Settings.kt`, `ui/repository/{RepositoryViewModel,ReviewPanel,
  RepositoryScreen,ShareReview}.kt`, `ui/phone/{PhoneFlow,PhoneGraphScreen,PhoneReviewScreen,
  PhoneSharedReviewsScreen}.kt`, `ui/settings/*`, `ui/App.kt`, `AppContainer.kt`, EN/FR strings,
  `androidApp` (manifest + test on ART), Gradle.

## Log

- 19:38 — worktree created.
- 19:45 — `ReviewShareClient` (§6) + `KtorReviewShareClient` (commonMain, `HttpClient()` finds
  the CIO of jvmSharedMain), 5 MockEngine tests.
- 19:55 — `GitRepository.remoteUrl()`, `Settings.serverUrl`; ViewModel: `shareReview`,
  `loadSharedReviews`, `openSharedReview`; 4 tests.
- 20:10 — UI: "Share review" button on the result (desktop + phone), "Shared
  reviews" list in the empty state of the Review tab (desktop) and "Shared" screen from the Graph
  (phone), "Review server URL" field in both settings.
- 20:20 — real engine tests: closed port → `ReviewShareException`, on JVM and on ART.

## Decisions

- Opening a shared review fills `state.review`: same nits, same apply, same anchoring by
  `originalSnippet` as a local review, and no call to the LLM.
- The token of a remote URL (`https://token@host/…`) is stripped before sending; the server
  normalizes SSH/HTTPS (#46).
- A single client-side error, `ReviewShareException`; the ViewModel types it as
  `ShareFailure.NoRemote` / `Unreachable`, the UI shows a message that says how to start the
  server. The rest of the app does not change without a server.
- Short timeouts (connect 3 s, request 10 s): a wrong LAN IP fails fast.
- `usesCleartextTraffic` on Android: the demo server speaks HTTP on the LAN.
- `ReviewShareClient` in `AppContainer`, the URL read from the settings on every call.

## Ponytail review (fixed)

- Opening a shared review **re-anchors** each nit on the device's files
  (`ReviewAnchoring.anchorOrFileLevel`): a moved snippet changes line, a missing snippet
  falls back to the whole file. Before, the desktop's lines were copied as-is.
- It **checks out the review's branch first** if the device is on another one (the phone
  clone is on `main`); a failed checkout shows its error and re-anchors on the current
  files. The phone shows the spinner during that checkout.
- **The commit keeps the unapplied nits**, re-anchored, instead of erasing the review: the
  §13 script ("apply, commit, then Share review", then "accept a suggestion from the
  phone") plays as-is. Behaviour change of #22, intended.
- `loadSharedReviews` cancels the previous request: a slow response no longer overwrites a
  more recent list.
- Tests: re-anchoring (moved line + missing snippet), checkout of the shared branch, remaining
  nits after commit then re-share. `ShareReviewViewModelTest` 7/7.

## Deviations from the ticket

- **No date** in the list: `SharedReview` has no date field; the order (most recent
  first) is the server's. Adding `sharedAt` = contract change + server.
- **"Greyed out without a server"**: the button stays active (to retry once the server is
  running) and a red message explains what to do; it is greyed out while sending.

## Checks

- `:shared:jvmTest`: `KtorReviewShareClientTest` 5/5, `KtorReviewShareClientEngineTest` 1/1,
  `ShareReviewViewModelTest` 4/4, `RepositoryViewModelTest` 19/19.
- `ReviewShareOnDeviceTest` on the API 36 emulator: 1/1.
- `./scripts/check.sh` green.

## Uncertain

- Not replayed by hand: Share on desktop → list on the phone with the server running.

## Next

- Replay point 5 of the demo: `./gradlew :server:run`, `adb reverse tcp:8080 tcp:8080`,
  Share on desktop, "Shared" on the phone.
