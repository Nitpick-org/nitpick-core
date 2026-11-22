# Ticket 34-settings-desktop

- Status: ready for review (PR)
- Human owner: @pepito2t (H3)
- Track: C android+polish (desktop here)
- Agent: Claude Fable 5.1 (interactive session)
- Model: fable
- Escalations: none
- Allowed files: shared/src/commonMain/kotlin/dev/nitpick/app/ui/settings/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/components/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/designsystem/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/App.kt, shared/src/commonMain/composeResources/**,
  shared/src/commonTest/kotlin/dev/nitpick/app/ui/settings/**

## Done

- Desktop `SettingsDialog` (handoff §3.5): 220 dp nav with a search that filters the sections,
  only General filled; Theme as a `NitpickSegmentedControl`, provider as a `NitpickRadioGroup`,
  model as a 300 dp mono select, API key 380 dp with Show/Hide and help naming the provider;
  Cancel / Save; Esc and clicking outside = Cancel.
- Draft in `SettingsViewModel`: nothing is written before Save; Save only copies the four
  dialog fields onto the current settings.
- Two components in `ui/components/` and the catalog.
- `LlmProvider.label` / `ThemeMode.label` labels shared.

## Decisions

- The phone keeps `SettingsScreen` (restyled by #35).
- Key help: "app settings file", not "keystore": it is the truth of the
  current storage.
- No "Review server URL" field: its behaviour arrives with #48, a dead field would be worse.

## Uncertain

- Light/dark visual rendering not compared with the mockup (no click possible from the session).

## Log

- 19:05 — worktree, read of handoff §3.5 and of the existing code.
- 19:25 — dialog, components, draft, tests; compile green.
