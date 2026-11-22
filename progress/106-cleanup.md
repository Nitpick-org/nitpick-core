# Audit 106-cleanup

- Status: partial PR (safe items)
- Human owner: @zooldyyk
- Track: UI/security cleanup
- Agent: lead
- Files: `ui/components/NitpickReview.kt`, `ui/repository/RepositoryScreen.kt`,
  `review/ReviewContext.kt`, `androidApp/src/main/AndroidManifest.xml`, strings

## Done

- Removal of the keyboard shortcut hints (`⌘⇧R/F/L/P`) while no key handler
  exists.
- Review card: `SUGGESTED CHANGE`, `Apply suggestion` and the severity labels
  go through `composeResources` (EN/FR).
- LLM filtering: `*.pem`, `*.key`, `*.p12`, `*.jks`, `local.properties`.
- Android `allowBackup="false"`.

## Remaining from #106 (not done)

- `ui/phone` ↔ `ui/repository` cycle for the clone helpers.
- Duplications `severityTone`, date-label, `selectedDiff`.
- Wizard tests `1+2==3`, `.editorconfig` 2 spaces.
→ ticket left open.

## Checks

- `:shared:jvmTest` + `:desktopApp:compileKotlin` green.
- Secret-file test added.
