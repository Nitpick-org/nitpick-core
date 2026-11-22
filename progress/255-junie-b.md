# Ticket 255-junie-b

- Statut : terminé
- Propriétaire humain : H3
- Piste : A core
- Agent : Junie Lite via Junie CLI 26.11.2 (`--model=junie-lite`, served as Qwen/Qwen3.6-27B-FP8), then Claude Fable 5.1 for the review
- Escalations : none
- Fichiers autorisés : shared/src/commonTest/kotlin/dev/nitpick/app/settings/LlmModelsTest.kt, progress/255-junie-b.md, docs/demo/junie-255-transcript.txt, README.md, docs/DEMO.md

## Journal

- 11:45 — worktree created; issue #255 opened (candidate B of `docs/demo/junie-lite-brief.md`, refs #227).
- 11:46 — Junie Lite run non-interactively with the brief's prompt (transcript: `docs/demo/junie-255-transcript.txt`). It added `LlmModelsTest.kt` (5 tests) and a `resolveModel()` extension in `LlmModels.kt`; its own run of `:shared:jvmTest --tests 'dev.nitpick.app.settings.*'` reported 29 tests, 0 failures.
- 11:48 — Review (H3 + Claude): `resolveModel()` rejected, it is production code with no caller after the feature freeze, and the brief said "test them as they are". The 4 tests on it dropped. Kept: every provider has a known model; added: `Settings()` defaults point at a known model of their provider.
- 11:52 — README "How we build it" bullet and DEMO Q&A answer 2 pasted (brief step "Then paste these"), adapted to the CLI since Air is not on the laptop.

## Decisions

- Junie CLI instead of Air: no Air install, 15 min before submission. The CLI runs the same Junie Lite tier; the transcript replaces the three PNG captures.
- No production change from this ticket.

## Uncertain

- The REVIEW_LOG / AGENT_LOG lines are human-only files: proposed in the PR body.
