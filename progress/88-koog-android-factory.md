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
