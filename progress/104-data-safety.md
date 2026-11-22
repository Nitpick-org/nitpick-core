# Audit 104-data-safety

- Status: ready for PR
- Human owner: @zooldyyk (taking over the audit)
- Track: A/B/C
- Agent: lead
- Files: `git/JGitRepository.kt`, `settings/{FileSettingsStore,SettingsViewModel}.kt`,
  `gradle/libs.versions.toml`, JGit/ViewModel tests

## Log

- 00:30 — pull: if tracked files are modified, `FastForwardMode.FF_ONLY`. A
  divergence raises "commit or discard" before any reset; `reset --hard` is no longer
  reached with a dirty tree.
- 00:32 — `minSdk` 26 → **33** (JGit/Java APIs used: `readNBytes`, `transferTo`,
  `ByteArrayOutputStream.toString(Charset)`).
- 00:34 — `SettingsViewModel.saveDraft` also copies `gitConnection` and `serverUrl`; only
  `recentRepositories` keeps the value from the store.
- 00:35 — `FileSettingsStore` replaces `File.renameTo` with `Files.move(REPLACE_EXISTING,
  ATOMIC_MOVE)`, with a non-atomic fallback.

## Checks

- New tests: FF pull with an unrelated dirty file, divergent pull with a dirty file
  kept, saving the Git account/server URL.
- `:shared:jvmTest` + `:desktopApp:compileKotlin` green.
- `assembleDebug`/instrumented tests not run (no SDK).

## Uncertain

- `minSdk` 33 excludes devices < Android 13; H1 decision already made.
