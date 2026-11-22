# Ticket 82-provider-paths

- Status: ready for PR
- Human owner: @dovatgabriel
- Track: A core
- Agent: lead (Claude Code), Opus 5.5
- Files: `jvmTest/.../review/RealReviewCallTest.kt`,
  `commonTest/.../review/KoogLlmClientTest.kt`, `docs/RESEARCH.md`

## Log

- 01:05 — `RealReviewCallTest` moves from Anthropic alone to one test per provider (Anthropic,
  OpenAI, Google, Ollama), same buggy diff, same assertion: at least one anchored nit.
- 01:08 — `refusesToCallAProviderWithoutAKey` covers Anthropic, OpenAI and Google.
- 01:10 — environment variables documented in `docs/RESEARCH.md` (#82).

## Decisions

- Skip = JUnit `assumeTrue`: the report counts `skipped`, not a fake green test.
- Ollama is opt-in through `APP_OLLAMA_MODEL`: a local server started for something else must
  not turn check.sh into a real slow review.
- No new fallback code: `KoogLlmClient` already has one (tools → without tools).

## Checks

- Targeted `:shared:jvmTest`: KoogLlmClientTest 2 tests, 0 failure; RealReviewCallTest 4 tests,
  **4 skipped** in the JUnit report (no key and no `APP_OLLAMA_MODEL` on this machine).

## Uncertain

- **No real call replayed here**: OpenAI, Google and Ollama remain to be run with a key
  or a server. Small Ollama models (`llama3.2`) may not anchor a nit.
- Koog #2308 could break the no-tools fallback on Google.

## Next

- Someone with the keys runs the command in `docs/RESEARCH.md` (#82) and pastes the
  result here.
