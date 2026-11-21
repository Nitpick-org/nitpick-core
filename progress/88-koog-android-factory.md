# Ticket 88-koog-android-factory

- Statut : prêt pour PR
- Propriétaire humain : @pepito2t (H3) ; implémentation H2 (reprise)
- Piste : A Android
- Agent : lead
- Fichiers : `review/KoogLlmClient.kt`

## Journal

- 23:55 — le JVM de Koog publie `META-INF/services/ai.koog.http.client.KoogHttpClient$Factory`,
  pas l’AAR Android. `llmClient()` passe maintenant une factory explicite
  `KtorKoogHttpClient.Factory()` aux quatre constructeurs (Anthropic/OpenAI/Google/Ollama).

## Vérifications

- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- Pas d’émulateur/SDK Android ici : la vraie review Android reste à rejouer.

## Incertain

- La factory Ktor par défaut doit trouver un engine dans l’AAR Android ; c’est le même
  chemin que la correction proposée dans #88.

## Preuve sur ART (H3, 21/11 23:44)

- `KoogClientOnDeviceTest` (`androidApp/src/androidTest`) : `KoogLlmClient.complete` avec une clé bidon sur l'émulateur API 36 échoue côté provider, plus sur « No KoogHttpClient.Factory provider found ». `connectedDebugAndroidTest` : 4 tests, 0 échec.
- Cause vérifiée dans le cache Gradle : `http-client-ktor-jvm-1.3.0.jar` contient `META-INF/services/ai.koog.http.client.KoogHttpClient$Factory`, `http-client-ktor-release.aar` (Android) non.
- Review Anthropic réelle sur l'émulateur : non rejouée, l'émulateur était pris par la session #64.
