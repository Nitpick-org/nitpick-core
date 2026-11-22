# Ticket 79-desktop-credentials

- Status: ready for PR
- Human owner: @zooldyyk
- Track: B desktop
- Agent: lead
- Files: `ui/settings/{SettingsDialog,SettingsFields}.kt`, `ui/App.kt`,
  `composeResources/values{,-fr}/strings.xml`

## Log

- 23:15 — **Git account** section in Settings: GitHub/GitLab host + masked token
  (Show/Hide), read/write hint. Writes `Settings.gitConnection`, already serialized.
- 23:18 — desktop Fetch/Pull/Push only send credentials when `gitConnection` is
  complete; an empty token does not replace a public remote.
- `SettingsTest` tests already present for `GitConnection.toCredentials()` and persistence.

## Decisions

- A single forge account stored in settings (as on Android), not one token per repo.
- The token stays masked and is never logged; it only goes into the JGit header.

## Checks

- `:shared:jvmTest` + `:desktopApp:compileKotlin` green.
- No Compose UI test; the credentials wiring is covered by `SettingsTest` and the
  direct pass of `Settings` to `App`.

## Uncertain

- Desktop clone keeps its own token field (#45); the Settings section is for
  fetch/pull/push of already open repos, not for pre-filling the clone.
