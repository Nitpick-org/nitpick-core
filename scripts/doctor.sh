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
if command -v node >/dev/null 2>&1; then
  NODE_MAJOR="$(node -p "process.versions.node.split('.')[0]" 2>/dev/null || echo 0)"
  if [ "$NODE_MAJOR" -ge 22 ]; then
    ok "node $(node -v)"
  else
    fail "node $(node -v) is too old (need 22+)"
  fi
else
  fail "node is not installed"
fi

command -v pnpm >/dev/null 2>&1 && ok "pnpm $(pnpm -v)" || warn "pnpm not installed (npm fallback)"
command -v gh   >/dev/null 2>&1 && ok "gh installed"        || warn "gh not installed"
command -v claude >/dev/null 2>&1 && ok "claude $(claude --version 2>/dev/null | head -1)" || fail "claude is not installed"

if command -v gh >/dev/null 2>&1; then
  gh auth status -h github.com >/dev/null 2>&1 && ok "gh authenticated" || warn "gh not authenticated (run: gh auth login)"
fi

echo
echo "── Stack ──────────────────────────────────────────────────"
if [ -f package.json ]; then
  ok "package.json present"
  if [ -d node_modules ]; then ok "node_modules present"; else warn "node_modules missing (run the install command)"; fi

  for script in typecheck lint test e2e; do
    if node -e "const s=require('./package.json').scripts||{}; process.exit(s['$script']?0:1)" 2>/dev/null; then
      ok "script: $script"
    else
      [ "$script" = "e2e" ] && warn "script: e2e missing (demo path not automated)" || fail "script: $script missing"
    fi
  done

  if [ -f pnpm-lock.yaml ] || [ -f package-lock.json ]; then
    ok "lockfile present"
  else
    warn "no lockfile yet (install dependencies, then commit it)"
  fi
else
  fail "no package.json: the stack is not initialized (see docs/STACK.md)"
fi

if [ -f .env ]; then
  ok ".env present"
  grep -q '^DEPLOY_URL=' .env 2>/dev/null && ok "DEPLOY_URL set in .env" || warn "DEPLOY_URL missing in .env (no preview URL to verify)"
else
  warn ".env missing (copy .env.example once the stack is chosen)"
fi

echo
echo "── Agents ─────────────────────────────────────────────────"
if [ -d .claude/agents ]; then
  COUNT="$(find .claude/agents -maxdepth 1 -name '*.md' | wc -l | tr -d ' ')"
  [ "$COUNT" -ge 7 ] && ok "$COUNT project subagents" || warn "only $COUNT subagents (expected 7+)"
else
  fail ".claude/agents missing"
fi

if [ -f .claude/settings.json ]; then
  if node -e "JSON.parse(require('fs').readFileSync('.claude/settings.json','utf8'))" 2>/dev/null; then
    ok ".claude/settings.json is valid JSON"
  else
    fail ".claude/settings.json is invalid JSON"
  fi
fi

[ -f AGENTS.md ] && ok "AGENTS.md present" || fail "AGENTS.md missing"
[ -f CLAUDE.md ] && ok "CLAUDE.md present" || fail "CLAUDE.md missing"

echo
echo "── Project docs ───────────────────────────────────────────"
for doc in docs/PRD.md docs/DEMO.md docs/DECISIONS.md docs/QUESTIONS.md docs/LAUZHACK.md docs/STRATEGY.md docs/AGENT-OPS.md docs/STACK.md; do
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
