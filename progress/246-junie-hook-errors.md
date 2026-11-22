# Ticket 246-junie-hook-errors

- Status: done
- Human owner: @H2
- Track: A core (review)
- Agent: Claude Code (Fable 5.1, interactive session)
- Allowed files: `shared/src/jvmMain/kotlin/dev/nitpick/app/review/JunieCliLlmClient.kt`,
  `shared/src/jvmTest/kotlin/dev/nitpick/app/review/JunieCliLlmClientTest.kt`

## Done

- `parseJunieOutput` returns `result` whenever it is present; `errors` is fatal only without an
  answer. Test with an envelope holding both.

## Decisions

- Found in `~/.junie/sessions/session-261122-110956-s4bd`: the review from the app was complete
  (nits in our schema), the session only failed on a user hook at the end (a Claude Code plugin
  hook Junie picks up from H2's config). Treating that as a provider failure hid a good review.

## Uncertain

- `--mcp-default-locations false` does not stop Junie from loading `~/.junie/mcp/mcp.json`: the
  user's MCP servers are still contacted at start (5 s, harmless). Not investigated further.

## Journal

- 22/11 — bug seen by H2 from the packaged app; fixed, test ran.
