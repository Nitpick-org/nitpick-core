# Ticket 20-koog-agent

- Statut : en cours
- Propriétaire humain : @zooldyyk (H2)
- Piste : A core partagé
- Agent : Claude Fable 5.1 (session interactive) + `researcher` (Sonnet) pour la doc Koog
- Model : fable
- Escalations : aucune
- Fichiers autorisés : shared/src/commonMain/kotlin/dev/nitpick/app/review/**,
  shared/src/commonTest/kotlin/dev/nitpick/app/review/**,
  shared/src/commonTest/kotlin/dev/nitpick/app/git/FakeGitRepository.kt,
  shared/src/commonMain/kotlin/dev/nitpick/app/domain/Review.kt (propriété `isFileLevel` seulement),
  shared/src/commonMain/kotlin/dev/nitpick/app/AppContainer.kt (câblage),
  gradle/libs.versions.toml, shared/build.gradle.kts, androidApp/build.gradle.kts (packaging),
  docs/RESEARCH.md (section Koog, par le researcher)

## Plan

1. Pipeline sans Koog, testable avec un `LlmClient` fake : `buildReviewContext` (diff filtré,
   fichiers complets, README, 10 commits, langage), `buildReviewPrompt` (lignes numérotées
   `   42 | +code`), `parseReviewResponse` (JSON tolérant), `anchorComment` (vérif, recalage,
   repli fichier entier), `LlmReviewAgent` qui compose le tout.
2. `KoogLlmClient` : le seul fichier qui importe Koog, construit depuis `Settings`
   (fournisseur, modèle, clé) ; API vérifiée dans `docs/RESEARCH.md` avant d'écrire.
3. Tests `commonTest` : prompt numéroté, parsing valide, snippet décalé recalé, snippet
   introuvable → fichier entier, filtre des lockfiles.
4. Build Android dès que la dépendance est posée (risque dex / META-INF).
5. Appel réel avec `APP_ANTHROPIC_API_KEY` sur un petit diff, preuve dans ce journal.

## Décisions

- Convention « fichier entier » : `startLine = endLine = 0`, exposée par
  `ReviewComment.isFileLevel` (ajout non cassant au contrat du §6, H2 = moi).
- Le parseur est tolérant (fences, clés inconnues, sévérité en texte) pour que les modèles
  sans JSON natif (Ollama) marchent sans la sortie structurée de Koog.

## Journal

- 17:14 — worktree créé.
- 18:20 — pipeline sans Koog écrit ; recherche Koog en cours (`researcher`).
- 18:50 — Koog 1.3.0 posé (`koog-agents` + `ktor-bom`, CIO en `jvmSharedMain`, client Google en
  `1.3.0-beta` seul publié) : compile JVM et `assembleDebug` verts du premier coup ; `check.sh`
  vert (48 tests). `KoogLlmClient` : `LLModel` custom depuis `Settings.model`, Anthropic avec
  `AnthropicClientSettings(modelVersionsMap)` car `AnthropicModels` 1.3.0 ignore
  `claude-opus-5-5` ; Ollama sur `localhost:11434` ; erreurs fournisseur reformulées sans la
  requête. `RealReviewCallTest` (jvmTest, opt-in sur `APP_ANTHROPIC_API_KEY`) attend son
  exécution par un humain : preuve à coller ici.

## Incertain

- `OllamaClient(baseUrl)` et `GoogleLLMClient(apiKey)` compilent mais n'ont pas été appelés.
- Pas de test sur ART de l'appel Koog : `assembleDebug` vert ne prouve pas le runtime Android
  (leçon de l'équipe) ; issue Koog #2185 (`removeLast` < API 35) à surveiller sur l'émulateur.
- #19 (`diffAgainst`) est encore `TODO` chez H1 : la boucle #22 attend ce diff.
- 17:45 — **Appel réel réussi** depuis l'app desktop (clé dans les réglages, modèle
  `claude-opus-5-5`, PR #52) : « 2 nits on 2658 changed files », deux WARNING ancrés au
  fichier entier (`foot.png.import:0`, `team2.png.import:0`), résumé cohérent. Premier essai
  refusé par Koog (« Model claude-opus-5-5 does not support tools ») : corrigé en déclarant le
  modèle avec les capacités de `AnthropicModels.Opus_5`.
- Constats pour la suite : le diff compte 2658 fichiers parce que `diffAgainst` (#50) compare
  l'arbre de la base et non le merge-base (#19) ; avec `MAX_FILES = 20` pris dans l'ordre du
  diff, la review a lu deux sidecars Godot au lieu du code. L'UI affiche `:0` pour un nit de
  fichier entier (#22/#33 : afficher « whole file »).
