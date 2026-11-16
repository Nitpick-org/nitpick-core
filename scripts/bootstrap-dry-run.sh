#!/usr/bin/env bash
# Bootstraps the throwaway dry-run starter on a disposable branch.
#
# Prefer a disposable clone:
#   git clone <repo> /tmp/lauzhack-dry-run && cd /tmp/lauzhack-dry-run
#   ./scripts/bootstrap-dry-run.sh
#
# It copies dry-run/starter into the repo root (package.json, src, e2e, configs),
# installs dependencies, and commits on branch dry-run/base.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

[ -f dry-run/starter/package.json ] || {
  echo "❌ dry-run/starter/package.json is missing."
  exit 1
}

command -v pnpm >/dev/null 2>&1 || {
  echo "❌ pnpm is required. Install Node 22+ and pnpm first."
  exit 1
}

if [ -n "$(git status --porcelain)" ]; then
  echo "❌ Working tree is not clean. Commit or stash before bootstrapping."
  exit 1
fi

if git show-ref --verify --quiet refs/heads/dry-run/base; then
  echo "❌ Branch dry-run/base already exists."
  echo "   Delete it first: git branch -D dry-run/base"
  exit 1
fi

git switch -c dry-run/base

cp dry-run/starter/package.json .
cp dry-run/starter/tsconfig.json .
cp dry-run/starter/vite.config.ts .
cp dry-run/starter/playwright.config.ts .
cp dry-run/starter/index.html .
cp dry-run/starter/.prettierignore .
mkdir -p src e2e
cp -R dry-run/starter/src/. src/
cp -R dry-run/starter/e2e/. e2e/

pnpm install

git add -A
git commit -m "chore: bootstrap dry-run starter"

cat <<'EOF'

✅ dry-run/base is ready: starter copied, dependencies installed, commit created.

Next:
  ./scripts/check.sh
  pnpm exec playwright install chromium   # once, for the e2e path
  ./scripts/check.sh --demo
  WT_FROM=dry-run/base ./scripts/new-worktree.sh 01-anomaly

Cleanup after the dry run:
  git switch main
  git branch -D dry-run/base
  git worktree remove --force .worktrees/wt-01-anomaly 2>/dev/null || true
  rm -rf .worktrees

This branch is throwaway. Never merge it into main.
EOF
