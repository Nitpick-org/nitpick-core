# Ticket 64 — Sign in with GitHub / GitLab on Android (OAuth device flow)

- Status: in progress (code ready, client ids to fill in)
- Human owner: @pepito2t (H3)
- Track: C android + polish
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Escalations: none
- Allowed files: `shared/src/commonMain/kotlin/dev/nitpick/app/settings/ForgeClient.kt`,
  `shared/src/commonTest/.../settings/ForgeClientTest.kt`, `AppContainer.kt`, `ui/App.kt`,
  `ui/phone/PhoneFlow.kt`, `ui/phone/ConnectScreen.kt`, `composeResources/values*/strings.xml`

## Done

- `ForgeClient` (RFC 8628): `start(host, clientId)` requests the code, `awaitToken` polls
  the token endpoint at the given interval, +5 s on `slow_down` (or the `interval` returned),
  gives up at `expires_in`. The body is read whatever the HTTP status: GitHub answers 200 with
  `error`, GitLab 400. The body never enters an exception (it carries the token).
- Endpoints verified in the official docs (GitHub "Authorizing OAuth apps", GitLab
  "OAuth 2.0 identity provider API"): `/login/device/code` + `/login/oauth/access_token`
  (scope `repo`), `/oauth/authorize_device` + `/oauth/token` (scopes
  `read_repository write_repository`). `Accept: application/json`, otherwise GitHub answers in
  form-encoded. No client secret.
- Connect screen: a "Sign in with GitHub/GitLab" button above the token; it opens the
  browser (`LocalUriHandler`, GitLab's `verification_uri_complete` when it exists), shows the
  user code in large type, "Open browser" and "Cancel" (cancels the polling). The token
  obtained goes through the same `onConnect(GitConnection)` as the typed token.
- The token typed by hand stays available as is.
- `ForgeOAuthClientIds`: public ids as constants; empty = button hidden. GitHub filled in
  (OAuth App "Nitpick", device flow enabled, tokens without expiry).
- Account shown: `GitConnection.login` (GitHub `login`, GitLab `username`, via
  `ForgeClient.fetchLogin`) filled after the connection or a typed token; `@login` replaces
  "Account" in the Repositories bar, and the Connect screen says "Connected to GitHub as
  @login". A failure to read the name does not block the connection.

## Decisions

- No new package: the client lives in `settings/` next to `GitConnection`.
- The client ids are code constants, not a BuildConfig: an OAuth id is public and
  Gradle KMP has no BuildConfig without a plugin.
- Codes and token in `remember`, never in `rememberSaveable` (same rule as the typed token).
- The polling counts the expected time instead of a `withTimeout`: in `runTest` virtual time
  the timeout fired during the MockEngine I/O and made the tests non-deterministic.

## Not done, to mention in the PR

- GitLab self-hosted: `GitConnection` has no forge URL (contract change, H2 approval).
- GitLab token refresh: `expires_in` 7200 s, no `refresh_token` in the device flow
  according to the docs; the user signs in again after 2 h.
- Copying the code to the clipboard (Compose API in transition, no usage in the repo).
- Proof on emulator: impossible without an OAuth App; test recipe handed to H3.

## Proof

- `check.sh` green; `ForgeClientTest`: 6 tests, 0 failure (jvm + androidHostTest).

## Log

- 23:20 — worktree created, GitHub/GitLab docs verified.
- 23:35 — client + tests + screen; check green.
- 23:45 — GitHub client id, polling tolerant to drops, connected account shown.
