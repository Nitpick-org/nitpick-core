#!/usr/bin/env bash
# Read-only readiness check for the hackathon base.
# Run it before the event, then again at H0 and H12.
#
# Usage: ./scripts/doctor.sh
set -uo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || {
  echo "❌ Not a git repository. Run: git init -b main"
  exit 2
}
cd "$ROOT"

IS_CI="${CI:-false}"
OK=0
WARN=0
FAIL=0

ok()   { printf '✅ %s\n' "$1"; OK=$((OK + 1)); }
warn() { printf '⚠️  %s\n' "$1"; WARN=$((WARN + 1)); }
fail() { printf '❌ %s\n' "$1"; FAIL=$((FAIL + 1)); }

echo "── Repository ─────────────────────────────────────────────"
if git rev-parse --verify HEAD >/dev/null 2>&1; then
  ok "git repository with at least one commit"
else
  warn "git repository has no commit yet"
fi

BRANCH="$(git branch --show-current 2>/dev/null || true)"
[ -n "$BRANCH" ] && ok "current branch: $BRANCH" || warn "detached HEAD"

HOOKS="$(git config --get core.hooksPath 2>/dev/null || true)"
if [ "$HOOKS" = ".githooks" ]; then
  ok "core.hooksPath = .githooks"
elif [ "$IS_CI" = "true" ]; then
  warn "core.hooksPath not set in CI (expected)"
else
  fail "core.hooksPath is not .githooks (run: git config core.hooksPath .githooks)"
fi

REMOTE="$(git remote get-url origin 2>/dev/null || true)"
[ -n "$REMOTE" ] && ok "origin: $REMOTE" || warn "no origin remote yet"

if git ls-files --error-unmatch .env >/dev/null 2>&1; then
  fail ".env is tracked by git (remove it from the index)"
else
  ok ".env is not tracked"
fi

echo
echo "── Toolchain ──────────────────────────────────────────────"
if command -v java >/dev/null 2>&1; then
  JAVA_MAJOR="$(java -version 2>&1 | head -1 | sed -E 's/[^"]*"([0-9]+).*/\1/')"
  if [ "${JAVA_MAJOR:-0}" -ge 17 ]; then
    ok "java $JAVA_MAJOR"
  else
    fail "java $JAVA_MAJOR is too old (need 17+)"
  fi
else
  fail "java is not installed (need a JDK 17+)"
fi

if [ -n "${ANDROID_HOME:-}" ] && [ -d "$ANDROID_HOME/platforms" ]; then
  ok "Android SDK: $ANDROID_HOME"
else
  fail "ANDROID_HOME is not set or has no platforms (install the Android SDK)"
fi

command -v adb >/dev/null 2>&1 && ok "adb installed" || warn "adb not on PATH (needed to install on a phone)"
command -v gh   >/dev/null 2>&1 && ok "gh installed"        || warn "gh not installed"
if command -v claude >/dev/null 2>&1; then
  ok "claude $(claude --version 2>/dev/null | head -1)"
elif [ "$IS_CI" = "true" ]; then
  warn "claude not installed in CI (expected)"
else
  fail "claude is not installed"
fi

if command -v gh >/dev/null 2>&1; then
  gh auth status -h github.com >/dev/null 2>&1 && ok "gh authenticated" || warn "gh not authenticated (run: gh auth login)"
fi

echo
echo "── Claude plan ────────────────────────────────────────────"
if [ -n "${ANTHROPIC_API_KEY:-}" ]; then
  if [ "$IS_CI" = "true" ]; then
    warn "ANTHROPIC_API_KEY set in CI (fine only if CI calls the API on purpose)"
  else
    fail "ANTHROPIC_API_KEY is set in this shell: Claude Code bills the API, not your plan (unset it; see docs/ULTRA.md)"
  fi
else
  ok "ANTHROPIC_API_KEY not set (Claude Code uses your subscription)"
fi
if [ -f .env ] && grep -qE '^[[:space:]]*(export[[:space:]]+)?ANTHROPIC_API_KEY=' .env 2>/dev/null; then
  fail ".env defines ANTHROPIC_API_KEY: rename it APP_ANTHROPIC_API_KEY"
