#!/usr/bin/env bash
# Verifies the demo path, not just the unit tests.
# Run it at H6, H12, H18, and before the code freeze.
#
# Usage:
#   ./scripts/demo-check.sh                 # e2e suite, if declared
#   ./scripts/demo-check.sh --url <url>     # also curl the deployed demo
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

URL=""
if [ "${1:-}" = "--url" ]; then
  [ -n "${2:-}" ] || { echo "Usage: $0 [--url <url>]"; exit 1; }
  URL="$2"
fi

if [ ! -f package.json ]; then
  echo "❌ No package.json: the stack is not initialized."
  exit 2
fi

PM="npm"
if [ -f pnpm-lock.yaml ] && command -v pnpm >/dev/null 2>&1; then PM="pnpm"; fi

has_script() {
  node -e "const s=require('./package.json').scripts||{}; process.exit(s['$1']?0:1)"
}

if has_script "e2e"; then
  echo "▶ e2e demo path"
  if [ "$PM" = "pnpm" ]; then pnpm run --silent e2e; else npm run --silent e2e; fi
else
  echo "⚠️  No \"e2e\" script. The demo path is not automated yet."
  echo "   Add Playwright and a single test that follows docs/DEMO.md."
fi

if [ -n "$URL" ]; then
  echo "▶ deployed demo: $URL"
  CODE="$(curl -sS -o /dev/null -w '%{http_code}' --max-time 15 "$URL" || echo 000)"
  if [ "$CODE" = "200" ]; then
    echo "✅ demo responds 200"
  else
    echo "❌ demo responds $CODE (not 200)"
    exit 1
  fi
fi

echo "✅ demo-check OK"
