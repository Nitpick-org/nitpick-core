#!/usr/bin/env bash
# One command for every feedback loop. Must pass before commit / PR.
#
# Usage:
#   ./scripts/check.sh            # shared tests + desktop + Android builds
#   ./scripts/check.sh --demo     # same, then a reminder to play docs/DEMO.md
#
# allTests compiles and tests every target (desktop included) without
# depending on the target names the KMP wizard picks; assembleDebug compiles
# Android, which has no host tests to force it.
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

echo "▶ gradle: allTests + assembleDebug"
./gradlew --quiet :composeApp:allTests :composeApp:assembleDebug

if [ "${1:-}" = "--demo" ]; then
  # shortcut: no automated UI test on Compose yet, add one if time allows after the freeze.
  echo "⚠️  Demo path is manual: play docs/DEMO.md on desktop and Android."
fi

echo "✅ check.sh OK ($((SECONDS - START))s)"
