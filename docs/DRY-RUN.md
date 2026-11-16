# Dry run — tester la base comme en conditions réelles

> Un mini-projet fictif, **2 h 30**, dans un **clone jetable**. Le but n'est pas de livrer
> Pulse : c'est de découvrir ce qui casse dans le repo, les agents et le rythme avant le
> vrai hackathon.

## Ce que ça teste

- L'onboarding : clone, hooks, `.env`, Claude Code, Ultra, skills.
- Le kickoff : DEMO, PRD, modules, tickets.
- Les worktrees, `check.sh`, `check.sh --demo`, les agents, la revue, le merge.
- La démo de 3 minutes et le Q&A.
- La capacité Ultra : 2 comptes, 8 sessions max, 3 écrivains.

## Ce que ça ne teste pas

- La vraie stack (le starter est un Vite + React jetable).
- Le déploiement, la base de données, les APIs sponsors.
- La nuit, le sommeil, les 24 h complètes.

## Règles

1. **Clone jetable** : `git clone <repo> /tmp/lauzhack-dry-run`. Ne fais pas ça dans le repo
   partagé.
2. **Aucun secret réel** : pas de clé API, pas de donnée personnelle.
3. **Ne merge jamais `dry-run/base` dans `main`.**
4. **Timebox** : 2 h 30. Si une étape dépasse, note-le et passe à la suivante.
5. **Note les frictions** : chaque blocage de plus de 5 minutes va dans `dry-run/RETRO.md`.

## Setup (10 minutes)

```bash
git clone git@github.com:zooldyyk/hackathon.git /tmp/lauzhack-dry-run
cd /tmp/lauzhack-dry-run
git config core.hooksPath .githooks
cp .env.example .env
unset ANTHROPIC_API_KEY

./scripts/bootstrap-dry-run.sh
pnpm exec playwright install chromium
./scripts/check.sh
./scripts/check.sh --demo
```

- `bootstrap-dry-run.sh` crée `dry-run/base`, copie le starter, installe les dépendances et
  commit.
- `playwright install chromium` est nécessaire pour `--demo` (une fois).
- Si `check.sh --demo` échoue, note-le : c'est peut-être un vrai bug de base.
- Le starter installe avec `--ignore-scripts` (connu, jetable). Si tu vois quand même un
  warning `Ignored build scripts: esbuild`, `pnpm rebuild esbuild` suffit ; le build passe.
- Si l'installation des navigateurs Playwright échoue (cache en lecture seule), utilise
  `PLAYWRIGHT_BROWSERS_PATH="$PWD/.playwright-browsers" pnpm exec playwright install chromium`
  puis la même variable devant `./scripts/check.sh --demo`.

## Rôles

| Humain | Rôle | Agents | Compte |
| --- | --- | --- | --- |
| H1 | Produit & Pitch : DEMO, PRD, script, Q&A | `devils-advocate`, `pitch` | Pro / autre |
| H2 | Architecte & Intégration : modules, merge, CI | `architect`, `reviewer`, `implementer` | Ultra |
| H3 | Builder & Démo : UI, e2e, démo | `implementer`, `qa-demo` | Ultra |

## Timeline

| Temps | Qui | Action | Porte |
| --- | --- | --- | --- |
| 0:00–0:10 | tous | Onboarding : clone, hooks, `.env`, `doctor.sh` | Un seul échec : `no package.json` avant bootstrap |
| 0:10–0:20 | tous | `bootstrap-dry-run.sh`, `check.sh`, `check.sh --demo` | Check vert |
| 0:20–0:35 | H1 | Lire `dry-run/TICKETS.md`, écrire `docs/DEMO.md` (Pulse) et `docs/PRD.md` | Démo en 5 points |
| 0:20–0:35 | H2/H3 | Lire l'architecture, créer les worktrees | 2 worktrees prêts |
| 0:35–0:50 | H2 | `architect` sur le PRD, puis `implementer` ticket #01 | Module + tests |
| 0:35–0:50 | H3 | `qa-demo` sur la baseline, prépare le test e2e | Le parcours de démo est clair |
| 0:50–1:35 | H2/H3 | Ticket #02 UI, puis #03 e2e/demo ; `reviewer` sur chaque diff | 3 tickets prêts |
| 1:35–1:55 | H2 | Merge dans `dry-run/base`, `check.sh --demo` | Tout vert |
| 1:55–2:10 | tous | Démo 3 min ×2 + Q&A 1 min | La démo tient en 2:45 |
| 2:10–2:25 | tous | Remplir `dry-run/RETRO.md` | 3 correctifs concrets |
| 2:25–2:30 | tous | Nettoyage, go/no-go | Décision écrite |

