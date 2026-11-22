# Ticket 252-desktop-signin

- Status: done, to replay by H2
- Human owner: @H2
- Track: B desktop
- Agent: Claude Code (Fable 5.1, interactive session)
- Allowed files: `shared/src/commonMain/kotlin/dev/nitpick/app/ui/settings/ForgeSignIn.kt`,
  `shared/src/commonMain/kotlin/dev/nitpick/app/ui/settings/SettingsDialog.kt`,
  `shared/src/commonMain/kotlin/dev/nitpick/app/ui/phone/ConnectScreen.kt` (two helpers made
  internal), `shared/src/commonMain/kotlin/dev/nitpick/app/ui/App.kt`

## Done

- Desktop Settings → Git: "Sign in with GitHub / GitLab" (device flow), the code to type with
  "Open browser" and "Cancel" while waiting, then the token and login land in the draft; "Signed
  in to GitHub as @login" above the host choice. The pasted-token field stays below, as on the
  phone. Strings reused from the phone screen, EN and FR.

## Decisions

- `ForgeSignIn` is a new block rather than a reuse of `ConnectScreen`: the phone screen owns a
  full-screen layout with its own top bar and bottom button; only its two helpers
  (`loginOrBlank`, `signInErrorText`) are shared. The 30 lines of device-flow state are
  duplicated on purpose; merging them means a refactor of the phone screen.
- The dialog edits a draft: the sign-in fills `gitConnection` in the draft, Save stores it.

## Uncertain

- No test: the device flow itself is covered by `ForgeClientTest`; the block is UI state only.
- Not replayed in the desktop window in this session.

## Journal

- 22/11 — asked by H2 after a push failed with the raw JGit "no CredentialsProvider" message.
