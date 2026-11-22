# Ticket 17-diff-parser

- Status: finished, to push and merge
- Human owner: @thomas
- Track: A core
- Agent: Claude Fable 5.1 (interactive session)
- Model: fable
- Escalations: none
- Allowed files: shared/src/commonMain/kotlin/dev/nitpick/app/diff/**, shared/src/commonTest/kotlin/dev/nitpick/app/diff/**

## Journal

- 14:40 — branch `ticket/17-diff-parser`, ticket #17 assigned. TDD: git diff fixtures
  (modified 2 hunks, added, deleted, renamed, binary, no final newline, malformed,
  multi-file) then the parser.
- 15:00 — `parseUnifiedDiff(text): List<FileDiff>` in `commonMain/diff`, 8 tests green.
- Decisions:
  - Pure top-level function, no class: nothing to inject.
  - Paths come from the `---`/`+++` lines (fallback to the `diff --git` header);
    `/dev/null` gives a missing path, so a deleted file keeps its former path.
  - `oldPath` is only filled for a rename.
  - End of hunk by counting the announced lines (`@@ -a,b +c,d @@`), with `b`/`d` at 1 when
    omitted; a line without a prefix stops the hunk and the parser returns what it has understood.
  - `\ No newline at end of file` ignored, line numbers unchanged.
  - Text without a `diff --git` header → empty list (JGit always emits this header).
- Files: shared/src/commonMain/kotlin/dev/nitpick/app/diff/UnifiedDiffParser.kt,
  shared/src/commonTest/kotlin/dev/nitpick/app/diff/UnifiedDiffParserTest.kt.
- Uncertain: paths containing ` b/` or quotes (git quotes them) badly split in
  the header; the `---`/`+++` lines cover the common case. Marked `shortcut:` in the code.
- Blocker: none.
- Next: #19 (JGit `diffAgainst` wired to this parser) and #20 (Koog agent) are unblocked.
- 15:30 — ponytail review: fixture added for the JGit label `Binary files differ` without `---`/`+++` lines (9 tests).
