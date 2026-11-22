# Human review log

> Every human rejection or correction of an agent's work, one line each. This is the
> evidence for the "agentic process" criterion (§11 of `docs/CONTEXT.md`) and the material
> for the "here is what we refused from our agents" slide. Written by humans, never by an
> agent.

| Time | Who | Ticket / PR | Agent (model) | What | Why | What changed |
| --- | --- | --- | --- | --- | --- | --- |
| 15:10 | H2 | #16 | subagents | First octopus test expected `[0,0,1,2,0]` | Lanes 1/2 carry branches and converge at the root | Test corrected to `[0,0,0,0,0]` |
| 18:45 | H2 | #22 | lead | `LocalReviewAgent` was the default in the ViewModel | Product review must use Koog | `AppContainer` wires `LlmReviewAgent(KoogLlmClient)`; local agent stays a test fallback |
| 19:05 | H3 | #23 | Claude Code | JGit tests wrote into the worktree index through `GIT_DIR` | Hook environment leaked into in-process JGit | Hook unsets `GIT_DIR`/`GIT_INDEX_FILE`; factory sets the git dir |
| 22:05 | H2 | #43 | lead | Tool loop lost the final answer and re-injected reasoning blocks | Real provider call exposed four bugs | Reasoning dropped, tool ids normalized, 32k output, effort low |
| 00:35 | H3 | #81 | Claude Code | A failed push on Graph showed nothing | `syncStatusMessage` ignored `pushError` | Status line now shows push failures |
| 00:45 | H2 | #104/#114 | Nitpick | Pull said `Pull failed: Aborted` on a dirty diverged tree | JGit returns `ABORTED` without throwing; the catch never ran | PR #118 raises the uncommitted-changes message |
| 02:20 | H2 | #174 | lead | Conflict between anchor ellipsis and separate `:line` | Both fixes had to coexist | Keep `StartEllipsis` on the path + separate `:line` text |
