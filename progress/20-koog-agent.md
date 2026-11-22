# Ticket 20-koog-agent

- Status: in progress
- Human owner: @zooldyyk (H2)
- Track: A shared core
- Agent: Claude Fable 5.1 (interactive session) + `researcher` (Sonnet) for the Koog docs
- Model: fable
- Escalations: none
- Allowed files: shared/src/commonMain/kotlin/dev/nitpick/app/review/**,
  shared/src/commonTest/kotlin/dev/nitpick/app/review/**,
  shared/src/commonTest/kotlin/dev/nitpick/app/git/FakeGitRepository.kt,
  shared/src/commonMain/kotlin/dev/nitpick/app/domain/Review.kt (the `isFileLevel` property only),
  shared/src/commonMain/kotlin/dev/nitpick/app/AppContainer.kt (wiring),
  gradle/libs.versions.toml, shared/build.gradle.kts, androidApp/build.gradle.kts (packaging),
  docs/RESEARCH.md (Koog section, by the researcher)

## Plan

1. Pipeline without Koog, testable with a fake `LlmClient`: `buildReviewContext` (filtered diff,
   full files, README, 10 commits, language), `buildReviewPrompt` (numbered lines
   `   42 | +code`), `parseReviewResponse` (tolerant JSON), `anchorComment` (check, re-anchor,
   whole-file fallback), `LlmReviewAgent` that composes it all.
2. `KoogLlmClient`: the only file that imports Koog, built from `Settings`
   (provider, model, key); API verified in `docs/RESEARCH.md` before writing.
3. `commonTest` tests: numbered prompt, valid parsing, shifted snippet re-anchored, snippet
   not found → whole file, lockfile filter.
4. Android build as soon as the dependency is in place (dex / META-INF risk).
5. Real call with `APP_ANTHROPIC_API_KEY` on a small diff, proof in this log.

## Decisions

- "Whole file" convention: `startLine = endLine = 0`, exposed by
  `ReviewComment.isFileLevel` (non-breaking addition to the §6 contract, H2 = me).
- The parser is tolerant (fences, unknown keys, severity as text) so that models
  without native JSON (Ollama) work without Koog's structured output.

## Journal

- 17:14 — worktree created.
- 18:20 — pipeline without Koog written; Koog research in progress (`researcher`).
- 18:50 — Koog 1.3.0 in place (`koog-agents` + `ktor-bom`, CIO in `jvmSharedMain`, the Google
  client on `1.3.0-beta`, the only version published): JVM compile and `assembleDebug` green
  on the first try; `check.sh` green (48 tests). `KoogLlmClient`: custom `LLModel` from
  `Settings.model`, Anthropic with `AnthropicClientSettings(modelVersionsMap)` because
  `AnthropicModels` 1.3.0 ignores `claude-opus-5-5`; Ollama on `localhost:11434`; provider
  errors rephrased without the request. `RealReviewCallTest` (jvmTest, opt-in on
  `APP_ANTHROPIC_API_KEY`) awaits a human run: proof to paste here.

## Uncertain

- `OllamaClient(baseUrl)` and `GoogleLLMClient(apiKey)` compile but have not been called.
- No ART test of the Koog call: a green `assembleDebug` does not prove the Android runtime
  (team lesson); Koog issue #2185 (`removeLast` < API 35) to watch on the emulator.
- #19 (`diffAgainst`) is still `TODO` on H1: the #22 loop waits on that diff.
- 17:45 — **Successful real call** from the desktop app (key in the settings, model
  `claude-opus-5-5`, PR #52): "2 nits on 2658 changed files", two WARNINGs anchored to the
  whole file (`foot.png.import:0`, `team2.png.import:0`), consistent summary. First attempt
  refused by Koog ("Model claude-opus-5-5 does not support tools"): fixed by declaring the
  model with the capabilities of `AnthropicModels.Opus_5`.
- Findings for later: the diff counts 2658 files because `diffAgainst` (#50) compares
  the base tree rather than the merge-base (#19); with `MAX_FILES = 20` taken in diff order,
  the review read two Godot sidecars instead of code. The UI shows `:0` for a whole-file nit
  (#22/#33: display "whole file").
