# Ticket 88-koog-android-factory

- Status: ready for PR
- Human owner: @pepito2t (H3); H2 implementation (resumed)
- Track: A Android
- Agent: lead
- Files: `review/KoogLlmClient.kt`

## Log

- 23:55 — Koog's JVM publishes `META-INF/services/ai.koog.http.client.KoogHttpClient$Factory`,
  the Android AAR does not. `llmClient()` now passes an explicit
  `KtorKoogHttpClient.Factory()` factory to the four constructors (Anthropic/OpenAI/Google/Ollama).

## Checks

- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` green.
- No Android emulator/SDK here: the real Android review remains to be replayed.

## Uncertain

- The default Ktor factory must find an engine in the Android AAR; it is the same
  path as the fix proposed in #88.

## Proof on ART (H3, 21/11 23:44)

- `KoogClientOnDeviceTest` (`androidApp/src/androidTest`): `KoogLlmClient.complete` with a dummy key on the API 36 emulator fails on the provider side, no longer on "No KoogHttpClient.Factory provider found". `connectedDebugAndroidTest`: 4 tests, 0 failure.
- Cause verified in the Gradle cache: `http-client-ktor-jvm-1.3.0.jar` contains `META-INF/services/ai.koog.http.client.KoogHttpClient$Factory`, `http-client-ktor-release.aar` (Android) does not.
- Real Anthropic review on the emulator: not replayed, the emulator was taken by session #64.
