# Ticket 170-share-proof

- Statut : prêt pour PR
- Propriétaire humain : @pepito2t (H3)
- Piste : D partage
- Agent : Claude Code, session humaine
- Model : Fable 5.1
- Escalations : aucune
- Fichiers autorisés : docs/demo/share-*.png, docs/demo/share-proof.md, docs/DEMO.md

## Journal

- 02:00 — serveur `:server:run` sur localhost:8080, `adb reverse tcp:8080 tcp:8080` sur l'émulateur `nitpick-qa` (API 36). Dépôt de test servi en `git://10.0.2.2/remote.git` ; le clone desktop et le clone téléphone ont la même URL de remote, donc la même clé `repoUrl` côté serveur.
- 02:05 — desktop (`main` 3b14570, build dev-run) : review Anthropic de `feature/search-pages-import` vs `main`, 2 nits (lastPage, toInt) ; **Share review** → badge « Shared », `GET /reviews?repoUrl=…` liste la review `05bc388` avec 2 nits.
- 02:06 — téléphone : Shared reviews → Refresh → ligne « feature/search-pages-import · 05bc388 · 2 nits » → ouverture avec le badge « Shared review » (pas d'appel au modèle) → nit `src/Search.kt:17` → Apply suggestion → « Applied », « 1 suggestion applied », `require(size > 0)` présent dans `files/repos/remote/src/Search.kt` sur l'appareil.
- Captures : `docs/demo/share-desktop-shared.png`, `share-android-opened.png`, `share-android-applied.png`.

## Incertain

- Le téléphone a cloné le même remote que le desktop ; avec deux URL différentes (SSH vs HTTPS) c'est la normalisation du serveur qui fait le lien, non rejouée ici.
