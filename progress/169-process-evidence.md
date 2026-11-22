# 169 — Process evidence draft

Status: draft for H1/H2. Do not copy into `docs/AGENT_LOG.md` or
`docs/REVIEW_LOG.md` without human validation: those two files are human-owned.

Sources: `progress/<ticket>.md`, `progress/16-graph.md:37-52`,
`docs/demo/dogfooding.md`, `docs/demo/dogfooding-104-review.png`.

## AGENT_LOG candidates

| Heure | Agent (modèle) | Piste | Tâche | Fichiers | Décisions | Incertain |
| --- | --- | --- | --- | --- | --- | --- |
| 14:20 | lead + two subagents (Sonnet) | A | Commit graph lanes; two independent algorithms compared, one-pass kept | `graph/`, `ui/repository/CommitGraph.kt` | one-pass with `active[lane]` table; snapshot lanes untrimmed | dark palette not captured yet |
| 17:30 | lead | B+A | Review loop: diff, apply with snippet re-check, commit; LocalReviewAgent fallback | `review/`, `JGitRepository.kt`, `RepositoryViewModel.kt` | diff against working tree; fallback is not the product AI | no Air session; real key call still to do |
| 18:15 | Claude Code (Fable 5.1) | C | Android clone, Repos/Graph, phone review, bottom sheet, push | `ui/phone/`, `JGitRepository.kt` | phone account in settings replaces per-clone token; checkout guard during review | device proof pending |
| 19:25 | lead | A | `ReviewToolbox` read-only tools, path jail, 6-call/64 KB budget | `review/ReviewToolbox.kt` | tools read only tracked, reviewable files | provider tool support varies |
| 19:38 | Claude Code (Opus) | D | Ktor share server + client, Share button, Shared reviews | `server/`, `share/`, `ReviewPanel.kt` | re-anchor shared nits; checkout the review branch first | cross-device replay pending |
| 22:05 | lead | A | Real tool-call review proof: `read_file Pricing.kt`; four Koog loop bugs fixed | `KoogLlmClient.kt`, `LlmReviewAgent.kt` | 32k output + effort low; normalize tool ids | cost/latency not measured |
| 23:20 | Claude Code (Fable 5.1) | C | GitHub/GitLab device flow, account display | `settings/ForgeClient.kt`, `ConnectScreen.kt` | client ids as public constants; no refresh token | GitLab self-hosted out of scope |
| 23:37 | Claude Code (Fable 5.1) | C | Branch card: upstream, ahead/behind, 44 dp sync actions | `PhoneGraphScreen.kt` | pushed banner and sync status on Graph | none |
| 00:30 | lead | B | Data-safety audit: dirty pull, minSdk 33, settings save, atomic move | `JGitRepository.kt`, `FileSettingsStore.kt`, `SettingsViewModel.kt` | FF-only on a dirty tree; minSdk 33 | excludes Android < 13 |
| 00:50 | lead | D | Dogfooding pass on `ticket/104`; Nitpick found the real `Aborted` pull bug (#114) | `docs/demo/dogfooding.md` | 2 nits, 1 true bug, 1 already fixed | one capture only |
| 01:20 | lead + subagents | — | Six-lane audit: spec, architecture, demo path, security, UX, release | `docs/audits/` | no P0 in code; P0 was submission packaging | tests not run in the audit |
| 02:00 | lead | — | P1/P2 fixes: applied-suggestion guards, indentation, tool loop, re-anchor, unreadable file, localhost server, contrast | `RepositoryViewModel.kt`, `ReviewAnchoring.kt`, `KoogLlmClient.kt`, `server/Main.kt` | 7 PRs, JVM/server/desktop green | full `check.sh` needs an SDK machine |

## REVIEW_LOG candidates — real human corrections

| Quand | Qui | Ticket / PR | Agent | Quoi | Pourquoi | Ce qui a changé |
| --- | --- | --- | --- | --- | --- | --- |
| 15:10 | H2 | #16 | subagents | First octopus test expected `[0,0,1,2,0]` | Lanes 1/2 carry branches and converge at the root | Test corrected to `[0,0,0,0,0]` |
| 18:45 | H2 | #22 | lead | `LocalReviewAgent` was the default in the ViewModel | Product review must use Koog | `AppContainer` wires `LlmReviewAgent(KoogLlmClient)`; local agent stays a test fallback |
| 19:05 | H3 | #23 | Claude Code | JGit tests wrote into the worktree index through `GIT_DIR` | Hook environment leaked into in-process JGit | Hook unsets `GIT_DIR`/`GIT_INDEX_FILE`; factory sets the git dir |
| 22:05 | H2 | #43 | lead | Tool loop lost the final answer and re-injected reasoning blocks | Real provider call exposed four bugs | Reasoning dropped, tool ids normalized, 32k output, effort low |
| 00:35 | H3 | #81 | Claude Code | A failed push on Graph showed nothing | `syncStatusMessage` ignored `pushError` | Status line now shows push failures |
| 00:45 | H2 | #104/#114 | Nitpick | Pull said `Pull failed: Aborted` on a dirty diverged tree | JGit returns `ABORTED` without throwing; the catch never ran | PR #118 raises the uncommitted-changes message |
| 02:20 | H2 | #174 | lead | Conflict between anchor ellipsis and separate `:line` | Both fixes had to coexist | Keep `StartEllipsis` on the path + separate `:line` text |

## Multi-agent comparison

`progress/16-graph.md:37-52` is the documented comparison: two subagents,
one-pass vs two-pass, tests and trade-offs, decision to keep the one-pass
algorithm and port B's edge cases.

## Air

No Air/Junie session or capture exists in the repo. State honestly:
"Claude Code + DSH subagents; no Air capture." Do not claim "built with Air".
