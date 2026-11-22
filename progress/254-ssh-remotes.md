# Ticket 254-ssh-remotes

- Status: done, to replay by H2 on a `git@` repo
- Human owner: @H2
- Track: B desktop (git)
- Agent: Claude Code (Fable 5.1, interactive session)
- Allowed files: `gradle/libs.versions.toml`, `shared/build.gradle.kts`,
  `shared/src/jvmSharedMain/kotlin/dev/nitpick/app/git/{JGitCredentials,JGitRepository,JGitRepositoryFactory}.kt`,
  `shared/src/jvmTest/kotlin/dev/nitpick/app/git/{JGitRepositoryTest,SshPromptsTest}.kt`, `docs/IDEAS.md`

## Done

- `org.eclipse.jgit.ssh.apache` and `org.eclipse.jgit.ssh.apache.agent` (JNA) on the desktop
  `jvmMain` classpath only. JGit finds the transport through ServiceLoader, so the SSH refusal
  from #232 now reads "no SSH session factory": still true on Android, false on desktop.
- `SshPrompts`: the credentials provider for SSH remotes. A new host key is accepted and
  stored (`StrictHostKeyChecking accept-new`); a passphrase prompt is refused with "add the key
  to ssh-agent" instead of hanging. HTTPS remotes keep the token provider.
- Tests ran: `SshPromptsTest` (3) and the desktop SSH test in `JGitRepositoryTest` (fetch and
  clone on `ssh://git@127.0.0.1:1/…` reach the transport and get a refused connection, not
  the Android sentence).

## Decisions

- Accept-new for host keys rather than a dialog: git on the CLI asks once, the app has no
  prompt channel in the git layer, and `known_hosts` already holds github.com for anyone who
  clones over SSH.
- The phone is untouched: no agent on Android and the module's dexing is unverified (H2).

- `DesktopSsh.install()` (jvmMain, called first in `main`): JGit's factory with a lenient
  agent connector. An agent socket that cannot be reached is skipped, as OpenSSH does, instead
  of failing the whole login: H2's `~/.ssh/config` names a 1Password socket that does not exist.
- A refused SSH login ("Cannot log in at github.com:22") becomes one sentence: load the key in
  the agent (`ssh-add --apple-use-keychain`) or use a key without passphrase. JGit cannot read
  the macOS keychain where OpenSSH keeps the passphrase.

## Replayed on H2's laptop (scratch test, not committed)

- Before the lenient connector: "Could not connect to SSH agent via socket '…agent.sockss'".
- After: the file key `~/.ssh/id_ed25519` is tried, it has a passphrase, the sentence above
  shows. OpenSSH itself logs in because macOS hands it the passphrase from the keychain.
  Next step for H2: `ssh-add --apple-use-keychain ~/.ssh/id_ed25519` (and fix the stray `ss`
  on line 42 of `~/.ssh/config`), then the agent path serves the app.

## Uncertain

- A real SSH fetch or push through the app after the key is in the agent: to replay by H2.
- `~/.ssh/config` with `IdentitiesOnly` or a non-default key: honoured by Apache MINA sshd
  per JGit's README, not replayed here.

## Journal

- 22/11 — H2 chose SSH on desktop over an HTTPS switch of the repo; implemented, tests ran.
