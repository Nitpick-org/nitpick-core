# Proof: sharing a desktop → Android review (#170)

22/11 02:05, `main` at `3b14570`, Ktor server on `localhost:8080`, emulator `nitpick-qa` (API 36) with
`adb reverse tcp:8080 tcp:8080`. The reviewed repository is the test repository `remote.git` served at
`git://10.0.2.2/remote.git`; desktop and phone cloned it from the same URL.

| Step | Result | Screenshot |
| --- | --- | --- |
| Desktop: Nitpick branch on `feature/search-pages-import` vs `main` | 2 nits (`lastPage` division by zero, `toInt()` with no line number) | `share-desktop-shared.png` |
| Desktop: **Share review** | Badge "Shared"; `GET /reviews?repoUrl=git://10.0.2.2/remote.git` returns review `05bc388` with 2 nits | same |
| Android: Shared reviews → Refresh | Line "feature/search-pages-import · 05bc388 · 2 nits" | |
| Android: open the review | Review screen with the **Shared review** badge, 2 files, no call to the model (see below) | `share-android-opened.png` |
| Android: nit `src/Search.kt:17` → Apply suggestion | "Applied", "1 suggestion applied", Stage & commit button; `require(size > 0)` present in the file on the device | `share-android-applied.png` |

Total walkthrough time: under a minute once the server is running. The phone list also showed
an `ae8778a` entry: it is a JSON sent by hand with `curl` during the 21/11 QA, not a
desktop share.

"No call to the model" comes from the code, not from the screenshot: `RepositoryViewModel.openSharedReview`
re-anchors the received comments (`reanchor`) and never calls the review agent.
