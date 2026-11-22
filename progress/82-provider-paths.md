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

- Skip = JUnit `assumeTrue` : le rapport compte `skipped`, pas un faux test vert.
- Ollama est opt-in par `APP_OLLAMA_MODEL` : un serveur local lancé pour autre chose ne
  doit pas transformer check.sh en vraie review lente.
- Pas de nouveau code de repli : `KoogLlmClient` en a déjà un (outils → sans outils).

## Vérifications

- `:shared:jvmTest` ciblé : KoogLlmClientTest 2 tests, 0 échec ; RealReviewCallTest 4 tests,
  **4 skipped** dans le rapport JUnit (aucune clé ni `APP_OLLAMA_MODEL` sur cette machine).

## Incertain

- **Aucun appel réel rejoué ici** : OpenAI, Google et Ollama restent à lancer avec une clé
  ou un serveur. Les petits modèles Ollama (`llama3.2`) peuvent ne pas ancrer de nit.
- Koog #2308 pourrait casser le repli sans outils chez Google.

## Suivant

- Quelqu'un avec les clés lance la commande de `docs/RESEARCH.md` (#82) et colle le
  résultat ici.
