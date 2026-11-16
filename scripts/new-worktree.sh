#!/usr/bin/env bash
# Creates an isolated worktree + branch for one ticket.
# Rule: one ticket = one worktree = one agent = one file zone.
#
# Usage: ./scripts/new-worktree.sh 12-login
set -euo pipefail

[ $# -eq 1 ] || { echo "Usage: $0 <ticket-number-name>"; exit 1; }
NAME="$1"
case "$NAME" in
  *[!A-Za-z0-9._-]*) echo "❌ Ticket name may only contain letters, digits, dot, dash, underscore."; exit 1 ;;
esac

ROOT="$(git rev-parse --show-toplevel)"
BRANCH="ticket/$NAME"
BASE_DIR="${WT_BASE:-$(dirname "$ROOT")}"
DIR="$BASE_DIR/wt-$NAME"

git -C "$ROOT" rev-parse --verify HEAD >/dev/null 2>&1 || {
  echo "❌ No initial commit yet. Commit the base before creating worktrees."
  exit 1
}

if git -C "$ROOT" show-ref --verify --quiet "refs/heads/$BRANCH"; then
  echo "❌ Branch $BRANCH already exists."
  exit 1
fi

if [ -e "$DIR" ]; then
  echo "❌ Directory already exists: $DIR"
  exit 1
fi

BASE="main"
git -C "$ROOT" fetch origin main >/dev/null 2>&1 || true
if git -C "$ROOT" show-ref --verify --quiet refs/remotes/origin/main; then
  BASE="origin/main"
fi

mkdir -p "$BASE_DIR"
git -C "$ROOT" worktree add -b "$BRANCH" "$DIR" "$BASE"

# Local secrets are never committed; copy them so the worktree can run.
for file in .env .env.local .env.development.local; do
  if [ -f "$ROOT/$file" ]; then
    cp "$ROOT/$file" "$DIR/$file"
  fi
done

mkdir -p "$DIR/progress"
if [ ! -f "$DIR/progress/$NAME.md" ]; then
  cat > "$DIR/progress/$NAME.md" <<EOF
# Ticket $NAME

- Statut : en cours
- Propriétaire humain : @
- Agent : @implementeur
- Fichiers autorisés : src/modules/<...>

## Journal

- $(date '+%H:%M') — worktree créé.
EOF
fi

echo "✅ Worktree ready: $DIR (branch $BRANCH from $BASE)"
echo
echo "Next:"
echo "  cd $DIR && pnpm install   # si node_modules n'est pas partagé"
echo "  cd $DIR && claude"
echo "  # or, from this checkout: claude --worktree $NAME"
echo
echo "Brief for the agent (paste it):"
echo "  Lis CLAUDE.md, docs/PRD.md, docs/DECISIONS.md et progress/$NAME.md."
echo "  Ta tâche : ticket #$NAME. Fichiers autorisés : <...>."
echo "  Écris dans progress/$NAME.md, jamais dans progress.txt ni docs/DECISIONS.md."
