# Ticket 36-sync-toolbar

- Status: in review; fixes after self-review (PR #72)
- Human owner: @zooldyyk
- Track: B + C (desktop toolbar, Android Graph screen)
- Agent: lead
- Model: main session
- Escalations: none
- Files: `shared/src/commonMain/.../domain/TrackingStatus.kt`,
  `git/GitRepository.kt`, `ui/repository/{RepositoryViewModel,RepositoryScreen,SyncStatus}.kt`,
  `ui/phone/{PhoneFlow,PhoneGraphScreen}.kt`, `ui/App.kt`, `jvmSharedMain/.../JGitRepository.kt`,
  `jvmTest/.../JGitRepositoryTest.kt`, `composeResources/values{,-fr}`

## Log

- 20:55 — `TrackingStatus(ahead, behind, upstream)` in the git contract, implemented with
  `BranchTrackingStatus`; `upstream` shortened to `origin/main`.
- 21:00 — VM: `fetchBranch`, `isFetching`, `fetchError`, `tracking` reloaded after load,
  commit, fetch, pull, push and checkout. `hasUnpushedCommits` uses `ahead` when it exists.
- 21:05 — desktop: Fetch / Pull n / Push n in the toolbar, `↑n` on the current branch,
  `↑n ↓m` in the status bar. Android: same three actions on the Graph screen.
- 21:10 — JGit test: clone with a local commit (ahead 1), then remote commit + fetch
  (ahead 1, behind 1, upstream `origin/main`). **126 JVM tests, 0 failures**.
- 21:20 — self-review: ⌘⇧F/L/P tooltips, separator before the CTA, Push hidden without
  upstream, `↑n` actually read from the tree, Android counters, `sync_*` strings EN/FR,
  fetchError cleared by a pull. Remaining: Android branch card 44 dp (the actions are
  still in the top bar), `DECISIONS.md` (human file), `check.sh` with SDK. **126 JVM
  tests, 0 failures**.

## Decisions

- Without upstream, the counters disappear and **Push is not offered**: publishing a
  branch is out of scope (the user can do it with git). Pull/Fetch stay visible.
- Fetch does not touch the working tree; Pull keeps rule #42 (conflict cancelled, review
  reset if commits arrive).
- A single `isSyncing` for Fetch / Pull / Push / Checkout.

## Verifications

- `:shared:jvmTest --rerun-tasks`: **126 tests, 0 failures**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` green.
- Full `./scripts/check.sh` not run (no Android SDK here): DoD item still
  open, do not merge before the machine with the SDK.
- Not replayed by hand: desktop toolbar and Android buttons.

## Uncertain

- `BranchTrackingStatus.of` requires a branch with an upstream; a local branch without
  upstream returns `null` and shows no counters.
- The buttons are not greyed out, only neutralised by `isSyncing`; a disabled visual
  state may follow in the polish.
- Android: the three actions are in the top bar, not yet the 44 dp branch card of
  handoff §4.2; the counters are shown in the header. Deviation accepted given the time.
- The "Push hidden without upstream" decision is recorded here, but **not yet in
  `docs/DECISIONS.md`** (file reserved for humans).
