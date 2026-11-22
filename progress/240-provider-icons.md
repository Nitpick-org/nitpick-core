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

## Decisions

- Monochrome marks (Google's "G" included): same treatment as the GitHub and GitLab marks already
  in the app, readable in both themes.
- The status bar still prints the provider name next to the model: a glyph alone is not
  self-explanatory inside a line of text.

## Uncertain

- Not replayed on a device or in the desktop window in this session.

## Journal

- 22/11 — ticket opened from H2's request while working on #227; worktree created.
