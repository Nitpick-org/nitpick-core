# Perf audit — git / diff hot paths

- Status: reviewed (second pass, Fable, 21/11 evening); still to replay on an ART device
- Track: desktop + Android performance, shared core
- Agent: lead (DeepSeek), 21/11 ~22:00; re-read + Android side: lead (Fable)
- Files: `git/JGitRepository.kt`, `ui/repository/{RepositoryViewModel,DiffPanel,
  RepositoryScreen,CommitGraph}.kt`, `domain/Diff.kt`, `review/ReviewContext.kt`,
  `settings/{Settings,LlmModels}.kt`, `ui/settings/SettingsViewModel.kt`,
  `ui/theme/NitpickTheme.kt`, `ui/phone/PhoneReviewScreen.kt`, JVM/common tests,
  `docs/PERFORMANCE-AUDIT.md`
- Full report: `docs/PERFORMANCE-AUDIT.md` (measurements, protocol, tracks not done)

## Short explanation

The slowness did not come from Compose but from two unbounded JGit paths:

- `log()` used JGit's TOPO sort, which walks **the whole history** before returning the
  first line. `buildReviewContext` paid that cost again for 10 commit messages.
- `diffAgainst()` compared the whole working tree: every tracked file was hashed on every
  refresh, even when 4 files had changed.

Fixed: time window + in-memory topological sort for `log()`; filter on the paths that
actually changed (status + fork→HEAD) for `diffAgainst()`, with a tree→tree path when
the working tree is clean. Measurements: `log(500)` 449 → 5 ms on 100k commits;
`diffAgainst` 234 → 28 ms (clean) / 107 ms (dirty) on 3k files.

Additions: single-flight diff refresh, precomputed line counters, grouped `stage()`,
single read per file at re-anchoring, settings not rewritten without a change,
UI micro-allocations. The Anthropic default moves to Sonnet (Opus stays in the list).

## Review done (Fable, 21/11 evening)

The 4 points below are reviewed: verdicts and the edge case found (file deleted
fork→HEAD but recreated untracked: it enters the review diff since the removal of
`TrackedFilter`) are in `docs/PERFORMANCE-AUDIT.md` § Review. `check.sh` green on a
machine with the SDK; counts verified: jvmTest 147, testAndroidHostTest 107, 0 failures. Android side
added to the report (no device plugged in: code analysis + levers, no ART
measurements). Outside perf, noticed along the way: after a process death, `PhoneFlow` restores
`screen = Graph` (rememberSaveable) on an empty state — empty Graph screen instead of Repos.

## To review first (Fable)

1. `JGitRepository.diffAgainst` + `changedPathsBetween`: rename, deletion, staged
   file, clean/dirty working tree. Matching `JGitRepositoryTest` tests.
2. `JGitRepository.log` + `childrenBeforeParents`: children-before-parents order, limit 500,
   reversed clock (`logKeepsChildrenBeforeParentsWhenAClockSkewInvertsCommitTimes`).
3. `RepositoryViewModel.refreshDiff`: single-flight, cancellation, repository change.
4. `LlmModels`: Sonnet by default is a product choice; an existing `settings.json` keeps
   Opus.

## Uncertain / not done

- The review prompt can still reach 120k characters of file content; reducing
  `MAX_PROMPT_TOTAL_CHARS` to 60k is a quality/latency track, not applied.
- `git status()` remains a floor of ~20–30 ms per diff; a cache keyed on HEAD + index
  mtime is possible but risky (external editing).
- The Sonnet default does not migrate an existing setting.
- ~~The full `./scripts/check.sh` has not run~~: done in the second pass, green (Android
  included). Nothing on ART, still.
- `diffAgainst` edge case (untracked file at a path deleted by the branch): not
  fixed, H2 decision — clean fix = re-filter on the index.

## Next

- Replay the demo on a real repository and on ART, compare the feel before/after; cold
  start via `am start -W`.
- H3 decision: Android demo in release (a `signingConfig` to add) or in debug.
- Decide with H1/H2: prompt budget, review cache for the demo, streaming.
