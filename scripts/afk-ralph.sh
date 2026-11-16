#!/usr/bin/env bash
# Bounded autonomous loop (AFK), e.g. while the team sleeps.
# Only run it in a dedicated ticket worktree, never on main.
#
# Usage:
#   AFK_OK=1 ./scripts/afk-ralph.sh 6
#
# Recommended: 1-6 iterations, one ticket zone, low-risk tasks only.
# Ultra: run it on an Ultra account before that person's sleep shift (docs/ULTRA.md).
# Hard requirements: clean tree, non-main branch, no secrets in the worktree.
set -euo pipefail

[ $# -eq 1 ] || { echo "Usage: AFK_OK=1 $0 <iterations 1-10>"; exit 1; }
ITERATIONS="$1"
case "$ITERATIONS" in
  ''|*[!0-9]*) echo "❌ Iterations must be an integer."; exit 1 ;;
esac
[ "$ITERATIONS" -ge 1 ] && [ "$ITERATIONS" -le 10 ] || {
  echo "❌ Iterations must be between 1 and 10."
  exit 1
}

[ "${AFK_OK:-0}" = "1" ] || {
  echo "❌ AFK mode is opt-in. Re-run with: AFK_OK=1 $0 $ITERATIONS"
  exit 1
}

if [ -n "${ANTHROPIC_API_KEY:-}" ]; then
  echo "❌ ANTHROPIC_API_KEY is set: an overnight loop would bill the API, not your plan."
  echo "   Run: unset ANTHROPIC_API_KEY   (see docs/ULTRA.md)"
  exit 1
fi

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

BRANCH="$(git branch --show-current)"
[ "$BRANCH" != "main" ] || {
  echo "❌ Never run AFK on main. Use a ticket worktree."
  exit 1
}

[ -f package.json ] || {
  echo "❌ No package.json: the stack is not initialized."
  exit 1
}

if [ -n "$(git status --porcelain)" ]; then
  echo "❌ Working tree is not clean. Commit or stash first; AFK must start from a known state."
  exit 1
fi

if [ -f .env ]; then
  echo "⚠️  .env is present in this worktree. The agent must never read or print it."
fi

mkdir -p .ralph
LOG=".ralph/afk-$(date +%Y%m%d-%H%M%S).md"
# Per-iteration guards. On a subscription the $ figure is an estimate: a runaway guard, not a bill.
BUDGET_PER_ITER="${AFK_BUDGET_USD:-4}"
MAX_TURNS="${AFK_MAX_TURNS:-60}"
case "$MAX_TURNS" in
  ''|*[!0-9]*) echo "❌ AFK_MAX_TURNS must be an integer."; exit 1 ;;
esac
if ! printf '%s' "$BUDGET_PER_ITER" | grep -Eq '^[0-9]+([.][0-9]+)?$'; then
  echo "❌ AFK_BUDGET_USD must be a number."; exit 1
fi

echo "▶ AFK — $BRANCH — $ITERATIONS iteration(s), max ${MAX_TURNS} turns and ~\$${BUDGET_PER_ITER} per iteration"
echo "  log: $LOG"
echo "  stop with Ctrl-C; every iteration commits on its own."
echo

for ((i = 1; i <= ITERATIONS; i++)); do
  echo "=== Iteration $i / $ITERATIONS — $(date '+%H:%M:%S') ===" | tee -a "$LOG"

  PROMPT="Tu es seul dans le worktree $(basename "$ROOT"), branche $BRANCH. Aucun humain n'est disponible.
Lis CLAUDE.md, docs/PRD.md, docs/DECISIONS.md, docs/ARCHITECTURE.md et progress/README.md.

1. Choisis la tâche non terminée la plus prioritaire de ta zone (risqué et intégrations d'abord): gh issue list --label ticket --state open --limit 30 (ou docs/TICKETS.md).
2. Si une question bloque, écris-la dans docs/QUESTIONS.md (question, options, recommandation) et choisis une autre tâche. N'invente pas de décision produit.
3. Écris le test, implémente, lance ./scripts/check.sh jusqu'au vert. Ne désactive jamais un test, un type ou un lint.
4. Écris ton journal dans progress/<ticket>.md. N'édite jamais progress.txt, docs/DECISIONS.md, docs/PRD.md.
5. Commit atomique. Ne push pas, ne merge pas, ne touche pas main.
6. Si plus aucune tâche n'est faisable, écris exactement <promise>COMPLETE</promise>.
NE TRAVAILLE QUE SUR UNE SEULE TÂCHE. Reste dans les fichiers du ticket."

  set +e
  claude -p \
    --permission-mode acceptEdits \
    --permission-prompts none \
    --max-turns "$MAX_TURNS" \
    --max-budget-usd "$BUDGET_PER_ITER" \
    "$PROMPT" 2>&1 | tee -a "$LOG"
  STATUS=${PIPESTATUS[0]}
  set -e

  if [ "$STATUS" -ne 0 ]; then
    echo "❌ Iteration $i failed with status $STATUS. Stopping; inspect the log." | tee -a "$LOG"
    exit "$STATUS"
  fi

  echo "— commits —" | tee -a "$LOG"
  git log --oneline -3 | tee -a "$LOG"
  echo | tee -a "$LOG"

  if grep -q '<promise>COMPLETE</promise>' "$LOG"; then
    echo "✅ Agent reports COMPLETE after $i iteration(s)." | tee -a "$LOG"
    exit 0
  fi
done

echo "⏹  Reached $ITERATIONS iteration(s). Review the diff, run ./scripts/check.sh --demo, then merge through a PR."
