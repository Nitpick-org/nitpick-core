#!/usr/bin/env bash
# PreToolUse guard for Bash: blocks .env access and writes to protected docs.
# Reads the hook JSON on stdin.
set -euo pipefail

INPUT="$(cat)"
CMD="$(jq -r '.tool_input.command // empty' <<<"$INPUT" 2>/dev/null || true)"
[ -n "$CMD" ] || exit 0
[ "${HACKATHON_ALLOW_PROTECTED:-0}" = "1" ] && exit 0

# .env access is forbidden; .env.example is allowed.
CLEAN="$(printf '%s' "$CMD" | sed 's/\.env\.example//g')"
if printf '%s' "$CLEAN" | grep -q '\.env'; then
  echo "⛔ Commande bloquée : accès à .env interdit (seul .env.example est autorisé)." >&2
  exit 2
fi

# Protected docs: block write-like commands, allow reads (grep, cat, diff…).
case "$CMD" in
  *">"*|*"sed -i"*|*"tee "*|*"rm "*|*"mv "*|*"cp "*|*"git apply"*|*"python"*|*"node -e"*|*"perl -i"*)
    for file in docs/PRD.md docs/DECISIONS.md docs/STRATEGY.md docs/AGENT-OPS.md \
      docs/ULTRA.md docs/MODEL-ROUTING.md docs/CONTEXT.md docs/REVIEW_LOG.md \
      AGENTS.md CLAUDE.md progress.txt; do
      case "$CMD" in
        *"$file"*)
          echo "⛔ Commande bloquée : $file est protégé." >&2
          echo "   Écris une proposition dans progress/<ticket>.md ou docs/QUESTIONS.md." >&2
          exit 2
          ;;
      esac
    done
    ;;
esac

exit 0
