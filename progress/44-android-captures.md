# Ticket 44 (part) — Android dogfooding captures

- Statut : fait (captures), le reste de #44 reste à H1/Gabriel
- Propriétaire humain : @pepito2t (H3) pour cette partie
- Piste : C android
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Fichiers autorisés : `docs/demo/android-*.png`, `docs/DEMO.md` (ligne captures + checklist Android)

## Fait

- Parcours Android complet sur l'émulateur, piloté par adb, compte GitHub connecté par le device
  flow (#64) : clone de `pepito2t/OpenTab` → checkout `perf/thumbnail-lookup` (branche de démo
  poussée exprès : lock non relâché sur cache miss, `>=` d'éviction) → review (les 2 bugs
  trouvés, suggestions correctes) → Apply ×2 → Stage & commit → Push. Commit `cb55041` sur
  GitHub, diff vérifié : `defer` rétabli, `>` rétabli.
- 8 captures à 50 % dans `docs/demo/android-*.png`, référencées dans `docs/DEMO.md`.

## Observé pendant le dogfooding (idées, pas corrigé)

- Après le push, l'écran Review affiche « Run a review to see nits » sans dire « pushed » ; la
  seule confirmation est ↑0 sur la carte de branche du Graph.
- Les commentaires ajoutés par la branche fautive (« avoids the defer closure », « Evicts
  before the cache reaches capacity ») restent faux après l'Apply : la suggestion ne touche
  que le code.
- Le message de commit par défaut « Apply review suggestions » est resté tel quel.

## Journal

- 00:40 — émulateur, clé LLM copiée des réglages desktop, branche de démo poussée.
- 00:46 — boucle complète, push vérifié sur GitHub.
