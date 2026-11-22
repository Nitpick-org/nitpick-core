# Ticket 35-settings-android

- Status: ready for review (PR)
- Human owner: @pepito2t (H3)
- Track: C android+polish
- Agent: Claude Fable 5.1 (interactive session)
- Model: fable
- Escalations: none
- Allowed files: shared/src/commonMain/kotlin/dev/nitpick/app/ui/settings/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/ui/components/**,
  shared/src/commonMain/composeResources/**

## Done

- `SettingsScreen` (phone) rebuilt per handoff §4.4: `PhoneTopBar`, `canvas` background,
  `surface` cards; Theme as a 48 dp `NitpickSegmentedControl`; provider as a vertical
  52 dp `NitpickRadioGroup` with a subtitle; model as a full-width select; API key 48 dp
  with Show/Hide and help; the key is hidden when Ollama is selected (no key).
  Every change is saved immediately, no Save on phone.
- `ModelSelector` and `ApiKeyField` extracted from `SettingsDialog` into `SettingsFields.kt`,
  shared desktop / phone.
- `NitpickSegmentedControl(height)`, `NitpickRadioGroup(vertical, subtitle, rowHeight)`,
  `NitpickTextField(minHeight)`.

## Decisions

- Key help: same text as desktop ("app settings file"), not "Android Keystore": it is the
  truth of the storage.
- Ollama subtitle: "self-hosted on this machine, no key"; the handoff said "asks for a
  server URL" but no URL field exists.
- Android states (empty, errors): already delivered by #23 (Repos) and #60 (review); nothing here.
- No "Review server URL" field: its behaviour arrives with #48.

## Uncertain

- Visual comparison with `Phone.dc.html` done on an emulator screenshot, not on a device.

## Log

- 19:19 — worktree created.
- 19:35 — read the ticket and the phone flow.
- 19:50 — screen, components, strings; compiles and `check.sh` green.
