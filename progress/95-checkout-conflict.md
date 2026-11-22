# Ticket 95-checkout-conflict

- Status: ready for PR
- Human owner: @zooldyyk
- Track: B desktop
- Agent: lead
- Files: `ui/repository/RepositoryViewModel.kt`,
  `commonTest/.../{RepositoryViewModelTest,FakeGitRepository}.kt`

## Log

- 23:45 — JGit puts conflicting paths after a `\n`; the status bar is on one line
  and only showed "Checkout conflict with files: ". The message is flattened
  (`lineSequence().joinToString(" ")`) before being stored.
- ViewModel test: `FakeGitRepository.checkoutError` throws a multi-line conflict and we
  check `Checkout conflict with files: src/Graph.kt`.

## Decisions

- No new error type: the `GitRepository` contract stays unchanged, the VM normalizes
  the existing JGit message.
- Other checkout errors keep their message.

## Checks

- `:shared:jvmTest --rerun-tasks` + `:desktopApp:compileKotlin` green.
