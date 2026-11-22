# Ticket 82-provider-paths

- Statut : prêt pour PR
- Propriétaire humain : @dovatgabriel
- Piste : A core
- Agent : lead (Claude Code), Opus 5.5
- Fichiers : `jvmTest/.../review/RealReviewCallTest.kt`,
  `commonTest/.../review/KoogLlmClientTest.kt`, `docs/RESEARCH.md`

## Journal

- 01:05 — `RealReviewCallTest` passe d'Anthropic seul à un test par provider (Anthropic,
  OpenAI, Google, Ollama), même diff bogué, même assertion : au moins un nit ancré.
- 01:08 — `refusesToCallAProviderWithoutAKey` couvre Anthropic, OpenAI et Google.
- 01:10 — variables d'environnement documentées dans `docs/RESEARCH.md` (#82).

## Décisions

- Skip = `println` + retour, comme le test Anthropic existant : pas de JUnit `Assume`
  (le test vit dans `jvmTest` avec `kotlin.test`).
- Ollama est détecté par une connexion TCP de 500 ms sur `localhost:11434`.
- Pas de nouveau code de repli : `KoogLlmClient` en a déjà un (outils → sans outils).

## Vérifications

- `:shared:jvmTest` ciblé : KoogLlmClientTest 2 tests, RealReviewCallTest 4 tests, 0 échec ;
  les 4 providers affichent `skipped` (aucune clé, pas d'Ollama sur cette machine).

## Incertain

- **Aucun appel réel rejoué ici** : OpenAI, Google et Ollama restent à lancer avec une clé
  ou un serveur. Les petits modèles Ollama (`llama3.2`) peuvent ne pas ancrer de nit.
- Koog #2308 pourrait casser le repli sans outils chez Google.

## Suivant

- Quelqu'un avec les clés lance la commande de `docs/RESEARCH.md` (#82) et colle le
  résultat ici.
