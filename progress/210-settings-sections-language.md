# Ticket 210 — Settings: working sections, language, About

- Status: done, to be validated on screen
- Human owner: @pepito2t (H3)
- Track: B desktop + C android (language) 
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Allowed files: `ui/settings/{SettingsDialog,SettingsScreen}.kt`, `ui/App.kt`, `ui/AppLocale*.kt`
  (commonMain + jvmMain + androidMain), `settings/Settings.kt` (`language`), strings EN/FR, `SettingsTest`

## Done

- Desktop dialog: the left navigation works (selection, highlight, search filter);
  sections General (theme, language), Review (provider, model, key), Git (account), Sharing (server
  URL), About (icon, tagline, repository link, the team's three GitHub accounts with the GitHub brand,
  clickable). Keymap removed: no configurable shortcut.
- Language: `Settings.language` (System / English / Français), segmented control on desktop and phone.
  Mechanism: `LocalAppLocale` (expect/actual, workaround documented by JetBrains in
  "Manage local resource environment") + `key(language)` around the theme to re-read all the
  strings. Desktop: `Locale.setDefault`; Android: locale + resource configuration.
- Tests: `SettingsTest` (System default, fr tag, file round-trip with the language).

## Log

- 02:55 — worktree, model, per-platform locale, dialog, phone screen, check green.
