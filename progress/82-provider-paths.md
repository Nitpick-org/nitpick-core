# Ticket 82-provider-paths

- Status: PR open (OpenAI path replayed through LM Studio)
- Human owner: @dovatgabriel
- Track: A core
- Agent: lead (Claude Code), Opus 5.5; real replay: Claude Code (Fable 5.1), session hackathon-66
- Files: `jvmTest/.../review/RealReviewCallTest.kt`,
  `commonTest/.../review/KoogLlmClientTest.kt`, `commonMain/.../review/KoogLlmClient.kt`,
  `docs/RESEARCH.md`, `docs/IDEAS.md`

## Log

- 01:05 — `RealReviewCallTest` moves from Anthropic alone to one test per provider (Anthropic,
  OpenAI, Google, Ollama), same buggy diff, same assertion: at least one anchored nit.
- 01:08 — `refusesToCallAProviderWithoutAKey` covers Anthropic, OpenAI and Google.
- 01:10 — environment variables documented in `docs/RESEARCH.md` (#82).
- 03:20 — no OpenAI or Google key on the machine; LM Studio 0.4.28 (OpenAI-compatible server,
  port 1234) stands in as a real provider. `KoogLlmClient` takes an optional `openAiBaseUrl` (test
  hook, default `api.openai.com`); `RealReviewCallTest` reads it from `APP_OPENAI_BASE_URL`.
- 03:25 — real OpenAI review replayed against LM Studio (`qwen/qwen3-vl-8b`): 1 BUG nit anchored on
  `Last.kt:3`, 1 tool used, summary correct. Koog's OpenAI path (Responses API + tools) proven.
- 03:27 — LM Studio does **not** serve the Ollama API (`/api/tags`, `/api/chat` → "Unexpected endpoint",
  even with `--port 11434`): the Ollama path stays unreplayed here.

## Decisions

- The OpenAI base URL is a constructor parameter, not a `Settings` field: an "OpenAI-compatible
  endpoint" field in the UI is a product decision (`docs/IDEAS.md`).

- Skip = JUnit `assumeTrue`: the report counts `skipped`, not a fake green test.
- Ollama is opt-in through `APP_OLLAMA_MODEL`: a local server started for something else must
  not turn check.sh into a real slow review.
- No new fallback code: `KoogLlmClient` already has one (tools → without tools).

## Checks

- Targeted `:shared:jvmTest`: KoogLlmClientTest 2 tests, 0 failure; RealReviewCallTest 4 tests,
  **4 skipped** in the JUnit report (no key and no `APP_OLLAMA_MODEL` on this machine).
- 22/11 03:25, `APP_OPENAI_API_KEY=lm-studio APP_OPENAI_MODEL=qwen/qwen3-vl-8b
  APP_OPENAI_BASE_URL=http://localhost:1234`: RealReviewCallTest tests=4 skipped=3 failures=0;
  `openAi` **ran** (1m04) and passes. Without variables: tests=4 skipped=4; KoogLlmClientTest 3 tests, 0 failures.

## Uncertain

- **Real OpenAI (api.openai.com, `gpt-5.5`) and Google**: still not replayed, no key in the team.
  The OpenAI path is proven against a compatible server, not against the real auth or `gpt-5.x` ids.
- **Ollama**: not replayed (no Ollama installed, LM Studio does not emulate its API). Small models
  (`llama3.2`) may not anchor a nit.
- Koog #2308 could break the no-tools fallback on Google.

## Next

- Someone with an OpenAI or Google key runs the command in `docs/RESEARCH.md` (#82) and pastes the
  result here; `brew install ollama && ollama pull qwen2.5-coder:7b` for the Ollama path.
