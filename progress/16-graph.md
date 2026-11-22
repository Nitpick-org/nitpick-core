# Ticket 16-graph

- Status: in progress, algorithm + preview UI ready for PR
- Human owner: @zooldyyk
- Track: A (shared core) + desktop UI
- Agent: lead (orchestrator) + two comparison sub-agents
- Model: sonnet (sub-agents), main session
- Escalations: none
- Allowed files: `shared/src/commonMain/kotlin/dev/nitpick/app/graph/`, `shared/src/commonTest/kotlin/dev/nitpick/app/graph/`, `shared/src/commonMain/kotlin/dev/nitpick/app/ui/`

## Log

- 14:20 — branch `ticket/16-graph` created from `origin/main` (`adc6a2d`).
- 14:35 — `CommitGraphRow` model and one-pass lane algorithm:
  `active[lane] = expected commit id`. The commit takes the first lane waiting for it,
  the others converge, the first parent continues in the lane, the following parents
  take a free or existing lane. Unknown parents free the lane.
- 14:45 — `CommitGraphScreen` UI: list of commits + coloured lanes + details panel.
  Preview data while waiting for the repo picker (#15); `App()` points at it.
- 15:00 — `commonTest` tests added: linear, branch + merge, orphan, octopus,
  lane reuse, convergence of two children, 500 cap, empty input.
- 15:10 — first manual run: the octopus gave `[0,0,1,2,0]` and not `[0,0,0,0,0]`;
  the test was fixed (lanes 1 and 2 carry the branches, they converge at the
  root). That is the expected behaviour.

- 16:00 — rebased onto `main` after the merge of #15 (#27), pushed as
  `ticket/16-graph-rebased` (no force push on `ticket/16-graph`). Conflict on `App.kt`:
  kept the real flow from #15.
- 16:15 — graph wired to the real repository: `RepositoryViewModel` computes
  `buildCommitGraph(repository.log())` off the UI thread (`Dispatchers.Default`, injectable
  for the tests) and keeps the selected commit (the most recent by default). Lane
  rendering and the details panel move to `ui/repository/CommitGraph.kt`, labels in EN/FR
  and localised date via `formatCommitTime` (#15). Removed: `CommitGraphScreen`,
  `PreviewCommits` and `CommitFormat` (UTC duplicate). 4 ViewModel tests, including the selection.
  Verified on screen on a real repository (lanes, merge, details of the selected commit).

## Multi-agent comparison (jury bonus criterion)

Two independent sub-agents produced an algorithm on the same `Commit` model and
the same cases:

| Agent | Approach | Tests delivered | Strengths | Weaknesses |
| --- | --- | --- | --- | --- |
| `graph-agent-a` | one pass, `active: MutableList<String?>` table | 10 cases, including duplicate/self parent, freed lane, octopus | the shortest, exact snapshots, easy to explain to the team | a single pass mixes allocation and snapshots |
| `graph-agent-b` | two passes (assignment ledger then projection) | 7 cases, including freed lane, orphan, cap | separation of allocation/rendering, trimmed snapshots | ~50 % more code, two logics to keep consistent, different trim convention |

Decision: **keep the one-pass approach (A)**, for three reasons:
1. at 500 commits, the cost is negligible;
2. the team is new to Kotlin: fewer states and internal types to re-read;
3. untrimmed snapshots keep stable lane indexes, which the UI uses
   directly.

B's edge cases (empty input, orphan, reused lane, convergence) were
added to the final tests. The final code also takes over A's parent deduplication and
self-parent guard.

## Verifications

- `GRADLE_USER_HOME=/tmp/nitpick-gradle ANDROID_USER_HOME=/tmp/nitpick-android ./gradlew :shared:jvmTest --rerun-tasks`
  → **8 `CommitGraphTest` tests, 0 failures**.
- `:shared:compileKotlinJvm` → green (the Compose UI compiles).
- full `./scripts/check.sh` not run in this environment: no Android SDK
  (`~/.android` + `platforms;android-37` missing). To run on the team machine.

## Decisions

- First parent = continuation of the commit's lane; the other parents open a
  lane. If a parent is already expected by a lane, that lane is reused.
- Unknown parents (outside the 500 cap or absent): no lane, no edge; the commit's
  lane is freed.
- Colour = `lane % 8`; the UI palette is fixed and readable in light/dark.
- The UI receives `List<Commit>` and does not depend on JGit; #15 will wire `GitRepository.log()`.

## Uncertain / blocking

- The dark theme was not captured; the palette is chosen for both backgrounds (the
  theme itself arrives with #18).
- Android does not display the graph yet: no repository before the clone (#21).

## Next

1. Open the PR from `ticket/16-graph-rebased`, have the UI reviewed.
2. Add a dark desktop screenshot once #18 is merged.
