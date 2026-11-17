#!/usr/bin/env bash
# One agent iteration, watched by a human (HITL). AI Hero "Ralph" pattern.
# Start here; only go AFK once the prompt is reliable.
#
# Usage:
#   ./scripts/ralph-once.sh            # agent picks the highest-priority ticket
#   ./scripts/ralph-once.sh 12-login   # agent works only on ticket 12
#
# Safety: no push, no merge, no main, no secrets, one ticket per run.
set -euo pipefail

# Headless run: the guard must treat this session as agent-driven (no human in the loop).
export HACKATHON_HEADLESS=1

if [ -n "${ANTHROPIC_API_KEY:-}" ]; then
  echo "❌ ANTHROPIC_API_KEY is set: Claude Code would bill the API instead of your plan."
  echo "   Run: unset ANTHROPIC_API_KEY   (see docs/ULTRA.md)"
  exit 1
fi

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

BRANCH="$(git branch --show-current)"
[ "$BRANCH" != "main" ] || {
  echo "❌ Do not run an agent on main. Create a ticket worktree first:"
  echo "   ./scripts/new-worktree.sh 12-login"
  exit 1
}

if [ -n "$(git status --porcelain)" ]; then
  echo "⚠️  Working tree is not clean. The agent may mix its changes with yours."
  printf 'Continue? [y/N] '; read -r answer
  [ "$answer" = "y" ] || exit 1
fi

TICKET="${1:-}"
if [ -n "$TICKET" ]; then
  TASK="Travaille uniquement sur le ticket #$TICKET. Lis-le avec: gh issue view $TICKET (ou docs/TICKETS.md)."
else
  TASK="Choisis le ticket non terminé le plus prioritaire. Utilise: gh issue list --label ticket --state open --limit 30 (ou docs/TICKETS.md). Priorise le risqué et les intégrations."
fi

MAX_TURNS="${RALPH_MAX_TURNS:-40}"
BUDGET_USD="${RALPH_BUDGET_USD:-3}"
case "$MAX_TURNS" in
  ''|*[!0-9]*) echo "❌ RALPH_MAX_TURNS must be an integer."; exit 1 ;;
esac
if ! printf '%s' "$BUDGET_USD" | grep -Eq '^[0-9]+([.][0-9]+)?$'; then
  echo "❌ RALPH_BUDGET_USD must be a number."; exit 1
fi

mkdir -p .ralph
LOG=".ralph/ralph-$(date +%Y%m%d-%H%M%S).md"

PROMPT="Tu es un membre de l'équipe dans le worktree $(basename "$ROOT"), branche $BRANCH.
Lis d'abord CLAUDE.md, docs/PRD.md, docs/DECISIONS.md, docs/ARCHITECTURE.md et progress/README.md.
$TASK

Règles:
1. Une seule tâche. Ne touche qu'aux fichiers du ticket.
2. Avant de coder, liste tes questions. Si une réponse change le résultat, écris-la dans docs/QUESTIONS.md et choisis une autre tâche ou demande à l'humain.
3. Écris d'abord le test du comportement attendu.
4. Implémente le minimum, puis lance ./scripts/check.sh jusqu'au vert. Ne désactive jamais un test, un type ou un lint.
5. Écris ton journal dans progress/<ticket>.md (crée-le). N'édite jamais progress.txt, docs/DECISIONS.md, docs/PRD.md.
6. Commit atomique (feat|fix|test|docs(scope): ...). Ne push pas, ne merge pas, ne touche pas main.
7. Si plus aucune tâche n'est faisable, écris exactement <promise>COMPLETE</promise>.
NE TRAVAILLE QUE SUR UNE SEULE TÂCHE."

echo "▶ ralph-once — $BRANCH — log: $LOG"
set +e
claude -p \
  --permission-mode acceptEdits \
  --permission-prompts none \
  --max-turns "$MAX_TURNS" \
  --max-budget-usd "$BUDGET_USD" \
  "$PROMPT" 2>&1 | tee -a "$LOG"
STATUS=${PIPESTATUS[0]}
set -e

if [ "$STATUS" -ne 0 ]; then
  echo "❌ Agent exited with status $STATUS. Read the log: $LOG"
  exit "$STATUS"
fi

if grep -q '<promise>COMPLETE</promise>' "$LOG"; then
  echo "✅ Agent reports COMPLETE. Verify the diff and the demo path yourself."
fi

echo "Next: git diff, ./scripts/check.sh --demo, then open a PR."
