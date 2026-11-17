#!/usr/bin/env bash
# PostToolUse hook: format only the file that was just written/edited.
# Reads the hook JSON on stdin.
set -euo pipefail

INPUT="$(cat)"
FILE="$(jq -r '.tool_input.file_path // .tool_input.notebook_path // empty' <<<"$INPUT" 2>/dev/null || true)"

[ -n "$FILE" ] || exit 0
[ -f "$FILE" ] || exit 0
[ -f package.json ] || exit 0

case "$FILE" in
  *.ts|*.tsx|*.js|*.jsx|*.json|*.css|*.scss|*.md|*.yml|*.yaml) ;;
  *) exit 0 ;;
esac

if [ -f pnpm-lock.yaml ] && command -v pnpm >/dev/null 2>&1; then
  pnpm exec prettier --write --ignore-unknown "$FILE" >/dev/null 2>&1 || true
elif command -v npx >/dev/null 2>&1; then
  npx prettier --write --ignore-unknown "$FILE" >/dev/null 2>&1 || true
fi
