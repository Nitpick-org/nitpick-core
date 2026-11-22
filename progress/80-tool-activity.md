# Ticket 80-tool-activity

- Status: ready for PR
- Human owner: @dovatgabriel (H1); H2 implementation
- Track: A + B
- Agent: lead
- Files: `domain/{Review,ReviewToolUse}.kt`, `review/{LlmReviewAgent,ReviewToolbox}.kt`,
  `ui/repository/ReviewPanel.kt`, `commonTest/.../LlmReviewAgentTest.kt`, strings

## Log

- 23:25 — `ReviewToolUse(tool, path)` moves into `ReviewResult` (empty default, compatible
  with existing shared reviews). `LlmReviewAgent` collects the calls of its toolbox.
- 23:28 — Review summary card: "Tools used: Pricing.kt" line (distinct paths, with no
  content and no key), visible only if tools were used.
- 23:30 — test: a tool-capable client that reads `Main.kt` produces
  `ReviewToolUse("read_file","Main.kt")` in the result.

## Decisions

- `toolUses` is a field of the result, not a separate UI state: a shared review keeps
  the trace of its tools.
- No grammatical counter for now: we list the paths, which avoids plural
  bug #90.

## Checks

- `:shared:jvmTest --rerun-tasks`: **137 tests, 0 failure**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` green.
- Android not replayed (SDK missing).

## Uncertain

- The line can be long on a small screen; limited to 2 lines and to distinct
  paths.
- `ReviewResult` gains a serialized field: the old JSON stays readable thanks to the
  default, to be checked on the sharing server side (#46/#48).
