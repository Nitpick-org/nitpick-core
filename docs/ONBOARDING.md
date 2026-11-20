# Onboarding — rejoindre le repo en 20 minutes

> Pour les 2 autres membres de l'équipe. Objectif : cloner, vérifier les outils, se
> connecter à Claude Code avec **Ultra**, et être prêt pour le dry run.
> Si un point bloque, ne bricole pas seul : note-le dans `docs/QUESTIONS.md`.

## 0. Accès GitHub

Le repo est **privé**. H2 (ou le propriétaire) doit :

1. GitHub → repo → **Settings → Collaborators → Add people**.
2. Ajouter les deux comptes GitHub.
3. Chacun accepte l'invitation par email.

Ensuite, clone en SSH (recommandé) :

```bash
git clone git@github.com:zooldyyk/hackathon.git
cd hackathon
```

Si le port 22 timeout :

```bash
git clone ssh://git@ssh.github.com:443/zooldyyk/hackathon.git
```

Ou en HTTPS (il faut un token GitHub) :

```bash
git clone https://github.com/zooldyyk/hackathon.git
```

## 1. Prérequis

| Outil | Version | Vérifier |
| --- | --- | --- |
| Node | 22+ | `node -v` |
| pnpm | 11+ | `pnpm -v` |
| Git | récent | `git --version` |
| Claude Code | 2.1.286+ | `claude --version` |
| `gh` (optionnel) | récent | `gh --version` |

Si `pnpm` manque : `corepack enable` puis `corepack prepare pnpm@11 --activate`.
Si `claude` manque : https://code.claude.com/docs/en/overview

## 2. Configurer le clone

```bash
git config core.hooksPath .githooks
cp .env.example .env
```

- `core.hooksPath` active le hook pre-commit (check.sh + refus des `.env`).
- `.env` est ignoré par git. Ne le commit jamais.
- **Trust dialog** : ouvre `claude` une fois dans le repo (et dans chaque worktree) et accepte
  le dialogue de confiance. Sans ça, `claude -p` / AFK ignore les règles `allow` du projet.

## 3. Claude Code + Ultra

```bash
unset ANTHROPIC_API_KEY
claude
```

Dans la session :

- `/status` → vérifier que tu es connecté avec le bon compte.
- Settings > Usage sur claude.ai → **pas de barre « Current session »** = compte vérifié.
- Si `ANTHROPIC_API_KEY` est définie dans ton shell, Claude Code facture l'API au lieu de
  l'abonnement. Retire-la de `~/.zshrc` / `~/.bashrc` si elle y est.
- La clé de l'**application** s'appelle `APP_ANTHROPIC_API_KEY` dans `.env`. Elle ne doit
  jamais s'appeler `ANTHROPIC_API_KEY`.

> Deux comptes Ultra dans l'équipe (H2, H3). H1 garde les décisions produit. Détail :
> `docs/ULTRA.md`.

## 4. Skills AI Hero

```bash
claude plugin install mattpocock-skills@claude-plugins-official
```

Puis, une fois par repo, dans une session :

```text
/setup-matt-pocock-skills
```

