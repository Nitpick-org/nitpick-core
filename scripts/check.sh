#!/usr/bin/env bash
# One command for every feedback loop. Must pass before commit / PR.
#
# Usage:
#   ./scripts/check.sh            # typecheck + lint + test
#   ./scripts/check.sh --demo     # same, then the demo path (e2e)
#
# Stack-agnostic on purpose: it only calls scripts declared in package.json.
# See docs/STACK.md for the expected script names.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || {
  echo "❌ Not a git repository. Run: git init -b main"
  exit 2
}
cd "$ROOT"

if [ ! -f package.json ]; then
  echo "❌ No package.json: the stack is not initialized."
  echo "   Read docs/STACK.md, then run the bootstrap for the chosen stack."
  exit 2
fi

if ! command -v node >/dev/null 2>&1; then
  echo "❌ node is not installed. Install Node 22+ (see .nvmrc)."
  exit 2
fi

PM="npm"
if [ -f pnpm-lock.yaml ] && command -v pnpm >/dev/null 2>&1; then
  PM="pnpm"
elif [ -f package-lock.json ]; then
  PM="npm"
elif command -v pnpm >/dev/null 2>&1; then
  PM="pnpm"
fi

has_script() {
  node -e "const s=require('./package.json').scripts||{}; process.exit(s['$1']?0:1)"
}

run_script() {
  local script="$1"
  echo "▶ $script"
  if [ "$PM" = "pnpm" ]; then
    pnpm run --silent "$script"
  else
    npm run --silent "$script"
  fi
}

START=$SECONDS
missing=0

for script in typecheck lint test; do
  if has_script "$script"; then
    run_script "$script"
  else
    echo "❌ Missing script \"$script\" in package.json (see docs/STACK.md)."
    missing=1
  fi
done

if [ "$missing" -ne 0 ]; then
  echo "❌ check.sh failed: required feedback loops are missing."
  exit 1
fi

if [ "${1:-}" = "--demo" ]; then
  if has_script "e2e"; then
    run_script "e2e"
  else
    echo "❌ --demo requested but no \"e2e\" script in package.json."
    exit 1
  fi
fi

echo "✅ check.sh OK ($((SECONDS - START))s, $PM)"
