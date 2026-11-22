# Ticket 78-publish-branch

- Status: ready for PR
- Human owner: @zooldyyk
- Track: B + C (git / desktop toolbar + Android Graph)
- Agent: lead
- Files: `git/{GitRepository,JGitRepository}.kt`, `ui/repository/{RepositoryViewModel,RepositoryScreen}.kt`,
  `ui/phone/{PhoneFlow,PhoneGraphScreen}.kt`, `ui/App.kt`, strings, `jvmTest/.../JGitRepositoryTest.kt`

## Log

- 23:30 — `publishBranch`: explicit push `refs/heads/<branch>:refs/heads/<branch>` to
  `origin`, checks the JGit statuses, then writes `branch.<name>.remote/merge` to create
  the upstream. Bare test: new `feature` branch, `origin/feature` upstream, ahead/behind
  at 0, ref visible in the bare repository.
- 23:35 — `publishBranch` VM (isPushing/pushError), desktop and Android toolbar: Publish
  replaces Push when the branch has no upstream; action only if `origin` exists.

## Decisions

- The button no longer appears without `origin`: no dead button.
- Publish uses the same `isPushing`/`pushError` as Push: a single network operation.

## Checks

- `:shared:jvmTest --rerun-tasks`: JGit tests green.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` green.
- Android not replayed (SDK missing).

## Uncertain

- An `origin` remote without a remote branch but with another remote is not published;
  out of scope.
