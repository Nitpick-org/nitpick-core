# Ticket 37-commit-filters

- Status: ready for PR
- Human owner: @zooldyyk
- Track: B desktop
- Agent: lead
- Files: `shared/src/commonMain/.../ui/repository/{CommitFilters,RepositoryScreen}.kt`,
  `commonTest/.../ui/repository/CommitFiltersTest.kt`, `composeResources/values{,-fr}/strings.xml`

## Log

- 19:00 — pure model `CommitFilters` + `filterCommits`: text/hash, branch (ancestors of the
  tip), exact author, date (today/week/month) with `now` injected.
- 19:05 — UI: "Text or hash" field + Branch/User/Date dropdowns + Clear under the Commits
  header. Active filter → **graph hidden**, simple list (message, author, hash, date);
  empty filter → full graph.
- 19:08 — tests 5 cases: text/subject, hash, author, branch, date; 100 JVM tests green.

## Decision

- Cheapest option recommended by the issue: **hide the graph** when a filter is
  active. The lanes are not recomputed on a partial list (they would draw
  edges to absent rows).

## Checks

- `:shared:jvmTest --rerun-tasks`: **100 tests, 0 failure**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` green.
- Full `./scripts/check.sh` not run (no Android SDK here).

## Uncertain

- The date's `now` is frozen when the screen opens; good enough for a demo session.
- Remote branch: we take the first local ref of the same name, otherwise the remote one.
