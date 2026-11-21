# Ticket 46-ktor-server

- Statut : prêt pour PR
- Propriétaire humain : @dovatgabriel
- Piste : D (partage des reviews)
- Agent : Claude Code (lead)
- Model : opus
- Escalations : aucune
- Fichiers : `server/**`, `settings.gradle.kts`, `gradle/libs.versions.toml`, `scripts/check.sh`,
  `shared/src/commonMain/.../domain/SharedReview.kt` (contrat §6)

## Journal

- 19:14 — worktree créé.
- 19:20 — contrat `SharedReview` ajouté à `domain`, tel que §6, avec `id = ""` par défaut.
- 19:30 — module `server/` : Ktor CIO, `POST /reviews`, `GET /reviews?repoUrl=`,
  `GET /reviews/{id}`, `GET /health` ; `ReviewStore` en mémoire + `~/.nitpick/server/reviews.json`.
- 19:35 — 6 tests `testApplication` verts ; `./gradlew :server:run` + curl vérifiés à la main.

## Décisions

- `id` a une valeur par défaut `""` : le client envoie un `SharedReview` sans id (§7.1) et le
  serveur l'attribue (UUID). Le contrat reste celui du §6 pour tout le reste.
- `ReviewShareClient` n'est pas ajouté ici : sa seule implémentation arrive avec #48.
- Même BOM Ktor (3.3.3) que le client Koog de `:shared` : une seule version de Ktor sur le
  classpath du serveur, qui dépend de `:shared`.
- Moteur CIO plutôt que Netty : pur Kotlin, déjà dans le cache, aucun natif.
- Limite de 1 Mo lue à la main (`readRemaining(max + 1)`) : elle tient aussi sans
  `Content-Length`. 413 au-delà, 400 sur JSON invalide, `repoUrl` vide ou id hors UUID.
- Écriture disque avant la mise à jour mémoire, par fichier temporaire + rename atomique :
  un échec d'écriture ne laisse ni review fantôme ni fichier à moitié écrit.
- Aucun log par requête (pas de `CallLogging`) ; `slf4j-nop` coupe l'avertissement SLF4J.

## Vérifications

- `:server:test` : 6 tests, 0 échec (aller-retour id/repoUrl, liste filtrée la plus récente
  d'abord, 404, 400 ×3, 413, persistance après redémarrage du store, health).
- `./gradlew :server:run` : URL localhost + IP LAN affichées ; POST → 201 + id, GET par id et
  par repoUrl identiques, id inconnu → 404, fichier écrit. Données de test supprimées ensuite.
- `./scripts/check.sh` (inclut désormais `:server:test`).

## Review ponytail (corrigée)

- `./gradlew build` était rouge : `:server:distZip` refusait deux jars lifecycle de même nom
  (androidx et JetBrains, tirés par `:shared`). `duplicatesStrategy = EXCLUDE` sur la
  distribution ; `distZip` et `installDist` passent (c'était aussi la cause de l'échec
  d'`installDist`).
- `repoUrl` normalisé à l'écriture et à la lecture : le remote SSH du laptop
  (`git@github.com:o/r.git`) et le clone HTTPS du téléphone donnent la même clé
  `https://github.com/o/r`. Sinon la liste Android revenait vide au point 5 de la démo.
- Test de la limite de 1 Mo sans `Content-Length` (corps en flux) ; vérifié qu'il échoue si
  le contrôle de taille du corps est retiré. 9 tests, 0 échec.

## Incertain

- slf4j-nop 2.0.16 choisi connu, pas vérifié comme le dernier 2.0.x.

## Suivant

- #48 : `ReviewShareClient` (Ktor client), bouton « Share review » desktop, écran « Shared
  reviews » Android.
