# Audit 105-visible-errors

- Status: ready for PR
- Human owner: @zooldyyk
- Track: B/C
- Agent: lead
- Files: `ui/repository/RepositoryViewModel.kt`, `ui/phone/PhoneReviewScreen.kt`,
  `commonTest/.../RepositoryViewModelTest.kt`

## Log

- 00:40 — `actionError` is rendered on the phone between the nits and the commit bar;
  it is cleared at the start of a review or a checkout.
- 00:42 — Apply: the `writeLock` covers the state update and checks `appliedIds`.
  Commit: the lock covers stage/commit/log/state and checks `appliedFiles`; a second click
  no longer creates an empty commit.
- 00:44 — `pushError` cleared at the start of a pull/fetch; the commit's `refs()`/`trackingStatus()`
  read outside the `_state.update` block.

## Checks

- `:shared:jvmTest` + `:desktopApp:compileKotlin` green.
- Tests added: apply twice = one patch, commit twice = one commit.

## Uncertain

- The phone `actionError` rendering is not covered by a Compose test.
