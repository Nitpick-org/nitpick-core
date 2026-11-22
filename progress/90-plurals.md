# Ticket 90-plurals

- Status: in progress
- Human owner: @pepito2t (H3), taken from H1 to leave him on the demo P0s
- Track: C android+polish
- Agent: Claude Code, human session
- Model: Fable 5.1
- Escalations: none
- Allowed files: composeResources/values{,-fr}/strings.xml, the 4 screens that read these strings, one commonTest test

## Log

- 00:05 — worktree created.
- 00:15 — `status_commits`, `commit_bar_applied` become `<plurals>`; `review_nits_on_files` carried two quantities (nits, files) that a single `<plurals>` cannot agree: split into `review_nits` + `review_changed_files`, concatenated in `ReviewPanel`. Read with `pluralStringResource` (Compose resources, a single number to agree per string).
- Test: `CountStringsTest` (jvmTest) reads both EN/FR forms in the `strings.xml` files. The Compose lookup (`getPluralString`) requires a display on desktop and `ResourceEnvironment` is internal: not testable headless.

- 00:40 — ponytail review: `diff_files` ("1 files", desktop diff header) also becomes a plural; `review_summary` ("%1$d nits · %2$d bug") had no caller left, removed in EN and FR.

## Uncertain

- French has the same `one`/`other` rule as English for these words; no other language is provided.