Vérifie que les skills `/grill-with-docs`, `/to-spec`, `/to-tickets`, `/implement`, `/tdd`,
`/code-review` sont disponibles. Le repo fournit aussi le skill local **`/route-task`**
(choisir l'agent et le modèle avant de déléguer).

## 5. Lire avant de toucher au code

1. `CLAUDE.md` — le contrat d'équipe.
2. `docs/LAUZHACK.md` — l'événement, les règles, le jury.
3. `docs/ULTRA.md` — les comptes, les budgets, Fable.
4. `docs/MODEL-ROUTING.md` — Haiku/Sonnet/Opus/Fable, escalade.
5. `docs/STRATEGY.md` — choisir l'idée, la démo.
6. `docs/PLAYBOOK.md` — les 24 h.
7. `docs/AGENT-OPS.md` — qui fait quoi avec les agents.
8. `docs/ARCHITECTURE.md` — les modules et les tickets.

## 6. Vérifier le repo

```bash
./scripts/doctor.sh
claude
```

- `doctor.sh` doit afficher **un seul échec** : `no package.json`. C'est normal tant que la
  stack n'est pas choisie. Tout le reste doit être vert ou warning.
- Dans Claude, demande « liste les sous-agents du projet » : les 8 agents de
  `.claude/agents/` doivent apparaître.
- Les scripts sont exécutables. Si `Permission denied` :
  `chmod +x scripts/*.sh .githooks/pre-commit`.

## 7. Faire le dry run

Le vrai test, c'est `docs/DRY-RUN.md` : un mini-projet fictif de 2 h 30, dans un clone
jetable, avec les vrais scripts, les vrais agents et une démo de 3 minutes.

**Ne le fais pas dans le repo partagé** : clone à part, branche `dry-run/base`, puis
suppression. À la fin, remplis `dry-run/REVIEW.md` et envoie-le.

## 8. Règles de travail (résumé)

- Un ticket = un worktree = une zone de fichiers.
- `main` uniquement par PR, `check.sh` vert. H2 merge.
- Ne touche pas `docs/PRD.md`, `docs/DECISIONS.md`, `CLAUDE.md` sans demande humaine.
- Les agents écrivent `progress/<ticket>.md`, jamais `progress.txt`.
- Pas de secret dans le code, un prompt, un log ou un commit.
- Pas de `--dangerously-skip-permissions` sur une machine avec des secrets.
- Pas de Fable par défaut ; Fable à la main pour les décisions à fort enjeu.
- Le goulot est la **revue**, pas la génération. 8 sessions max, 3 écrivains.

### Réglages GitHub (owner)

- [x] **Collaborators** : les 2 autres sont ajoutés et ont accepté (16/11).
- [x] **Branches** : `main` protégée, PR obligatoire, `check` requis, pas de force push (16/11).
- [x] **Actions** : activées ; le workflow CI tourne sur les PR (16/11).
- [x] **Labels** : `ticket`, `bug`, `demo`, `P0`, `P1`, `P2`, `blocked`, `review` (16/11).
- [ ] **Project** : colonnes Todo / Doing / Review / Done (à créer via l'UI web ; le token `gh` n'a pas le scope `project`).
- [ ] **Visibilité** : repo privé pendant le hack ; **public avant la soumission** (le lien
      de `app.lauzhack.com` doit s’ouvrir sans login).

## 9. Dépannage

| Symptôme | Cause | Solution |
| --- | --- | --- |
| `fatal: not a git repository` | mauvais dossier | `cd hackathon` |
| `Permission denied` sur un script | bit exécutable perdu | `chmod +x scripts/*.sh .githooks/pre-commit` |
| `Host key verification failed` | known_hosts | `ssh -p 443 git@ssh.github.com` puis accepter |
| `ssh: connect to host github.com port 22: timeout` | réseau | utiliser `ssh://git@ssh.github.com:443/...` |
| `doctor.sh` échoue sur `no package.json` | stack pas choisie | normal avant J-4 |
| `ANTHROPIC_API_KEY is set` | variable de shell | `unset ANTHROPIC_API_KEY` |
| `gh: not logged in` | CLI GitHub | `gh auth login` (optionnel) |
| `claude: command not found` | pas installé | installer Claude Code |
| `pnpm: command not found` | pas installé | `corepack enable` |

## 10. Checklist « je suis prêt »

- [ ] Repo cloné, hooks configurés, `.env` créé.
- [ ] `claude` connecté au bon compte, `/status` vérifié.
- [ ] `ANTHROPIC_API_KEY` absente du shell.
- [ ] Skills installés, `/setup-matt-pocock-skills` passé.
- [ ] `doctor.sh` : un seul échec, `no package.json`.
- [ ] 8 agents visibles.
- [ ] `CLAUDE.md`, `LAUZHACK.md`, `ULTRA.md`, `PLAYBOOK.md` lus.
- [ ] Dry run planifié (2 h 30).
