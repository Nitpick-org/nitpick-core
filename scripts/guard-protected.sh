#!/usr/bin/env bash
# PreToolUse guard: subagents may not edit the files that define the project.
# Humans (main session) can still edit them; an explicit env var can override.
#
# Reads the hook JSON on stdin.
set -euo pipefail

INPUT="$(cat)"
FILE="$(jq -r '.tool_input.file_path // .tool_input.notebook_path // empty' <<<"$INPUT" 2>/dev/null || true)"
AGENT="$(jq -r '.agent_type // empty' <<<"$INPUT" 2>/dev/null || true)"
HEADLESS="${HACKATHON_HEADLESS:-0}"

[ -n "$FILE" ] || exit 0
# A subagent, or a headless `claude -p` session (AFK/Ralph), has no human in the loop.
if [ -z "$AGENT" ] && [ "$HEADLESS" != "1" ]; then
  exit 0
fi
[ "${HACKATHON_ALLOW_PROTECTED:-0}" = "1" ] && exit 0

case "$FILE" in
  */docs/PRD.md|docs/PRD.md|*/docs/DECISIONS.md|docs/DECISIONS.md|*/docs/STRATEGY.md|docs/STRATEGY.md|*/docs/AGENT-OPS.md|docs/AGENT-OPS.md|*/docs/ULTRA.md|docs/ULTRA.md|*/docs/MODEL-ROUTING.md|docs/MODEL-ROUTING.md|*/CLAUDE.md|CLAUDE.md|*/progress.txt|progress.txt|*/.env|.env|*/.env.*|.env.*)
    echo "⛔ Un agent ne peut pas éditer $FILE." >&2
    echo "   Écris une proposition dans progress/<ticket>.md ou docs/QUESTIONS.md ;" >&2
    echo "   un humain tranche. Override explicite : HACKATHON_ALLOW_PROTECTED=1." >&2
    exit 2
    ;;
esac

exit 0
