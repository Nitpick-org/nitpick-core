@AGENTS.md

# Claude Code — en plus d'AGENTS.md

Les règles de l'équipe sont dans `AGENTS.md`, importé ci-dessus. Ce fichier ne couvre que
les sous-agents, le routage des modèles, les hooks, l'AFK et la capacité Ultra.

## Sous-agents

| Besoin | Agent | Modèle par défaut |
| --- | --- | --- |
| Doc factuelle, README, progress, résumé | `scribe` | Haiku |
| Implémenter un ticket en TDD | `implementer` | Sonnet (Opus si critique démo à risque ≥ moyen) |
| Recherche sourcée (Koog, JGit, Compose, jury) | `researcher` | Sonnet |
| Rejouer la démo, chercher ce qui casse | `qa-demo` | Sonnet |
| Relire un diff avant merge | `reviewer` | Opus |
| Découper en packages et tickets | `architect` | Opus |
| Challenger le scope ou un plan | `devils-advocate` | Opus |
| Script de démo, README de soumission, Q&A | `pitch` | Opus |

- **Route avant de déléguer** : `/route-task`, détail dans `docs/MODEL-ROUTING.md`. Commence
  bas ; deux échecs sur le même point → relance le même agent avec le modèle au-dessus.
  Fable se choisit à la main (`/model`), pour une décision à fort enjeu.
- La reconnaissance du code passe par les agents intégrés `Explore` et `Plan` (lecture seule).
- Les sous-agents absorbent le bruit (lecture, recherche, review, QA) ; l'écriture de code
  se fait dans un worktree par ticket. Plafond : 8 sessions, dont 3 écrivains de code
  (`docs/AGENT-OPS.md`, `docs/ULTRA.md`). Les agent teams restent désactivées.
- Un rapport de sous-agent est une proposition : l'humain du domaine décide ce qu'on garde,
  et chaque rejet ou correction va dans `docs/REVIEW_LOG.md`.

## Hooks

- **`Stop`** (`scripts/stop-check.sh`) : tant que `check.sh` est rouge sur un arbre modifié,
  la session ne peut pas s'arrêter. Exploration volontairement sale :
  `HACKATHON_SKIP_STOP_CHECK=1 claude`.
- **`PreToolUse` Edit/Write** (`scripts/guard-protected.sh`) : un sous-agent ou une session
  headless n'édite pas `AGENTS.md`, `CLAUDE.md`, `docs/CONTEXT.md`, `docs/REVIEW_LOG.md`,
  `docs/DECISIONS.md`, `docs/PRD.md`, `docs/STRATEGY.md`, `docs/AGENT-OPS.md`,
  `docs/ULTRA.md`, `docs/MODEL-ROUTING.md`, `progress.txt`, `.env`. Override explicite :
  `HACKATHON_ALLOW_PROTECTED=1`.
- **`PreToolUse` Bash** (`scripts/guard-bash.sh`) : bloque tout accès à `.env` et toute
  commande d'écriture (`>`, `sed -i`, `mv`, `python`…) qui nomme un fichier protégé, **même
  dans la session humaine**. Pour les lire, utilise Read ; pour les modifier, Edit ou Write.
- **`pre-commit`** (`.githooks/pre-commit`) : lance `check.sh` et refuse un `.env` indexé.

## AFK

`AFK_OK=1 ./scripts/afk-ralph.sh 4` dans un worktree de ticket, seulement si la tâche a un
oracle déterministe (`check.sh`, un test), qu'elle est low-risk (tests, docs, refactor
local, aucune décision produit) et que le worktree est propre. Ouvre `claude` une fois dans
le worktree avant (trust dialog). Modèle Sonnet par défaut ; les scripts refusent Fable.
Détail : `docs/AGENT-OPS.md`, `docs/ULTRA.md`.

## Capacité et budget

- H2 et H3 sont sur Ultra : la recherche large, la QA répétée et l'AFK partent chez eux ;
  les décisions restent chez leur propriétaire.
- `/usage` à chaque sync, comparé aux paliers de `docs/ULTRA.md`.
- Claude Code tourne sur l'abonnement : `doctor.sh`, `ralph-once.sh` et `afk-ralph.sh`
  refusent de tourner si `ANTHROPIC_API_KEY` est définie.

## Agent skills

### Issue tracker

GitHub Issues on `zooldyyk/hackathon`, via `gh`. See `docs/agents/issue-tracker.md`.

### Triage labels

The five default roles (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `docs/GLOSSARY.md` + `docs/DECISIONS.md`. See `docs/agents/domain.md`.
