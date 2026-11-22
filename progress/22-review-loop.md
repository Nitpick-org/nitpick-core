# Ticket 22-review-loop

- Status: ready for PR, wired to the Koog agent merged in #51
- Human owner: @zooldyyk
- Track: B + A (desktop + core)
- Agent: lead, without Air session (to be completed for the bonus)
- Model: main session
- Escalations: none
- Files: `shared/src/jvmSharedMain/.../JGitRepository.kt`, `commonMain/review/*`, `commonMain/ui/repository/*`, `commonMain/composeResources/values*`, jvm/common tests

## Log

- 17:30 — JGit: `diffAgainst` (base → working tree), `writeFile`, `stage` (add/rm),
  `commit`. Tests on a temporary repo, including an uncommitted change.
- 17:45 — `ReviewAnchoring`: applies a suggestion while checking `originalSnippet`,
  re-anchors if the lines have moved, refuses if the snippet has disappeared. Tests.
- 18:00 — `LocalReviewAgent`: deterministic fallback (println, TODO/FIXME, `!!`, lines
  > 120) behind `ReviewAgent`. This is not the final model: it allows building and
  demonstrating the loop without an API key.
- 18:15 — UI: Diff and Review tabs, file/hunk/line list, nit cards,
  Apply/Dismiss, commit bar. The CTA starts the review.
- 18:25 — `RepositoryViewModel`: diff on load, review, apply, dismiss, commit,
  reload of the graph/diff.
- 18:35 — rebase on #51: `AppContainer.reviewAgent` (LlmReviewAgent + KoogLlmClient) is
  passed to the ViewModel. The real Koog agent is therefore the production path; `LocalReviewAgent`
  remains only a test/dev fallback.

## Decisions

- `diffAgainst` compares the base to the **working tree** (not just HEAD), so that the diff
  refreshes after Apply.
- The local agent is explicitly a **fallback**; the `ReviewAgent` contract does not change
  when Koog arrives.
- A suggestion path stays validated by snippet before writing.

## Verifications

- `:shared:jvmTest --rerun-tasks`: **71 tests, 0 failures** (JGit, anchoring, prompt,
  parser, local review, diff, graph, settings, VM).
- `:shared:compileKotlinJvm` and `:desktopApp:compileKotlin` green.
- Full check not run: no Android SDK in this environment.

## Uncertain / blocking

- #20 is merged (#51): the prompt, the parser, the anchoring and `KoogLlmClient` are in main.
  PR #50 only wires them to the UI; a real call with a key remains to be made.
- No Air proof for this ticket; to be done by H2/H1 before the demo.
- The Apply/Commit UI has not been replayed by hand on a screen (no GUI here).

## Next

1. Make a real call with a key (Anthropic/OpenAI) and capture the loop.
2. Replay the loop on the demo repo and capture (Air + desktop).
3. Add the "Review in progress" mode / errors from the handoff (#33).
