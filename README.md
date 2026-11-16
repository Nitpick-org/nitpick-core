# LauzHack 2026 — base d'équipe (3 humains + agents)

Base de travail pour **LauzHack 2026** (21–22 novembre, EPFL BC) : 24 h de hack, démo de
3 minutes, équipe de 3. L'idée n'est pas encore choisie : ce repo contient tout ce qui ne
dépend pas d'elle — méthode, rôles, agents, scripts, garde-fous, faits de l'événement.

Méthode inspirée d'[AI Hero](https://www.aihero.dev) (Matt Pocock) :
**idée → recherche → prototype → PRD → tickets → exécution → QA**, avec des modules
profonds et des garde-fous exécutables.

## État actuel (16/11/2026)

- La base a été auditée et corrigée : voir [docs/AUDIT-2026-11-16.md](./docs/AUDIT-2026-11-16.md).
- La **stack n'est pas choisie** : `package.json` n'existe pas encore.
- `./scripts/check.sh` et la CI échouent tant que la stack n'est pas initialisée.
  **C'est volontaire** : une base sans stack n'est pas une base prête.
- Prochaine étape : choisir la stack (J-4) et déployer un hello world (J-4 au plus tard).

## Démarrage rapide

```bash
# 1. Hooks git (bloque les commits si check.sh échoue)
git config core.hooksPath .githooks

# 2. Variables d'environnement
cp .env.example .env      # remplir, ne jamais commiter

# 3. État de préparation
./scripts/doctor.sh

# 4. Claude Code + skills AI Hero (une fois par repo)
claude plugin install mattpocock-skills@claude-plugins-official
# puis dans une session : /setup-matt-pocock-skills
```

Vérifier : `claude` puis « liste les sous-agents du projet » → les 7 agents de
`.claude/agents/` doivent apparaître.

## Les 5 jours avant

| Jour | Objectif | Porte |
| --- | --- | --- |
| **J-5 lun 16** | Lire les docs, appel équipe, rôles, repo GitHub public | Rôles + repo |
| **J-4 mar 17** | Choisir la stack, hello world déployé, CI verte | URL de preview |
| **J-3 mer 18** | Agents testés, worktree testé, `doctor.sh` | 7 agents, check.sh vert |
| **J-2 jeu 19** | Répétition générale de 2 h sur une idée bidon | La boucle tourne |
| **J-1 ven 20** | Shortlist de 3 idées, clés, quotas, valises, sommeil | `doctor.sh` sans échec |

Détail : [docs/CHECKLIST-AVANT.md](./docs/CHECKLIST-AVANT.md).

## Le jour J en 5 commandes

```bash
claude                                  # kickoff: /grill-with-docs, /to-spec, /to-tickets
./scripts/new-worktree.sh 12-login      # un worktree par ticket
cd ../wt-12-login && claude             # un agent par worktree (2–3 par humain max)
./scripts/check.sh --demo               # avant chaque PR
AFK_OK=1 ./scripts/afk-ralph.sh 4       # boucle bornée avant une sieste
```

## Carte du repo

| Chemin | Rôle |
| --- | --- |
| `CLAUDE.md` | Contrat lu par chaque agent : état, règles, commandes, définitions. |
| `AGENTS.md` | Point d'entrée pour les autres outils (Cursor, Codex…). |
| `docs/LAUZHACK.md` | Faits, règles, horaires, soumission, logistique. **À lire.** |
| `docs/STRATEGY.md` | Comment choisir l'idée, scorer, construire la démo de 3 min. |
| `docs/RESEARCH-HACKATHON.md` | Synthèse des sources : ce qui fait gagner, ce qui fait perdre. |
| `docs/PLAYBOOK.md` | Déroulé des 24 h, rôles, rituels, portes. |
| `docs/AGENT-OPS.md` | 3 humains + agents : concurrency, briefs, coûts, sécurité. |
| `docs/GLOSSARY.md` | Le langage commun ; à remplir à H0. |
| `docs/ARCHITECTURE.md` | Modules profonds, interfaces, tickets parallèles. |
| `docs/STACK.md` | Choisir la stack, bootstrap, déploiement, résilience de démo. |
| `docs/PRD.md` | La destination : ce que voit l'utilisateur. Écrit à H0. |
| `docs/DEMO.md` | Script de démo 3 min + plan B. Écrit **avant** le code. |
| `docs/DECISIONS.md` | Une ligne par décision durable. |
| `docs/QUESTIONS.md` | File de questions agents → humains. |
| `docs/IDEAS.md` | Idées hors périmètre. |
| `docs/RESEARCH.md` | Recherche externe mise en cache. |
| `docs/CHECKLIST-AVANT.md` | Checklist J-5 → J0. |
| `docs/AUDIT-2026-11-16.md` | Review de la base et corrections. |
| `progress.txt` | Synthèse tenue par l'intégrateur. |
| `progress/<ticket>.md` | Journal par ticket, sans conflit. |
| `.claude/agents/` | 7 subagents : chercheur, avocat, architecte, implémenteur, reviewer, QA, pitch. |
| `.claude/settings.json` | Permissions, hooks, limites de concurrence. |
| `scripts/check.sh` | Typecheck + lint + tests (+ e2e avec `--demo`). |
| `scripts/doctor.sh` | Readiness check. |
| `scripts/new-worktree.sh` | Worktree + branche + journal de ticket. |
| `scripts/ralph-once.sh` | Une itération surveillée. |
| `scripts/afk-ralph.sh` | Boucle bornée pour la nuit. |
| `.github/` | CI, modèle de ticket, modèle de PR. |

## Règles d'or

1. Un ticket = un worktree = une zone de fichiers.
2. `main` uniquement par PR, `check.sh` vert. H2 merge.
3. Squelette de démo bout en bout à **16:00** ; code gelé à **09:30** dimanche.
4. Les idées viennent des humains ; les agents questionnent, cherchent, exécutent.
5. Chaque erreur d'agent corrigée → une ligne dans `CLAUDE.md` (« Leçons apprises »).
6. La démo de 3 minutes est **sans slides** ; images/vidéos de secours autorisées.
7. La nouveauté doit être **explicite** : ce qu'on a créé vs ce qu'on a réutilisé.

## Sources

- [LauzHack 2026](https://lauzhack.com/) · [détails](https://lauzhack.com/details) · [règles](https://lauzhack.com/pdf/rules.pdf)
- [AI Hero — 7 phases](https://aihero.dev/my-7-phases-of-ai-development) · [skills](https://www.aihero.dev/skills) · [Ralph](https://www.aihero.dev/tips-for-ai-coding-with-ralph-wiggum)
- [Claude Code — subagents](https://code.claude.com/docs/en/sub-agents) · [worktrees](https://code.claude.com/docs/en/worktrees)
