# Ticket 105-audit-vm-errors

- Status: ready for PR
- Human owner: @dovatgabriel
- Track: B git+desktop, C android+polish
- Agent: Claude Code, main session (quality audit of 22/11)
- Model: Fable, picked by hand by H1
- Escalations: none
- Files: `ui/phone/{PhoneReviewScreen,CloneScreen}.kt`,
  `ui/repository/{RepositoryViewModel,RepositoryScreen,DiffPanel,CloneDialog,CloneProgress}.kt`,
  `ui/components/NitpickReview.kt`, `ui/settings/SettingsViewModel.kt`,
  `jvmSharedMain/.../git/JGitRepository.kt`, their tests, `README.md`, `docs/QUESTIONS.md`,
  `.editorconfig`

## Context

H2 took over the audit issues and merged #109, #111 and #112 while I was writing the same
fixes. My PR #113 was closed as a duplicate. This ticket only contains what those three
PRs left open, re-read on `main` after their merge.

## Log

- 00:35 — worktree created from an up-to-date `main`.
- `PhoneReviewScreen` still did not display `actionError`: #111's log says it is done,
  but the file was not in its diff. Added between the nits list and the
  commit bar.
- Phone severity badge: `nit.severity.name` replaced by the card's `severityLabel`;
  a single `severityTone`.
- Pull messages (#109): three red tests on the merged code. Divergence → "Pull failed:
  Aborted"; unreachable origin → "Uncommitted changes…" wrongly; fast-forward over a modified
  file → sentence about a diverging branch. The two real cases now say "Commit or
  discard your changes before pulling: <paths>", the other errors keep their message.
- Apply or commit during a pull or a checkout: ignored. Found by the `reviewer` agent: a
  suggestion applied during the fetch could be wiped by the reset of a conflicting pull.
- Docs and comments aligned on minSdk 33; Q-020 added; `DiscardOutputStream` removed.
- Leftover from #106: clone helpers moved into `ui/repository/CloneProgress.kt` (no more
  `ui/phone` ↔ `ui/repository` cycle), two duplicates merged, 3 wizard tests deleted,
  `.editorconfig` at 4 spaces for Kotlin.

## Decisions

- The "no apply during a sync" guard only covers pull and checkout, which write
  files. A fetch or a push does not block a tap.
- `CHECKOUT_CONFLICT` leaves the reset branch: JGit 6.10 throws an exception for this case
  and `merge.conflicts` is null there.
- `LocalReviewAgent` stays: removing it makes a parameter mandatory and would break `main` if
  an in-flight PR adds a ViewModel test. Noted in #107.

## Verification

- Red then green tests: 3 on the pull, 1 on the apply during a pull.
- `:shared:allTests`: 169 tests on the JVM, 123 on the Android host, 0 failure.
- `./scripts/check.sh` green at every commit (pre-commit hook).

## Uncertain

- The rendering of `actionError` and of the badge on the phone has no Compose test: to check
  by hand on the device.
- Tests on ART not re-run (no emulator started).
- The demo phone must run Android 13 or later (Q-020).

## Next

- Human review of the PR. The architecture and tooling points are in #107.
