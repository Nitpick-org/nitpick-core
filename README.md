# Nitpick

**Nitpick your branch before main does.**

Nitpick is a visual git client for desktop and Android, built with Kotlin Multiplatform,
that reviews your current branch with the LLM of your choice **locally, before you push**:
comments anchored on lines like a pull-request review (*nits*), and suggestions you apply
in one click.

Built in 24 hours at [LauzHack 2026](https://lauzhack.com/) (EPFL, 21–22 November 2026)
for the JetBrains Kotlin Multiplatform challenge.

## Why

Coding agents write more of what lands on `main`, and fewer people read it end to end.
AI review tools such as CodeRabbit, Copilot review or Graphite run after the push, on the
pull request, on a server. Nitpick runs **before** the push, on your machine, with the
repository's context, and talks to the provider you pick: Anthropic, OpenAI, Google, or
Ollama running locally.

## Status

> Updated at each team sync. Only what runs today is listed as done.

| Piece | State |
| --- | --- |
| KMP project (desktop + Android, shared Compose UI) | ✅ generated, builds on both targets |
| JGit behind a shared source set (`jvmSharedMain`) | ✅ log, diff, stage, commit, fetch/pull/push and checkout run on desktop and Android (emulator, API 36) |
| Shared contracts (`GitRepository`, review model) | ✅ in `commonMain`, JGit behind the interface |
| Commit graph, branch diff | ✅ lanes, colours, refs, per-commit details, diff vs base |
| AI review with Koog, anchored nits, apply a suggestion, commit | ✅ real provider calls, snippet anchoring, Review panel, Apply/Dismiss, commit |
| Light/dark theme, English/French | ✅ system/light/dark, 179 strings + 7 plurals EN/FR |

Full scope (MUST / SHOULD / WON'T) and the hour-by-hour plan: [docs/CONTEXT.md](docs/CONTEXT.md).

## Platforms

| Target | Role |
| --- | --- |
| Desktop (macOS, Windows, Linux) | The main app: full git client + AI review |
| Android | Review on the go: clone over HTTPS with a token, see the graph, run the review, accept suggestions, commit and push |
| Kotlin server (Ktor) | Shares a finished review between devices and teammates; the review itself still runs locally |
| iOS, web | Out of scope: JGit does not run there |

## Architecture

As much as possible lives in `commonMain`; platform code stays thin.

```text
shared/                       Kotlin Multiplatform module (targets: jvm, android)
  src/commonMain/             domain, git contract, commit graph, diff parsing,
                              Koog review agent, settings, Compose UI, EN/FR strings
  src/jvmSharedMain/          JGit + FileSettingsStore, shared by desktop and Android
desktopApp/                   main(), the folder picker and the "Nitpick" window
androidApp/                   MainActivity, app-private clone folder; androidTest/ runs on a device
server/                       Ktor server that shares reviews, reusing the domain models
```

- **No `org.eclipse.jgit` import outside `jvmSharedMain`.** The rest of the app only sees the
  `GitRepository` interface in `commonMain`.
- **Shared AI logic**: the review agent (Koog), the repo context, the output validation and
  the code that applies a suggestion all live in `commonMain`. Only API-key storage is
  per platform.
- **Reliable anchoring**: the LLM sees every diff line prefixed with its new line number and
  must copy back the snippet it comments on. Nitpick checks that snippet against the file,
  re-anchors it if the lines moved, and falls back to a file-level comment rather than
  crash.

## Tech stack

| Piece | Choice | Version |
| --- | --- | --- |
| Language | Kotlin Multiplatform | 2.4.21 |
| UI | Compose Multiplatform + Material 3 | 1.12.2 |
| Android build | AGP, minSdk 33, targetSdk 37 | 9.1.1 |
| Git | JGit | 6.10.1 |
| AI agents | Koog | 1.3.0 |
| Backend | Ktor server + client | 3.3.3 |

JGit is pinned to 6.10: JGit 7 ships Java records, which the Android dexer refuses below
API 34 (details in commit `df36830`).

## Run it

Prerequisites: JDK 17+, the Android SDK with `ANDROID_HOME` set, and an emulator or a
phone for Android. Gradle downloads everything else, including the JDK 21 its daemon runs on.

```bash
git clone https://github.com/Nitpick-org/nitpick-core.git nitpick && cd nitpick
git config core.hooksPath .githooks      # pre-commit runs scripts/check.sh

./gradlew :desktopApp:run                # desktop app
./gradlew :androidApp:installDebug       # Android app on the connected device

./scripts/check.sh                       # shared tests + desktop and Android builds
./gradlew :androidApp:connectedDebugAndroidTest   # tests on Android (device needed)
./scripts/doctor.sh                      # checks the toolchain and the repo setup
```

API keys are entered in the app's settings and never committed.

## How we build it: 3 humans, many agents

The JetBrains challenge rewards an authentic development process, so ours is in the repo.

- **Three tracks** work in parallel and only meet through the shared contracts: A (shared
  core: graph, diff, review agent), B (JGit + desktop), C (Android + finishing touches).
- **Agents do real work, humans review it.** Each ticket gets its own git worktree and one
  writing agent. A read-only reviewer agent goes over every diff before a human merges it.
- **Everything is logged**: each agent keeps a journal in `progress/<ticket>.md`, compiled
  into [docs/AGENT_LOG.md](docs/AGENT_LOG.md). Every time a human rejects or corrects an
  agent's work, it goes into [docs/REVIEW_LOG.md](docs/REVIEW_LOG.md).
- **Multi-agent comparison**: at least one task is solved by two different agents and the
  comparison is documented.
- **Dogfooding**: once the review works, Nitpick reviews the branches our agents produce.

The rules every agent follows are in [AGENTS.md](AGENTS.md) (Claude Code adds
[CLAUDE.md](CLAUDE.md)); hooks enforce them (`scripts/`, `.githooks/`).

## Repository map

| Path | What it is |
| --- | --- |
| `shared/`, `desktopApp/`, `androidApp/` | The app |
| [docs/README.md](docs/README.md) | Docs index: current docs first, pre-event history last |
| [docs/CONTEXT.md](docs/CONTEXT.md) | Master project brief: mission, stack, scope, plan, demo (French) |
| [docs/JETBRAINS-CHALLENGE.md](docs/JETBRAINS-CHALLENGE.md) | Official JetBrains KMP challenge brief and how Nitpick maps to it |
| [docs/HANDOFF.md](docs/HANDOFF.md) | Current handoff: changes, ownership, open issues, review checklist |
| [docs/DESIGN-SYSTEM.md](docs/DESIGN-SYSTEM.md) | JetBrains New UI design system: tokens, components, rules (French) |
| [AGENTS.md](AGENTS.md) | Team rules for every agent, plus an index of all team docs (French) |
| `docs/` | Team docs: event facts, demo script, decisions, questions, research, agent ops |
| `progress/` | One journal per ticket, written by the agent working on it |
| `scripts/` | `check.sh`, `doctor.sh`, worktree and AFK helpers, hook scripts |
| `.claude/` | Claude Code subagents, the `/route-task` skill, settings and hooks |
| `dry-run/` | Pre-event rehearsal notes; the TypeScript starter they referred to was removed after the freeze |

Team docs are written in French; code, comments and commit messages are in English.

## Created during the event vs reused

LauzHack asks every team to state this explicitly.

- **Reused**: the KMP + Compose Multiplatform wizard template, JGit 6.10, Koog 1.3,
  Ktor 3.3, JetBrains Mono, and the team's pre-event docs and agent tooling.
- **Created during the event**: the domain, graph, diff, review/anchoring, JGit backend,
  Compose UI for desktop and Android, the Koog review agent, the Ktor sharing server and
  their tests.
- **Not mocked**: the review path calls a real provider. The demo repository and the
  backup video are still being prepared; see `docs/DEMO.md`.

## License

Nitpick is released under the MIT License: see [LICENSE](LICENSE). The third-party
components it depends on or bundles, and their licenses, are listed in
[THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
