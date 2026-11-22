# Ticket 240-provider-icons

- Status: in progress
- Human owner: @H2
- Track: C polish (shared UI)
- Agent: Claude Code (Fable 5.1, interactive session)
- Allowed files: `shared/src/commonMain/kotlin/dev/nitpick/app/ui/components/NitpickRadioGroup.kt`,
  `shared/src/commonMain/kotlin/dev/nitpick/app/ui/settings/*`,
  `shared/src/commonMain/composeResources/drawable/ic_{anthropic,openai,google,ollama}.xml`,
  `shared/src/commonTest/kotlin/dev/nitpick/app/ui/settings/LlmProviderIconTest.kt`,
  `THIRD-PARTY-NOTICES.md`

## Done

- `NitpickRadioGroup` takes an optional `icon` per option. It replaces the label text; the label
  becomes the content description and a hover tooltip, so the names stay for a screen reader and
  on desktop.
- Both provider selectors (desktop dialog, Android screen) pass the provider mark; Android keeps
  its hint subtitle.
- Four Simple Icons marks, tinted `text` / `textSecondary` like the segmented control; credited
  in `THIRD-PARTY-NOTICES.md`.
- Hover and press feedback stays on the radio disc only (row click with no indication, the
  disc carries the row's interaction source), after H2's first look.
- A greyed "JetBrains AI (Junie)" placeholder after the four providers, on both platforms: no
  click, no call, the reason in the tooltip and in the Android subtitle. H2 decided reading B of
  #227 this way on 22/11.
- Then H2 asked for a connectable Junie (22/11, 10:50). Probe first: the Junie CLI 26.11.2
  signed in on this laptop, free `junie-lite` model, our review prompt on a diff with a planted
  off-by-one and SQL injection: 9 s, cost 0, both bugs found, our JSON schema, verbatim snippets.
  So `JUNIE` is a real provider on desktop: `JunieCliLlmClient` (jvmMain) runs
  `junie --task … --output-format json --act-as-interactive --mcp-default-locations false` in an
  empty temp project, reads the `result` of the JSON envelope, and `KoogLlmClient` hands the
  JUNIE provider to it (plain prompt, no tools). Android keeps the greyed placeholder: no CLI
  on a phone. A missing or signed-out CLI is its own `ReviewFailure.JunieUnavailable` with a
  terminal instruction, EN and FR.

## Decisions (Junie)

- `--act-as-interactive` is mandatory: without it `result` is Junie's summary of its work, not
  the model's answer. `--mcp-default-locations false` keeps the user's own MCP servers out of a
  review. Both pinned by `JunieCliLlmClientTest`.
- Junie gets an empty temp project, never the open repository: the diff is in the prompt and
  Junie's own tools must not touch the user's files (guardrail in `AGENTS.md`).
- The binary is looked up in `~/.local/bin`, `/usr/local/bin`, `/opt/homebrew/bin`, then PATH:
  an app launched from the Dock does not have the shell's PATH.
- `docs/CONTEXT.md` still lists a runtime Junie integration as WON'T; H2 overrode it on 22/11.
  Row proposed in `docs/QUESTIONS.md` on the #227 branch.

## Decisions

- Monochrome marks (Google's "G" included): same treatment as the GitHub and GitLab marks already
  in the app, readable in both themes.
- The status bar still prints the provider name next to the model: a glyph alone is not
  self-explanatory inside a line of text.

## Uncertain

- Not replayed on a device in this session; desktop replayed by H2 for the icons.
- A real Junie review on a full branch (prompt size, time against the 90 s review timeout): the
  CLI probe used a 60-line diff and took 9 s.

## Known limits (Junie)

- The task is one command-line argument: 100 000 characters max, refused with a clear message
  above that. The CLI has no file input for the task.
- Command-line arguments are visible to every local process (`ps`): the diff under review is,
  too, for the length of the review. The HTTP providers never exposed it this way.

## Journal

- 22/11 — ticket opened from H2's request while working on #227; worktree created.
