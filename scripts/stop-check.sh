#!/usr/bin/env bash
# Stop hook: do not let an agent declare "done" while check.sh is red.
# Skips when there is no stack yet, when the tree is clean, or when the
# stop was already triggered by this hook (prevents an infinite loop).
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"

INPUT="$(cat 2>/dev/null || true)"
ACTIVE="$(jq -r '.stop_hook_active // false' <<<"$INPUT" 2>/dev/null || echo false)"
[ "$ACTIVE" = "true" ] && exit 0

[ -f package.json ] || exit 0
[ -n "$(git status --porcelain 2>/dev/null || true)" ] || exit 0

mkdir -p .ralph
LOG=".ralph/stop-check.log"
if ./scripts/check.sh >"$LOG" 2>&1; then
  exit 0
fi

echo "⛔ check.sh est rouge : le travail n'est pas fini." >&2
tail -40 "$LOG" >&2
echo "Corrige les erreurs (ou dis explicitement que tu es bloqué) avant de t'arrêter." >&2
exit 2
