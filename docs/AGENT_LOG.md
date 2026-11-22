# Agent log

> Agents do **not** write here: they write in `progress/<ticket>.md`, one file per
> ticket, so that two worktrees never fight over the same file at merge.
> A human compiles the useful entries here at each sync (proof of the agentic process, §11 of
> `docs/CONTEXT.md`). A session started in **JetBrains Air** is noted in the Agent column
> ("Junie via Air", "Codex via Air", etc.), with the model and the track.

| Time | Agent (model) | Track | Task | Files | Decisions | Uncertain |
| --- | --- | --- | --- | --- | --- | --- |
| 14:20 | lead + two subagents (Sonnet) | A | Commit graph lanes; two independent algorithms compared, one-pass kept | `graph/`, `ui/repository/CommitGraph.kt` | one-pass with `active[lane]` table; snapshot lanes untrimmed | dark palette not captured yet |
| 17:30 | lead (main session) | B+A | Review loop: diff, apply with snippet re-check, commit; LocalReviewAgent fallback | `review/`, `JGitRepository.kt`, `RepositoryViewModel.kt` | diff against working tree; fallback is not the product AI | no Air session; real key call still to do |
| 18:15 | Claude Code (Fable 5.1) | C | Android clone, Repos/Graph, phone review, bottom sheet, push | `ui/phone/`, `JGitRepository.kt` | phone account in settings replaces per-clone token; checkout guard during review | device proof pending |
| 19:25 | lead (main session) | A | `ReviewToolbox` read-only tools, path jail, 6-call/64 KB budget | `review/ReviewToolbox.kt` | tools read only tracked, reviewable files | provider tool support varies |
| 19:38 | Claude Code (Opus) | D | Ktor share server + client, Share button, Shared reviews | `server/`, `share/`, `ReviewPanel.kt` | re-anchor shared nits; checkout the review branch first | cross-device replay pending |
| 22:05 | lead (main session) | A | Real tool-call review proof: `read_file Pricing.kt`; four Koog loop bugs fixed | `KoogLlmClient.kt`, `LlmReviewAgent.kt` | 32k output + effort low; normalize tool ids | cost/latency not measured |
| 23:20 | Claude Code (Fable 5.1) | C | GitHub/GitLab device flow, account display | `settings/ForgeClient.kt`, `ConnectScreen.kt` | client ids as public constants; no refresh token | GitLab self-hosted out of scope |
| 23:37 | Claude Code (Fable 5.1) | C | Branch card: upstream, ahead/behind, 44 dp sync actions | `PhoneGraphScreen.kt` | pushed banner and sync status on Graph | none |
| 00:30 | lead (main session) | B | Data-safety audit: dirty pull, minSdk 33, settings save, atomic move | `JGitRepository.kt`, `FileSettingsStore.kt`, `SettingsViewModel.kt` | FF-only on a dirty tree; minSdk 33 | excludes Android < 13 |
| 00:50 | lead (main session) | D | Dogfooding pass on `ticket/104`; Nitpick found the real `Aborted` pull bug (#114) | `docs/demo/dogfooding.md` | 2 nits, 1 true bug, 1 already fixed | one capture only |
| 01:20 | lead + subagents | — | Six-lane audit: spec, architecture, demo path, security, UX, release | `docs/audits/` | no P0 in code; P0 was submission packaging | tests not run in the audit |
| 02:00 | lead (main session) | — | P1/P2 fixes: applied-suggestion guards, indentation, tool loop, re-anchor, unreadable file, localhost server, contrast | `RepositoryViewModel.kt`, `ReviewAnchoring.kt`, `KoogLlmClient.kt`, `server/Main.kt` | 7 PRs, JVM/server/desktop green | full `check.sh` needs an SDK machine |
| 11:45 | Junie Lite via Junie CLI (junie-lite, served as Qwen3.6-27B) | A | #255: known-models test, every provider lists a model and the defaults point at a known one | `settings/LlmModelsTest.kt`, `docs/demo/junie-255-transcript.txt` | no Air on the laptop: the CLI transcript replaces the captures; unrequested helper rejected | none |