## Commandes utiles

```bash
# Worktree pour un ticket, basé sur la branche du dry run
WT_BASE="$PWD/.worktrees" WT_FROM=dry-run/base ./scripts/new-worktree.sh 01-anomaly
cd .worktrees/wt-01-anomaly
pnpm install --ignore-scripts
claude

# Vérifier le diff par rapport à la base du dry run
git diff dry-run/base...HEAD

# Après merge dans le clone principal
./scripts/check.sh --demo

# Nettoyage
WT_BASE="$PWD/.worktrees" ./scripts/rm-worktree.sh 01-anomaly --force
git switch main
git branch -D dry-run/base
rm -rf .worktrees
```

## Ce qu'on observe

| Question | Pourquoi |
| --- | --- |
| Combien de temps pour le premier `check.sh` vert ? | La base est-elle vraiment prête ? |
| Un agent neuf est-il productif en < 5 min ? | Le brief et les docs suffisent-ils ? |
| Combien de conflits de merge ? | Les zones de fichiers sont-elles respectées ? |
| Un agent a-t-il déclaré « fini » à tort ? | Le hook `Stop` et les tests marchent-ils ? |
| Le reviewer trouve-t-il un vrai problème ? | La revue est-elle utile ou théâtrale ? |
| La démo tient-elle en 2:45 sans slides ? | Le format est-il compris ? |
| Où sont les 10 minutes perdues ? | C'est le vrai gain à corriger. |

## Routage (à tester)

Avant chaque délégation, utilise `/route-task` et note la décision :

- doc → `scribe` / Haiku (Sonnet si le texte doit être nuancé) ;
- code standard → `implementer` / Sonnet ;
- code critique → `implementer` / Opus ;
- review code → `reviewer` / Opus ;
- pitch/PRD → `pitch` / Opus.

Si un ticket échoue 2 fois, relance le **même agent** avec le modèle supérieur et note
`Escalations:` dans `progress/<ticket>.md`. Fable reste manuel.

## Ultra

- H2 et H3 travaillent sur les comptes Ultra.
- **8 sessions max, 3 écrivains max.** Les autres sont des lecteurs/QA.
- `/usage` au début et à la fin. Note le pourcentage consommé.
- **Pas de Fable** pour ce dry run, sauf blocage > 30 min. Si tu l'utilises, note-le.
- Si tu as le temps, teste **un** AFK d'une itération sur un ticket low-risk :
  `AFK_OK=1 ./scripts/afk-ralph.sh 1` dans un worktree.

## Critères de succès

- [ ] `./scripts/check.sh` vert au root et dans les worktrees.
- [ ] `./scripts/check.sh --demo` vert.
- [ ] Les 3 tickets sont mergés dans `dry-run/base`.
- [ ] Au moins 4 agents utilisés (`architect`, `implementer`, `reviewer`, `qa-demo`).
- [ ] Un doc a été écrit par `scribe` (Haiku), pas par Opus.
- [ ] Une escalade a été journalisée si un ticket a échoué 2 fois.
- [ ] La démo de 3 minutes passe 2 fois, sans slides.
- [ ] `dry-run/RETRO.md` contient 3 correctifs concrets.
- [ ] Aucun secret n'a été commité.

## Après le dry run

1. Remplir `dry-run/RETRO.md` à chaud.
2. Créer 3 tickets dans le vrai repo (GitHub Issues, label `ticket`) pour les 3 correctifs.
3. Les corriger avant H0, avec `check.sh` vert.
4. Supprimer le clone jetable.

> Si le dry run échoue, c'est une réussite : tu viens de trouver un problème avant le
> hackathon, pas pendant la démo.
