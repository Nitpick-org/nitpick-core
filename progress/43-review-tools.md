# Ticket 43-review-tools

- Status: done — #77 merged
- Human owner: @zooldyyk
- Track: A shared core
- Agent: lead
- Model: main session
- Escalations: none
- Files: `shared/src/commonMain/.../review/{ReviewToolbox,ToolCallingLlmClient,KoogLlmClient,LlmReviewAgent,ReviewPrompt}.kt`,
  `commonTest/.../review/ReviewToolboxTest.kt`

## Log

- 19:25 — `ReviewToolbox`: `list_files` + `read_file`, relative paths only
  (absolute, `..`, `.git` refused), cap of 6 calls and 64 KB read per review.
- 19:30 — `ToolCallingLlmClient`; `KoogLlmClient` runs a native Koog loop
  (`ToolDescriptor` + `MessagePart.Tool.Call` + `PromptBuilder.toolResult`), 4 rounds max.
  If the first call with tools fails, fall back to the prompt without tools.
- 19:35 — `LlmReviewAgent` passes the toolbox only if the client supports tools;
  the prompt adds the Tools section in that case. Tests: 6 guard-rail cases.
- 19:38 — **118 JVM tests, 0 failures**; desktop compiles.
- 22:05 — real proof through the app settings (key never read by the agent):
  `read_file Pricing.kt` triggered, then the review answered. Four loop bugs
  revealed and fixed: Anthropic `Reasoning` re-injected, argument order of
  `toolResult`, tool ids not normalised, 8k output budget entirely consumed by
  adaptive reasoning. Final fix: 32k + `output_config.effort=low`.
- 22:29 — #77 merged (`c4a7168`), app validated by H2. The proof `println` is removed
  from `AppContainer`; #80 will carry the display of the tools in the UI.
- 19:55 — self-review: `read_file` still read ignored/binary files and
  `.env`; only tracked and reviewable files pass now, `.git` is
  case-insensitive, listings count in the budget. Tests `.env`, `.png`, NUL,
  `.GIT` and tool-capable path added. **119 tests, 0 failures**.

## Decisions

- The tools stay **read-only** and go through `GitRepository`; no direct filesystem
  access.
- The tool-free mode stays the default: a client that is not a `ToolCallingLlmClient` does not see the
  Tools section and never calls `completeWithTools`.
- 6 calls / 64 KB, 4 rounds: enough for a signature or a neighbouring test, without blowing up
  the cost or the review time.
- The cap is in bytes (`encodeToByteArray`), not in characters.

## Verifications

- `:shared:jvmTest --rerun-tasks`: **119 tests, 0 failures**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` green.
- Full `./scripts/check.sh` not run (no Android SDK here).

## Uncertain

- **Real proof**: `TOOL_USE read_file Pricing.kt` + JSON obtained on 21/11 at 22:05,
  confirmed by H2 on 21/11 at 22:28; comment added to #43.
- The Koog loop is covered only by the `ToolCallingLlmClient`/toolbox path and the real
  proof; #80 will add the UI display.
- Tool-calling support varies by provider; the tool-free fallback covers the case.
