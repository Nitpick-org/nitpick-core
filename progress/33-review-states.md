# Ticket 33-review-states

- Status: ready for PR (desktop; Android will follow with #23)
- Human owner: @dovatgabriel (H1) for the review; H2 implementation
- Track: B + C (shared UI)
- Agent: lead
- Files: `shared/src/commonMain/.../ui/repository/{ReviewPanel,RepositoryViewModel,RepositoryScreen}.kt`,
  `composeResources/values{,-fr}/strings.xml`, `commonTest/.../RepositoryViewModelTest.kt`

## Log

- 18:45 — typed states: `ReviewFailure.MissingKey`, `Network`, `Timeout`, `Other`; mapping
  from `MissingApiKeyException`, `ReviewProviderException` (timeout by message/cause).
- 18:50 — ReviewPanel: progress (4 steps), missing key → Open Settings, network/timeout
  → Retry, clean branch → CLEAN, unchanged result.
- 18:55 — ViewModel tests: missing key, provider/network, distinct timeout.

## Decisions

- The provider error stays reformulated by `KoogLlmClient`; the UI never shows the key.
- The "Push N commits" button of the clean state is omitted as long as #36 (fetch/pull/push) is
  not there; the state simply indicates the clean branch.

## Checks

- `:shared:jvmTest --rerun-tasks`: added tests, 0 failure.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` green.
- Android: shared states ready, Android screen to wire with #23.

## Next

- Replay the 6 states on the desktop/Android screen and compare with the handoff.
