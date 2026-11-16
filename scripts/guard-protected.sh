#!/usr/bin/env bash
# PreToolUse guard: subagents may not edit the files that define the project.
# Humans (main session) can still edit them; an explicit env var can override.
#
# Reads the hook JSON on stdin.
set -euo pipefail

INPUT="$(cat)"
FILE="$(jq -r '.tool_input.file_path // .tool_input.notebook_path // empty' <<<"$INPUT" 2>/dev/null || true)"
AGENT="$(jq -r '.agent_type // empty' <<<"$INPUT" 2>/dev/null || true)"

[ -n "$FILE" ] || exit 0
# No agent_type means the main conversation: a human is in the loop.
[ -n "$AGENT" ] || exit 0
[ "${HACKATHON_ALLOW_PROTECTED:-0}" = "1" ] && exit 0

case "$FILE" in
  */docs/PRD.md|*/docs/DECISIONS.md|*/CLAUDE.md|*/.env|*/.env.*)
    echo "⛔ Un sous-agent ne peut pas éditer $FILE." >&2
    echo "   Écris une proposition dans progress/<ticket>.md ou docs/QUESTIONS.md ;" >&2
    echo "   un humain tranche. Override explicite : HACKATHON_ALLOW_PROTECTED=1." >&2
    exit 2
    ;;
esac

exit 0
