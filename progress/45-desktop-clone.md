# Ticket 45-desktop-clone

- Status: done, to review then merge (issue #45). Manual proof with a private repo still to do.
- Track: B
- Agent: Claude Code, main session
- Model: opus 5.5
- Escalations: none
- Files: `ui/repository/CloneDialog.kt` (new), `ui/repository/RepositoryScreen.kt`,
  `ui/App.kt`, `composeResources/values{,-fr}/strings.xml`,
  `commonTest/.../ui/repository/CloneCredentialsTest.kt`

## Log

- Branch `ticket/45-desktop-clone` from `main`. #21 is closed: `GitRepositoryFactory.clone`
  and `RepositoryViewModel.cloneRepository` exist, nothing to add on the git side.
- `CloneDialog`: URL, masked token (Show/Hide), destination folder (Choose… button that
  reuses the native `pickDirectory` picker already passed to `App`), folder name inferred from
  the URL. The dialog stays open during the clone ("Cloning…" button) and shows the JGit
  error in place; it closes when the repo is loaded.
- `cloneCredentials(url, token)`: empty token → anonymous clone; otherwise `x-access-token`, or
  `oauth2` if the URL contains `gitlab`. 3 JVM tests run, 0 failures.
- Toolbar: "Clone repository…" next to "Open repository…", only if the platform has
  a folder picker (desktop). `recentRepositories`: existing effect of `App`.
- `check.sh` green.
- Ponytail review, points 1 and 2 fixed:
  - A failed clone closed the open repo (`load()` clears the state before git). The clone now
    runs in its own job and goes through `RepositoryUiState.clone` (path + error);
    only a successful clone replaces the state. Test `aFailedCloneKeepsTheOpenRepositoryAndReportsTheError`
    run, green. Effect also on Android: a failed clone leaves the previous state.
  - The "clone in progress / error / done" logic of `CloneScreen` and `CloneDialog` is
    shared in `rememberCloneProgress` (`ui/phone/CloneScreen.kt`).
  - Points 3 (GitLab detected on the whole URL) and 4 (fallback to `Settings.gitConnection`)
    left aside.

## Decisions

- The token lives in a `remember` of the dialog: not `settings.json`, nor saved state, nor the
  ViewModel state (already guaranteed by #21).
- No GitHub/GitLab choice in the dialog: it is inferred from the URL.
- Token help: exact text of the issue, new string `clone_token_help`.

## Uncertain

- The "invalid token" / "network" errors are the raw JGit message, as on Android.
- Push/pull after a private clone use `Settings.gitConnection`, not the dialog token:
  without a configured account, pushing a private repo will fail.
- Not verified on screen: a human must clone a private repo with a token (criterion 1).

## Next

- Manual proof: `./gradlew :desktopApp:run`, Clone repository…, private repo + token.
