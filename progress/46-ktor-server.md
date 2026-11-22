# Ticket 46-ktor-server

- Status: ready for PR
- Human owner: @dovatgabriel
- Track: D (review sharing)
- Agent: Claude Code (lead)
- Model: opus
- Escalations: none
- Files: `server/**`, `settings.gradle.kts`, `gradle/libs.versions.toml`, `scripts/check.sh`,
  `shared/src/commonMain/.../domain/SharedReview.kt` (§6 contract)

## Log

- 19:14 — worktree created.
- 19:20 — `SharedReview` contract added to `domain`, as per §6, with `id = ""` by default.
- 19:30 — `server/` module: Ktor CIO, `POST /reviews`, `GET /reviews?repoUrl=`,
  `GET /reviews/{id}`, `GET /health`; in-memory `ReviewStore` + `~/.nitpick/server/reviews.json`.
- 19:35 — 6 green `testApplication` tests; `./gradlew :server:run` + curl verified by hand.

## Decisions

- `id` has a default value `""`: the client sends a `SharedReview` without an id (§7.1) and the
  server assigns it (UUID). The contract stays §6's for everything else.
- `ReviewShareClient` is not added here: its only implementation arrives with #48.
- Same Ktor BOM (3.3.3) as `:shared`'s Koog client: a single Ktor version on the
  server classpath, which depends on `:shared`.
- CIO engine rather than Netty: pure Kotlin, already in the cache, no native code.
- 1 MB limit read by hand (`readRemaining(max + 1)`): it also holds without
  `Content-Length`. 413 beyond it, 400 on invalid JSON, empty `repoUrl` or non-UUID id.
- Disk write before the in-memory update, via a temporary file + atomic rename:
  a write failure leaves neither a phantom review nor a half-written file.
- No per-request logging (no `CallLogging`); `slf4j-nop` silences the SLF4J warning.

## Verification

- `:server:test`: 6 tests, 0 failure (id/repoUrl round trip, filtered list newest
  first, 404, 400 ×3, 413, persistence after store restart, health).
- `./gradlew :server:run`: localhost URL + LAN IP displayed; POST → 201 + id, GET by id and
  by repoUrl identical, unknown id → 404, file written. Test data deleted afterwards.
- `./scripts/check.sh` (now includes `:server:test`).

## Ponytail review (fixed)

- `./gradlew build` was red: `:server:distZip` rejected two lifecycle jars with the same name
  (androidx and JetBrains, pulled by `:shared`). `duplicatesStrategy = EXCLUDE` on the
  distribution; `distZip` and `installDist` pass (it was also the cause of the
  `installDist` failure).
- `repoUrl` normalized on write and on read: the laptop's SSH remote
  (`git@github.com:o/r.git`) and the phone's HTTPS clone give the same key
  `https://github.com/o/r`. Otherwise the Android list came back empty at step 5 of the demo.
- Test of the 1 MB limit without `Content-Length` (streamed body); verified that it fails if
  the body size check is removed. 9 tests, 0 failure.

## Uncertain

- slf4j-nop 2.0.16 chosen as known, not verified as the latest 2.0.x.

## Next

- #48: `ReviewShareClient` (Ktor client), desktop "Share review" button, Android "Shared
  reviews" screen.