fi
if [ "$IS_CI" != "true" ]; then
  warn "manual: /usage shows ≥ 75 % weekly left and the account is verified (docs/ULTRA.md)"
fi

echo
echo "── Stack ──────────────────────────────────────────────────"
if [ -x gradlew ]; then
  ok "gradlew present"
  [ -d composeApp/src/commonMain ] && ok "composeApp/src/commonMain present" || fail "composeApp/src/commonMain missing (see docs/CONTEXT.md §6)"
else
  fail "no gradlew: the KMP project is not generated yet (see docs/CONTEXT.md §5)"
fi

if [ -f .env ]; then
  ok ".env present"
else
  warn ".env missing (copy .env.example once the stack is chosen)"
fi

echo
echo "── Agents ─────────────────────────────────────────────────"
if [ -d .claude/agents ]; then
  COUNT="$(find .claude/agents -maxdepth 1 -name '*.md' | wc -l | tr -d ' ')"
  [ "$COUNT" -ge 8 ] && ok "$COUNT project subagents" || warn "only $COUNT subagents (expected 8+)"
else
  fail ".claude/agents missing"
fi

if [ -f .claude/skills/route-task/SKILL.md ]; then
  ok "route-task skill present"
else
  warn "route-task skill missing"
fi

for script in scripts/guard-protected.sh scripts/guard-bash.sh scripts/format-edited.sh scripts/stop-check.sh; do
  if [ -x "$script" ]; then
    ok "hook script: $script"
  else
    fail "hook script missing or not executable: $script"
  fi
done

if [ -f .claude/settings.json ]; then
  if jq empty .claude/settings.json 2>/dev/null; then
    ok ".claude/settings.json is valid JSON"
  else
    fail ".claude/settings.json is invalid JSON"
  fi
fi

[ -f AGENTS.md ] && ok "AGENTS.md present" || fail "AGENTS.md missing"
[ -f CLAUDE.md ] && ok "CLAUDE.md present" || fail "CLAUDE.md missing"

echo
echo "── Project docs ───────────────────────────────────────────"
for doc in docs/PRD.md docs/DEMO.md docs/DECISIONS.md docs/QUESTIONS.md docs/LAUZHACK.md docs/STRATEGY.md docs/AGENT-OPS.md docs/ULTRA.md docs/MODEL-ROUTING.md docs/STACK.md docs/ONBOARDING.md docs/DRY-RUN.md; do
  [ -f "$doc" ] && ok "$doc" || warn "$doc missing"
done

if grep -qE '<NOM DU PROJET>|<PRÉNOM|<UNE PHRASE|<DATE>' docs/PRD.md CLAUDE.md 2>/dev/null; then
  warn "placeholders remain in CLAUDE.md/PRD.md — fill them at H0"
else
  ok "no obvious placeholders in CLAUDE.md/PRD.md"
fi

[ -d progress ] && ok "progress/ present" || warn "progress/ missing (agents need a conflict-free log)"

echo
echo "── CI ─────────────────────────────────────────────────────"
[ -f .github/workflows/ci.yml ] && ok "CI workflow present" || warn "no CI workflow"
[ -f .github/ISSUE_TEMPLATE/ticket.md ] && ok "ticket template present" || warn "no ticket template"
[ -f dry-run/starter/package.json ] && ok "dry-run starter present" || warn "dry-run starter missing"
[ -f dry-run/REVIEW.md ] && ok "review template present" || warn "review template missing"
[ -f .github/ISSUE_TEMPLATE/bug.md ] && ok "bug template present" || warn "no bug template"

echo
echo "───────────────────────────────────────────────────────────"
printf 'Summary: %d ok, %d warnings, %d failures\n' "$OK" "$WARN" "$FAIL"
if [ "$FAIL" -gt 0 ]; then
  echo "❌ Not ready. Fix the failures above."
  exit 1
fi
if [ "$WARN" -gt 0 ]; then
  echo "⚠️  Ready enough to keep going; review the warnings."
  exit 0
fi
echo "✅ Ready."
