#!/usr/bin/env bash
# Removes a ticket worktree and its branch.
# Refuses to remove a dirty worktree unless --force is passed.
#
# Usage: ./scripts/rm-worktree.sh 12-login [--force]
set -euo pipefail

[ $# -ge 1 ] || { echo "Usage: $0 <ticket-number-name> [--force]"; exit 1; }
NAME="$1"
FORCE="${2:-}"
case "$NAME" in
  *[!A-Za-z0-9._-]*) echo "❌ Invalid ticket name."; exit 1 ;;
esac

ROOT="$(git rev-parse --show-toplevel)"
BRANCH="ticket/$NAME"
BASE_DIR="${WT_BASE:-$(dirname "$ROOT")}"
DIR="$BASE_DIR/wt-$NAME"

if ! git -C "$ROOT" worktree list --porcelain | grep -Fq "worktree $DIR"; then
  echo "❌ No worktree registered at $DIR"
  exit 1
fi

if [ "$FORCE" = "--force" ]; then
  git -C "$ROOT" worktree remove --force "$DIR"
else
  if ! git -C "$ROOT" worktree remove "$DIR" 2>/dev/null; then
    echo "❌ Worktree is dirty or locked. Commit/merge its work first, or rerun with --force."
    exit 1
  fi
fi

if git -C "$ROOT" show-ref --verify --quiet "refs/heads/$BRANCH"; then
  git -C "$ROOT" branch -D "$BRANCH" >/dev/null
fi

echo "✅ Removed $DIR and branch $BRANCH"
echo "   Check that progress/$NAME.md was merged into progress.txt before deleting it."
