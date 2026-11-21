#!/usr/bin/env bash
# One command for every feedback loop. Must pass before commit / PR.
#
# Usage:
#   ./scripts/check.sh            # shared + server tests, desktop + Android builds
#   ./scripts/check.sh --demo     # same, then a reminder to play docs/DEMO.md
#
# :shared:allTests runs the JVM and Android host tests; the two app modules
# hold no tests, so they are only built.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || {
  echo "❌ Not a git repository. Run: git init -b main"
  exit 2
}
cd "$ROOT"

if [ ! -x gradlew ]; then
  echo "❌ No gradlew: the KMP project is not generated yet."
  echo "   Generate it from https://kmp.jetbrains.com (see docs/CONTEXT.md §5)."
  exit 2
fi

START=$SECONDS

echo "▶ gradle: shared + server tests, desktop and Android builds"
./gradlew --quiet :shared:allTests :server:test :desktopApp:assemble :androidApp:assembleDebug

if [ "${1:-}" = "--demo" ]; then
  # shortcut: no automated UI test on Compose yet, add one if time allows after the freeze.
  echo "⚠️  Demo path is manual: play docs/DEMO.md on desktop and Android."
fi

echo "✅ check.sh OK ($((SECONDS - START))s)"
