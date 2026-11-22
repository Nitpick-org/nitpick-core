# Onboarding — joining the repo in 20 minutes

> For the 2 other team members. Goal: clone, check the tools, sign in
> to Claude Code with **Ultra**, and be ready for the dry run.
> If something blocks you, don't hack around it alone: write it in `docs/QUESTIONS.md`.

## 0. GitHub access

The repo is **private**. H2 (or the owner) must:

1. GitHub → repo → **Settings → Collaborators → Add people**.
2. Add the two GitHub accounts.
3. Each of them accepts the invitation by email.

Then clone over SSH (recommended):

```bash
git clone git@github.com:zooldyyk/hackathon.git
cd hackathon
```

If port 22 times out:

```bash
git clone ssh://git@ssh.github.com:443/zooldyyk/hackathon.git
```

Or over HTTPS (you need a GitHub token):

```bash
git clone https://github.com/zooldyyk/hackathon.git
```

## 1. Prerequisites

| Tool | Version | Check |
| --- | --- | --- |
| Node | 22+ | `node -v` |
| pnpm | 11+ | `pnpm -v` |
| Git | recent | `git --version` |
| Claude Code | 2.1.286+ | `claude --version` |
| `gh` (optional) | recent | `gh --version` |

If `pnpm` is missing: `corepack enable`, then `corepack prepare pnpm@11 --activate`.
If `claude` is missing: https://code.claude.com/docs/en/overview

## 2. Configure the clone

```bash
git config core.hooksPath .githooks
cp .env.example .env
```

- `core.hooksPath` enables the pre-commit hook (check.sh + rejecting `.env`).
- `.env` is ignored by git. Never commit it.
- **Trust dialog**: open `claude` once in the repo (and in each worktree) and accept
  the trust dialog. Without it, `claude -p` / AFK ignores the project's `allow` rules.

## 3. Claude Code + Ultra

```bash
unset ANTHROPIC_API_KEY
claude
```

In the session:

- `/status` → check that you are signed in with the right account.
- Settings > Usage on claude.ai → **no "Current session" bar** = verified account.
- If `ANTHROPIC_API_KEY` is set in your shell, Claude Code bills the API instead of
  the subscription. Remove it from `~/.zshrc` / `~/.bashrc` if it is there.
- The **application** key is called `APP_ANTHROPIC_API_KEY` in `.env`. It must never
  be called `ANTHROPIC_API_KEY`.

> Two Ultra accounts in the team (H2, H3). H1 keeps the product decisions. Details:
> `docs/ULTRA.md`.

## 4. AI Hero skills

```bash
claude plugin install mattpocock-skills@claude-plugins-official
```

Then, once per repo, in a session:

```text
/setup-matt-pocock-skills
```

Check that the `/grill-with-docs`, `/to-spec`, `/to-tickets`, `/implement`, `/tdd`,
`/code-review` skills are available. The repo also provides the local **`/route-task`**
skill (choose the agent and the model before delegating).

## 5. Read before touching the code

1. `CLAUDE.md` — the team contract.
2. `docs/LAUZHACK.md` — the event, the rules, the jury.
3. `docs/ULTRA.md` — the accounts, the budgets, Fable.
4. `docs/MODEL-ROUTING.md` — Haiku/Sonnet/Opus/Fable, escalation.
5. `docs/STRATEGY.md` — choosing the idea, the demo.
6. `docs/PLAYBOOK.md` — the 24 h.
7. `docs/AGENT-OPS.md` — who does what with the agents.
8. `docs/ARCHITECTURE.md` — the modules and the tickets.

## 6. Check the repo

```bash
./scripts/doctor.sh
claude
```

- `doctor.sh` must show **a single failure**: `no package.json`. That is normal as long
  as the stack is not chosen. Everything else must be green or a warning.
- In Claude, ask "list the project subagents": the 8 agents of
  `.claude/agents/` must appear.
- The scripts are executable. If `Permission denied`:
  `chmod +x scripts/*.sh .githooks/pre-commit`.

## 7. Run the dry run

The real test is `docs/DRY-RUN.md`: a fictitious 2 h 30 mini-project, in a throwaway
clone, with the real scripts, the real agents and a 3-minute demo.

**Do not do it in the shared repo**: separate clone, `dry-run/base` branch, then
deletion. At the end, fill in `dry-run/REVIEW.md` and send it.

## 8. Working rules (summary)

- One ticket = one worktree = one file zone.
- `main`: free merges during the hackathon. Green `check.sh` recommended; H2 merges if possible.
- Do not touch `docs/PRD.md`, `docs/DECISIONS.md`, `CLAUDE.md` without a human request.
- Agents write `progress/<ticket>.md`, never `progress.txt`.
- No secret in the code, a prompt, a log or a commit.
- No `--dangerously-skip-permissions` on a machine with secrets.
- No Fable by default; Fable by hand for high-stakes decisions.
- The bottleneck is **review**, not generation. 8 sessions max, 3 writers.

### GitHub settings (owner)

- [x] **Collaborators**: the 2 others are added and have accepted (16/11).
- [x] **Branches**: `main` without protection, direct merge allowed (21/11).
- [x] **Actions**: enabled; the CI workflow runs on PRs (16/11).
- [x] **Labels**: `ticket`, `bug`, `demo`, `P0`, `P1`, `P2`, `blocked`, `review` (16/11).
- [ ] **Project**: Todo / Doing / Review / Done columns (to create via the web UI; the `gh` token lacks the `project` scope).
- [ ] **Visibility**: private repo during the hack; **public before the submission** (the link
      of `app.lauzhack.com` must open without a login).

## 9. Troubleshooting

| Symptom | Cause | Fix |
| --- | --- | --- |
| `fatal: not a git repository` | wrong folder | `cd hackathon` |
| `Permission denied` on a script | executable bit lost | `chmod +x scripts/*.sh .githooks/pre-commit` |
| `Host key verification failed` | known_hosts | `ssh -p 443 git@ssh.github.com` then accept |
| `ssh: connect to host github.com port 22: timeout` | network | use `ssh://git@ssh.github.com:443/...` |
| `doctor.sh` fails on `no package.json` | stack not chosen | normal before J-4 |
| `ANTHROPIC_API_KEY is set` | shell variable | `unset ANTHROPIC_API_KEY` |
| `gh: not logged in` | GitHub CLI | `gh auth login` (optional) |
| `claude: command not found` | not installed | install Claude Code |
| `pnpm: command not found` | not installed | `corepack enable` |

## 10. "I am ready" checklist

- [ ] Repo cloned, hooks configured, `.env` created.
- [ ] `claude` signed in to the right account, `/status` checked.
- [ ] `ANTHROPIC_API_KEY` absent from the shell.
- [ ] Skills installed, `/setup-matt-pocock-skills` run.
- [ ] `doctor.sh`: a single failure, `no package.json`.
- [ ] 8 agents visible.
- [ ] `CLAUDE.md`, `LAUZHACK.md`, `ULTRA.md`, `PLAYBOOK.md` read.
- [ ] Dry run scheduled (2 h 30).
