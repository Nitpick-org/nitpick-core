# Nitpick — Project context: LauzHack 2026, JetBrains challenge (Kotlin Multiplatform)

This document is the project's master context. Every agent (orchestrator or subagent) reads
it before producing anything. When in doubt about a scope decision, this document
is authoritative. If a request contradicts it, the agent flags it instead of deciding alone.

## 1. Mission

Build Nitpick in 24 hours (from Saturday 21.11.2026 at 12h00 to Sunday 22.11.2026 at 12h00).
It is a cross-platform visual git client (in the spirit of Fork or SourceGit)
with an integrated AI code review on the current branch: comments anchored to
lines, like a pull request review, and suggestions applicable in one click.

**The problem**: with coding agents, nobody really reads what leaves for
main anymore. Existing AI review tools (CodeRabbit, Copilot review, Graphite…)
step in after the push, on the PR, server-side.

**Our difference**: a review run locally, before the push, with the repo's context, and
wired to the LLM chosen by the user (Claude, OpenAI, Gemini, or Ollama locally).

### Identity

- **Name**: Nitpick. In review jargon, a "nit" is a small comment on
  a specific line. The name states the flagship feature: line-by-line review.
- **Tagline**: "Nitpick your branch before main does." (FR: "Faites pinailler votre
  branche avant qu'elle n'arrive sur main.")
- **Main action**: the "Nitpick this branch" button (FR: "Nitpicker cette
  branche"). A review comment is called a nit in the UI ("7 nits · 1 bug").
- **Technical identifiers**: Gradle name `nitpick`, package `dev.nitpick.app`,
  Android applicationId `dev.nitpick.app`, desktop window titled "Nitpick".
- **Visual tone**: a sober, precise dev tool, aligned with the **JetBrains IDE /
  New UI** design language (`docs/DESIGN-SYSTEM.md`). Single **teal** accent (light/dark),
  graphite chrome, 28 dp controls, code in JetBrains Mono. Simple app icon (magnifying
  glass or stylized tweezers, nothing generic).

## 2. What the judges evaluate (and therefore what we optimize)

- **Implementation / proof of concept**: the app actually runs on at least 2
  platforms, and the core feature works.
- **UX / UI**: a polished, native-looking result on each platform, not merely functional.
- **Technical**: the share of logic genuinely shared in commonMain versus code
  duplicated per platform. Bonus points for: Compose Multiplatform, localization, shared
  AI/agent logic (Koog), Kotlin backend.
- **Innovation / creativity**: the originality of the idea.
- **Agentic process (bonus)**: build with **JetBrains Air** in the IDE. Air
  orchestrates agents side by side (Junie, Codex, Gemini, GitHub Copilot, Claude, others via
  ACP); humans review, correct and steer instead of accepting everything. Air is a
  **development tool**, not an AI provider nor a Nitpick runtime dependency.
  Extra bonus for multi-agent work (different agents on shared code and on
  platform code, or comparison of their results).
- **Presentation**: a clear demo that explains what is shared and what remains
  specific to each platform.

Explicitly listed bonus features: dark/light mode, localization, Compose
Multiplatform, Kotlin backend, shared AI via Koog. We target **all five**, and they
are mandatory for us: dark/light, localization, Compose MP, Koog, and a Kotlin
backend (Ktor) to share reviews (§7.1). Criterion 5 (Air, multi-agent) is
treated as mandatory as well.

> The challenge recommends Air to **build** the project. The **product's** AI engine
> remains Koog: Air/Junie are not called by the app. The official brief is in
> [`docs/JETBRAINS-CHALLENGE.md`](JETBRAINS-CHALLENGE.md).

## 3. Team and human constraints

3 people, all Kotlin beginners. They are developers otherwise (fullstack,
TypeScript/React Native), so the general concepts are established, but Kotlin syntax,
Gradle, KMP and Compose are new.

Consequences for the agents:

- Briefly explain each non-trivial choice (in a PR or commit comment, not in
  verbose code comments), so the human can review and understand.
- Prefer simple, readable Kotlin over "clever" Kotlin. No home-grown DSL, no
  gratuitous generics, no metaprogramming.
- Never deliver code that does not compile. Before handing back, run the
  desktop and Android build.
- Humans review everything and reject what is wrong. This is a judging criterion
  (see §11).
- Each human installs the **Air** plugin in their IDE and connects an agent (Junie Lite on
  the free tier, or Codex/Gemini/Copilot/Claude). Each track works in Air, with a
  human reviewing the diffs.
- Process evidence (agents launched, human corrections) goes into `AGENT_LOG.md` and
  `REVIEW_LOG.md`; keep 2–3 Air screenshots for the demo.

## 4. Target platforms

| Target | Role | Priority |
| --- | --- | --- |
| Desktop JVM (macOS, Windows, Linux) | The main app: full git client + AI review | P0 |
| Android | "Review on the go" mode: clone a repo over HTTPS with a token, see the graph, launch the AI review or open a shared review, accept suggestions, commit and push | P0 |
| Kotlin server (Ktor, JVM) | Sharing reviews between devices and teammates (§7.1). Not a UI target: a `server/` module | P0, after the review loop |
| iOS / Web | Out of scope. No JGit on those targets, the whole git backend would have to be rewritten. | ✗ |

macOS, Windows and Linux form a single KMP target. Android is the second mandatory
platform and must not be an empty shell.

## 5. Technical stack

Always take the latest stable version of each lib. Do not invent APIs: check
the official docs (links §15) before writing Koog or Compose code. If an API has
changed, adapt to it and flag it.

- **Language**: Kotlin (latest stable), Kotlin Multiplatform.
- **Project**: generated from the kmp.jetbrains.com wizard (Android + Desktop targets,
  UI shared in Compose).
- **UI**: Compose Multiplatform + Material 3, all UI in commonMain, tokens and
  components in `docs/DESIGN-SYSTEM.md` (JetBrains New UI design language, teal accent, IDE shell).
- **Theme**: light and dark, following the system by default, with a manual toggle in
  settings.
- **Localization**: Compose Multiplatform Resources
  (`composeResources/values/strings.xml` and `values-fr/strings.xml`). English by default,
  complete French. No hard-coded UI string.
- **State**: multiplatform ViewModel (JetBrains' lifecycle-viewmodel-compose) +
  StateFlow. Simple MVI/UDF-style architecture: immutable State, Intent/actions,
  ViewModel.
- **Navigation**: a simple screen state in a root ViewModel is enough. Add a navigation
  lib only if really necessary.
- **DI**: manual injection (an `AppContainer` object). No Koin or Hilt, to
  reduce the learning surface.
- **Concurrency**: Kotlin Coroutines. Git and LLM operations always run off the
  UI thread (`Dispatchers.IO` on the JVM side).
- **Serialization**: kotlinx.serialization.
- **Git**: JGit (Eclipse), used behind an interface defined in commonMain.
- **Product AI**: Koog (JetBrains' Kotlin agent framework), in commonMain. Koog
  supports JVM, Android, iOS, JS and Wasm, as well as several providers (Anthropic,
  OpenAI, Google, Ollama…). It is the piece that demonstrates the "shared AI logic" requested by
  the judges.
- **Backend**: Ktor server (JVM) in a Gradle `server/` module, which reuses the
  `@Serializable` models from `domain`. On the app side, a Ktor client in commonMain (CIO
  engine in `jvmSharedMain`, shared by desktop + Android).
- **Development**: JetBrains Air + agents (Junie, Codex, Gemini, Copilot, Claude) to
  produce the code. These are IDE tools, not app dependencies.
- **Local storage** (settings, API key, recent repos): a minimal expect/actual (preferences
  file on desktop, SharedPreferences/DataStore on Android). Never commit an API
  key.

## 6. Architecture and code distribution

Goal: as much code as possible in commonMain. Platform code must be thin and
boring.

```text
composeApp/src/
├── commonMain/            ← EVERYTHING that can be
│   ├── domain/            pure models: Commit, Ref, Branch, FileDiff, Hunk, DiffLine,
│   │                      ReviewComment, Suggestion, ReviewResult
│   ├── git/               GitRepository interface (contract), GitRepositoryFactory (expect)
│   ├── graph/             commit graph lane algorithm (100% pure Kotlin, tested)
│   ├── diff/              unified diff parsing → Hunk/DiffLine model with line numbers
│   ├── review/            Koog agent, repo context building, output parsing/validation,
│   │                      applying suggestions (text patch)
│   ├── settings/          settings model + SettingsStore interface (expect)
│   ├── share/             backend client: ReviewShareClient (Ktor client), §7.1
│   ├── ui/                all Compose screens and components, theme, ViewModels
│   └── composeResources/  EN/FR strings, icons
├── jvmShared/             ← intermediate source set shared by desktop + Android
│   └── git/               JGitRepository: GitRepository implementation backed by JGit
├── desktopMain/           main(), window, native folder picker, desktop SettingsStore
└── androidMain/           MainActivity, Android SettingsStore, clone storage location

server/                    ← separate JVM module: Ktor server, depends on shared for domain
```

The server imports neither Compose nor JGit: it only sees the `domain` models.

`jvmShared` is an intermediate source set that desktopMain and androidMain depend on (via
`applyDefaultHierarchyTemplate { common { group("jvmShared") { withJvm(); withAndroidTarget() } } }`
or an explicit `dependsOn`, depending on what the Kotlin version accepts). Thus,
the JGit implementation is written only once.

**Golden rule**: no `org.eclipse.jgit` import outside jvmShared. No Android or
AWT import outside its source set.

Plan for a simple script or Gradle task at the end that counts lines per source set,
for the "shared vs platform-specific" slide.

### Contracts to freeze FIRST (hour 1)

These interfaces and models are written and validated by a human before parallel
work starts, so the tracks do not block each other:

```kotlin
interface GitRepository {
    suspend fun log(limit: Int = 500): List<Commit>
    suspend fun refs(): List<Ref>
    suspend fun currentBranch(): String
    suspend fun status(): WorkingTreeStatus
    suspend fun diffAgainst(baseRef: String): List<FileDiff>   // current branch vs base
    suspend fun readFile(path: String): String?
    suspend fun listFiles(): List<String>
    suspend fun writeFile(path: String, content: String)
    suspend fun stage(paths: List<String>)
    suspend fun commit(message: String)
    suspend fun fetch(credentials: Credentials?)
    suspend fun pull(credentials: Credentials?): PullResult
    suspend fun push(credentials: Credentials?)
    suspend fun checkout(branch: String)
}
```

Review model (to refine, but the shape must stay like this):

```kotlin
@Serializable data class ReviewResult(val summary: String, val comments: List<ReviewComment>)
@Serializable data class ReviewComment(
    val id: String,
    val filePath: String,
    val startLine: Int,            // line number in the NEW version of the file
    val endLine: Int,
    val severity: Severity,        // INFO, SUGGESTION, WARNING, BUG
    val category: String,          // e.g. "security", "perf", "readability", "logic"
    val message: String,
    val originalSnippet: String,   // the targeted lines, copied back: used to check anchoring
    val suggestion: String? = null // replacement code for startLine..endLine, or null
)
```

Backend contract (§7.1), to freeze before writing the server:

```kotlin
@Serializable data class SharedReview(
    val id: String,                // assigned by the server
    val repoUrl: String,           // origin remote URL: identifies the repo on every device
    val branch: String,
    val baseRef: String,
    val headCommit: String,
    val result: ReviewResult,
)

interface ReviewShareClient {
    suspend fun share(review: SharedReview): String          // returns the id
    suspend fun list(repoUrl: String): List<SharedReview>
    suspend fun get(id: String): SharedReview?
}
```

## 7. The AI review (heart of the project)

### Flow

1. The user is on a branch and picks a base (main by default).
2. We compute `diffAgainst(base)`.
3. We build the repo context: file tree (truncated), full content of the
   modified files, README, last 10 commit messages, detected language or framework.
4. The Koog agent produces a `ReviewResult` as structured output (Koog does this
   natively, with retries and an output correction when the model does not support
   native JSON).
   > The review engine is Koog. Air/Junie are used to build and review the code during
   > the hackathon; they are not part of the Nitpick runtime.
5. **Anchor validation**: for each comment, we check that `originalSnippet`
   matches the `startLine..endLine` lines of the file. Otherwise, we search for the
   snippet in the file to realign the line numbers. Otherwise, the comment is attached to
   the whole file. A badly anchored comment must never crash the UI.
6. The UI shows the diff with inline comments (GitHub PR style), sorted by severity.
7. **Apply a suggestion**: we re-check that the lines have not moved, replace them,
   write the file, refresh the diff, then offer to stage and commit. We also need
   "Ignore" and "Apply all for this file".

### Reliable anchoring

- In the prompt sent to the LLM, prefix each line of the diff with its number in the new
  file (`  42 | code`), and require the model to quote these numbers and copy
  `originalSnippet`.
- Limit the size: ignore lockfiles, binaries and generated files, and keep
  only the N most important modified files if the diff is huge.

### Agentic (bonus, if time permits)

- Give the Koog agent `readFile(path)` and `listFiles()` tools so it can fetch
  context itself (for example the definition of a function called in the
  diff). This is what justifies the phrase "with the repo's context".
- Always keep a tool-less mode (a single call) as a fallback, faster and more
  predictable for the demo.

### Providers

- Settings: choice of provider (Anthropic / OpenAI / Google / Ollama), of model and of
  API key.
- All this code is in commonMain. Only the key storage is an expect/actual.

## 7.1 Sharing reviews (Kotlin backend)

The review stays **local**: the LLM is called from the app, with the user's key.
The backend only receives what we choose to share, that is, an already produced `ReviewResult`.

- **"Share review" button** after a review: the app sends a `SharedReview` to the server.
- **"Shared reviews" screen**: the list of shared reviews for the open repo
  (filtered by `repoUrl`). Opening one shows the nits inline like a local review.
  Anchoring by `originalSnippet` (§7) applies as is, so a review made on
  another commit breaks nothing: the nits realign or fall back to file level.
- **API**: `POST /reviews`, `GET /reviews?repoUrl=…`, `GET /reviews/{id}`, in JSON
  (kotlinx.serialization, ContentNegotiation).
- **Storage**: in memory, plus a JSON file to survive a restart. No
  database, no accounts.
- **Server URL** in settings, `http://localhost:8080` by default. On Android:
  `adb reverse tcp:8080 tcp:8080` over USB, or the laptop's LAN IP.
- **Without a server, the app still works**: sharing is greyed out, with a clear error message.

For the demo, this is also the fallback for LLM latency: Android opens the review shared
by the desktop, without a new LLM call.

## 8. Scope

### MUST (without it, no demo)

- Open a local repo (desktop) or clone a repo over HTTPS with a token (Android and
  desktop).
- Commit graph with lanes and colors, branches and tags displayed, selection of a commit
  with its details.
- Diff of the current branch against the base, with add/delete coloring.
- **AI review → inline comments → apply a suggestion → commit.** This is the loop
  that wins. It must work first, even on an ugly UI.
- Light/dark theme and EN/FR localization.
- It runs on desktop and Android.
- Ktor backend: share a review from the desktop, open it on Android (§7.1). We only
  start it **after the 21:00 gate**: the review loop comes first.

### SHOULD

- Fetch, pull, push and branch checkout.
- `readFile`/`listFiles` tools for the agent.
- Global review summary and score or count per severity.
- Streaming or progress indicator during the review.
- Adaptive layout: side-by-side panes on desktop, screen navigation on Android.

### WON'T (do not start, even if "it's quick")

- Interactive conflict resolution (roadmap only in the pitch).
- Interactive rebase, stash UI, submodules, LFS, SSH.
- iOS, web.
- Anything beyond backend review sharing: accounts, authentication,
  database, cloud hosting, review executed server-side.
- GitHub OAuth authentication (a personal token is enough).
- Runtime integration of Air/Junie (calling Air/Junie from the app, making it a review
  provider or launching the CLI inside the product): out of scope for the hackathon. Air/Junie are
  the **development** tools; the product keeps Koog.

## 9. Parallel work tracks (multi-agent)

Each human drives a track and its agent(s). Tracks only touch each other via the
§6 contracts.

| Track | Scope | Source sets |
| --- | --- | --- |
| A: Shared core | Models, commit graph lane algorithm (+ tests), diff parsing, Koog review agent, anchor validation, suggestion application | commonMain (domain, graph, diff, review) |
| B: Git + desktop | JGitRepository, Gradle configuration of source sets, desktop window, folder picker, main screens in desktop layout | jvmShared, desktopMain, commonMain/ui |
| C: Android + finishing touches | Android target (HTTPS clone, storage), mobile layout, light/dark theme, EN/FR strings, settings, shared line-count script, demo preparation | androidMain, commonMain/ui, composeResources |
| D: Backend | `server/` module (Ktor), `ReviewShareClient`, Share button, Shared reviews screen. Starts after the 21:00 gate, when the desktop loop writer frees up: we stay at 3 code writers in parallel | server/, commonMain/share, jvmSharedMain (client engine) |

To match the multi-agent criterion: on at least one task (ideally the lane
algorithm or the review prompt), have two different agents produce work on the same problem,
compare their results, keep the best, and document the comparison.

**Process**: each track is driven in **Air** (Junie/Codex/Gemini/Copilot/Claude agent),
with a human reviewing each diff. Air/Junie are not a product dependency: Nitpick's
code does not import them. The fallback is Claude Code/Codex CLI, documented in
`AGENT_LOG.md`.

### 9.1 Issue ownership (maintained by H2)

Working mapping (to fix if a handle does not match):

| Role | GitHub | Open issues | Air |
| --- | --- | --- | --- |
| **H1 — Product & Pitch** | `@dovatgabriel` | **#19 desktop diff (now, before 21:00)**, #39 demo/submission/Air evidence; after 21:00: track D #46 server, #48 sharing, #44 demo repo; then #33 states, #40 line count, #45 desktop clone | A Junie + second agent comparison, screenshots, `AGENT_LOG`/`REVIEW_LOG` |
| **H2 — Architect & Integration** | `@zooldyyk` | **#20 Koog agent, then #22 desktop review loop (21:00 gate)**; then #36 sync toolbar, #37 filters, #38 icons, #43 Koog tools | One Air session on the prompt/diff, comparison, human review |
| **H3 — Builder & Demo** | `@pepito2t` | **#21 Android clone, then #23 Android review loop (02:00 gate)**; then #34/#35 desktop/Android settings, #42 pull/checkout | One Air session on the UI/Android, screenshot, human review |

Split by time window, not by track: #19 goes to H1 so that H2 has only
#20 and #22 before 21:00 (Q-018); #42 goes with H3's push; #40 serves H1's demo.

Rules:

- One issue = one owner = one worktree; tickets marked `blocked` (#36–#38, #42, #43,
  #45, #46, #48) wait for their owner's gate: 21:00 for H2 and track D, 02:00
  for H3.
- Each owner adds at least one `Agent via Air` entry in `AGENT_LOG.md` and one
  human correction/rejection in `REVIEW_LOG.md`.
- Only one multi-agent comparison is needed for the bonus, but it must be
  **real** (same task, two agents, kept result, reason for the choice).


## 10. Planning (checkpoints are blocking control points)

| Time | Goal | Checkpoint |
| --- | --- | --- |
| 12h–13h | Project generated via the wizard, build OK on desktop and Android, §6 contracts written and validated, JGit spike on Android | ✅ "Hello" on the 2 targets, JGit reads a log on Android |
| 13h–17h | A: lanes + diff + 1st Koog call. B: JGitRepository + desktop graph screen. C: theme, strings, Android shell. **Air installed on the 3 laptops + first agent logged in (17:30, #39)** | ✅ Real graph displayed on desktop, Air screenshot |
| 17h–21h | Full review loop on desktop (diff → Koog → inline comments → apply) | ✅ End-to-end review loop that works (critical checkpoint) |
| 21h–02h | Android wired to the same core, HTTPS clone, fetch/pull/push, UI polish. Track D: Ktor server + desktop → Android sharing | ✅ Review loop on Android, shared review opened on Android |
| 02h–07h | Sleep in rotation (at least 3h each). At most one person keeps going on non-risky polish. | |
| 07h–09h30 | Bugfix, agentic tools if stable, UX finishing touches | ✅ Feature freeze at 09h30 |
| 09h30–11h | Demo repo prepared, demo script, full-screen images (shared vs specific, agentic process: no slides, LauzHack rules), backup video | |
| 11h–12h | Rehearsal and submission | ✅ Submitted before 12h |

**Rule**: if a checkpoint is missed by more than an hour, we cut scope (SHOULD →
WON'T). We do not push back the next checkpoint.

## 11. Working rules for the agents

- Read this document in full before any task. Respect the scope of §8.
- Small increments: one task = one commit or one targeted PR, with a clear English
  message (`feat(graph): ...`, `fix(review): ...`).
- It compiles on both targets before handing back (`./scripts/check.sh`: shared tests,
  `:desktopApp:assemble`, `:androidApp:assembleDebug`).
- No new dependency without justifying it in the PR.
- Code and comments in English, UI strings in the EN and FR resource files.
- Respect source set boundaries (§6). If a task seems to require platform code, say so
  and propose a minimal expect/actual.
- Do not hallucinate Koog, Compose or JGit APIs: check the docs. When in doubt,
  spell it out in the PR.
- No secrets in the repo (API keys, tokens). Use a local `.env` ignored by git or
  the app settings.
- **Agent log**: the agent writes to `progress/<ticket>.md` (agent, track, task,
  files touched, decisions, what remains uncertain). The human who launched the session (Air
  or CLI) reports one line in `docs/AGENT_LOG.md` at the end of the session (Q-017).
- **Humans review and reject**: each human rejection or correction is recorded in
  `docs/REVIEW_LOG.md` (what, why, what was changed). This is the evidence for criterion 5.
  Do not skip this step.
- **Dogfooding**: as soon as the AI review works, use it to review the branches
  produced by the agents, and keep 2 or 3 screenshots of the cases where it found a real
  problem. This is the presentation's punchline.
- **Air/Junie**: launch agentic tasks in Air, with a human reviewing each
  diff. Never import Air/Junie into the app code: they are development tools, not
  runtime dependencies.
- **If Air is unavailable**: fallback to Claude Code/Codex CLI, and note it in
  `AGENT_LOG.md`. The product does not depend on Air.

## 12. UX: requirements

- **Desktop**: 3-pane layout (sidebar with branches and refs | graph and commit
  list | details, diff and review). Keyboard shortcuts for the main actions
  (fetch, review, apply the suggestion).
- **Android**: screen navigation (Repos → Graph → Diff + Review), large action
  buttons, bottom sheet for a review comment.
- **Review comments**: colored severity badge, message, "before /
  after" diff block for the suggestion, Apply and Ignore buttons.
- **Sharing**: "Share review" button on a review result; "Shared
  reviews" screen (branch, commit, nit count), accessible on the 2 targets.
- Empty, loading and error states polished everywhere. These are the details that make
  the app look "native" and "polished" (criterion 2).
- Mono font for code, add/delete coloring readable in both themes.

## 13. Demo (the target everything converges toward)

1. **Hook (20 s)**: "Who here has merged AI-written code without reading it in full?
   Nitpick is the nitpicking colleague we no longer have time to be."
2. Open a repo on desktop: the graph, the branches.
3. Switch to a branch written by an agent during the hackathon and click
   "Nitpick this branch": the nits appear inline, including a real bug.
4. Apply the suggestion, then commit. Click "Share review".
5. On Android: same repo, and the shared review arrives through the Ktor backend (same nits,
   without calling the LLM again). Accept a suggestion from the phone, then push.
6. Shared vs specific, without slides (LauzHack rules, Q-007/Q-016): the output of the
   line-count-per-source-set script (#40) live, or a full-screen image with the
   §6 diagram. Message: the same `domain` models run on desktop, Android **and**
   the server.
7. Agentic process, without slides: 2–3 Air screenshots as full-screen images (agents side by
   side), the 3 tracks, the multi-agent comparison, a few REVIEW_LOG entries ("here is
   what we rejected from our agents"). The message: we used Air/Junie to build,
   and our own tool to review our agents.
8. Roadmap in one sentence: AI-assisted conflict resolution, iOS via the GitHub API.

Prepare a dedicated demo repo, with a branch containing deliberate but
credible defects (off-by-one bug, SQL injection, unclosed resource), plus a backup video
in case the Wi-Fi or the LLM gives out.

## 14. Known risks and fallbacks

| Risk | Fallback |
| --- | --- |
| Recent JGit incompatible with Android | Spike from hour 1. Try core library desugaring (java.nio) with minSdk 26, otherwise an older JGit version, otherwise an Android fork of JGit. As a last resort, Android reads the repo via the GitHub REST API, behind the same GitRepository interface (it remains a clean demonstration of the abstraction). |
| Koog API different from what we think | Official docs + examples from the Koog GitHub repo. Start with the simplest call (one prompt, one structured output), and add tools only afterwards. |
| The LLM returns wrong line numbers | Validation via `originalSnippet` (§7) and, as a last resort, attaching to the whole file. |
| LLM latency during the demo | Progress indicator, fast model by default, and a cached review result for the demo repo (toggleable in settings). |
| The phone cannot reach the server (Wi-Fi that isolates clients) | `adb reverse tcp:8080 tcp:8080` over USB, or the emulator (`10.0.2.2`). The app remains usable without a server. |
| Slow graph on a large repo | Limit to 500 commits, LazyColumn, and lane computation off the UI thread. |
| Gradle or KMP blocking beginners | Do not "optimize" the config: stay as close as possible to the wizard template. |
| Air/Junie unavailable (EAP, account, network) | Use Claude Code/Codex CLI; the product does not depend on Air; document the fallback in `AGENT_LOG.md`. |
| Air asks for project trust / permissions | Accept trust on the repo, check `.junie`/MCP, never expose `.env`; keep the agents inside the open repo. |

## 15. References

- JetBrains challenge brief: [`docs/JETBRAINS-CHALLENGE.md`](JETBRAINS-CHALLENGE.md)
- Review handoff: [`docs/HANDOFF.md`](HANDOFF.md)
- KMP templates / wizard: https://kmp.jetbrains.com
- KMP docs: https://kotlinlang.org/docs/multiplatform/multiplatform-create-first-app.html
- Compose Multiplatform: https://www.jetbrains.com/compose-multiplatform/
- Koog: https://docs.koog.ai and https://github.com/JetBrains/koog
- Ktor (server and client): https://ktor.io/docs/
- Air in JetBrains IDEs (EAP): https://blog.jetbrains.com/ai/2026/10/air-in-ides-eap/
- Air docs: https://www.jetbrains.com/help/air/
- Junie / Junie Lite: https://junie.jetbrains.com
- JGit: https://github.com/eclipse-jgit/jgit (cookbook: https://github.com/centic9/jgit-cookbook)
- MVI + Compose MP video: https://youtu.be/dveR4xWid4Q
