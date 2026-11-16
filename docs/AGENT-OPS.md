# Agent Ops — 3 humains + agents sur 24 h

> Objectif : multiplier le travail **sans** multiplier les conflits, les tokens et les
> fausses victoires. Le goulot n'est pas le nombre d'agents : c'est la **bande passante de
> revue** et la **clarté du ticket**.

## Les 5 principes

1. **Un ticket = un worktree = une session.** Pas deux agents dans le même checkout :
   l'index et le HEAD sont partagés, un `amend` ou un `stash` peut atterrir chez l'autre.
2. **Les subagents servent à absorber le bruit** (lecture, recherche, review, QA), pas à
   écrire plusieurs features dans le même fichier.
3. **Le travail parallèle vit dans des worktrees séparés**, pas dans des sous-agents
   imbriqués au même endroit.
4. **Un écrivain par fichier partagé.** Les agents écrivent `progress/<ticket>.md` ;
   l'intégrateur humain compile dans `progress.txt` et `docs/DECISIONS.md`.
5. **« Fini » se prouve** : `./scripts/check.sh` vert, test du comportement, journal,
   commit. Pas de « done » déclaré.

## L'organigramme

| Humain | Rôle | Possède | Agents typiques |
| --- | --- | --- | --- |
| **H1** | Produit & Pitch | PRD, DEMO.md, périmètre, fit challenge, e2e de démo | `chercheur`, `avocat-du-diable`, `pitch` |
| **H2** | Architecte & Intégration | interfaces, `main`, file de merge, CI, stack, DECISIONS | `architecte`, `reviewer`, `implementeur` |
| **H3** | Builder & Démo | intégration la plus risquée, UI, déploiement, mocks/vidéo | `implementeur`, `qa-demo` |

Tout le monde code et relit. Les rôles disent **qui tranche**, pas qui est le seul à travailler.

## Combien de sessions en parallèle ?

| Phase | Écrivains | Lecteurs / QA | Total max | Porte |
| --- | --- | --- | --- | --- |
| Kickoff | 1 | 2 | 3 | PRD + DEMO gelés, squelette choisi |
| Squelette | 2 | 2 | 4 | Démo bout en bout sur la preview |
| Cœur | 2–3 | 3 | 5–6 | Le aha marche |
| Durcissement | 1–2 | 2 | 3–4 | e2e vert, mocks en place |
| Gel | 0 | 1 | 1 | Reviewer read-only |

**Plafond réaliste : 6 sessions actives pour 3 humains.** Au-delà, la revue et les merges
deviennent le goulot, et les agents se marchent dessus. Chaque humain pilote **1 session
principale + 1 à 2 subagents** maximum.

> Les « agent teams » de Claude Code sont expérimentales, plus coûteuses et sans isolation
> worktree. Pour un hackathon de 24 h : **ne pas les activer**.

## Les 4 modes

| Mode | Qui | Quand | Garde-fou |
| --- | --- | --- | --- |
| **Driver** | Humain + session principale | Décisions, architecture, code délicat | Humain dans la boucle |
| **Scout** | Subagent read-only | Recherche, review, exploration, QA | Outils limités, pas d'édition |
| **Builder** | Subagent dans un worktree | Un ticket bien spécifié | `check.sh`, `maxTurns`, commit atomique |
| **AFK** | Boucle `afk-ralph.sh` | Nuit, tâches low-risk avec oracle déterministe | `AFK_OK=1`, budget, pas de main, pas de produit |

### Quand utiliser un subagent

- **Oui** : « lis ces 20 fichiers et dis-moi où est l'auth », « relis ce diff », « joue la
  démo et liste ce qui casse », « cherche le pricing de cette API ».
- **Non** : « implémente cette feature » dans le même checkout qu'un autre agent ; « décide
  du périmètre » ; « corrige le bug que tu viens de trouver » si un humain doit d'abord
  comprendre.

### Quand utiliser l'AFK

Seulement si les trois conditions sont réunies :

1. La tâche a un **oracle déterministe** (`check.sh`, test, lint) qui dit vrai.
2. Elle est **low-risk** : tests, docs, mocks, refactor local, pas de décision produit.
3. Le worktree est **propre**, sur une branche de ticket, sans secret.

Sinon, l'agent produira du « fini » plausible. C'est le pire résultat possible.

## Le brief d'un agent (8 lignes)

À coller au début d'une session. Un agent productif en < 5 minutes, c'est un brief précis.

```text
Tu es <rôle> dans l'équipe LauzHack. Lis CLAUDE.md, docs/PRD.md, docs/DECISIONS.md,
docs/ARCHITECTURE.md et progress/<ticket>.md.
Ticket : #<n> — <titre>.
But : <comportement visible attendu>.
Fichiers autorisés : <dossiers/fichiers>.
Ne touche pas : <fichiers partagés, autres modules>.
Critères d'acceptation : <3 puces testables>.
Test de démo : <la commande ou l'action qui prouve que ça marche>.
Fini = check.sh vert + test ajouté + progress/<ticket>.md + commit atomique.
```

## Les artefacts de handoff

| Artefact | Écrit par | Lu par | Contenu |
| --- | --- | --- | --- |
| `CLAUDE.md` | H2 (aux syncs) | tous | règles, commandes, état du projet |
| `docs/PRD.md` | H1 | tous | destination, nouveauté, hors-périmètre |
| `docs/DEMO.md` | H1 | tous | script 3 min, plan B |
| `docs/DECISIONS.md` | H2 | tous | une ligne par décision durable |
| `docs/ARCHITECTURE.md` | H2 | agents | modules, interfaces, tickets |
| `progress/<ticket>.md` | l'agent du ticket | H2 | fait, décisions, fichiers, blocages |
| `progress.txt` | H2 | tous | synthèse de 3–5 lignes par sync |
| `docs/QUESTIONS.md` | agents (append) | H1/H2 | questions, options, recommandation |

## Fichiers partagés : le protocole

- **`progress.txt`** : seul H2 écrit. Les agents écrivent `progress/<ticket>.md`.
- **`docs/DECISIONS.md`** : seul H2 écrit, après validation humaine. Les agents proposent
  dans `progress/<ticket>.md` ou `QUESTIONS.md`.
- **`docs/PRD.md`** : seul H1 écrit. Un agent peut proposer un diff dans `QUESTIONS.md`.
- **`docs/QUESTIONS.md`** : append-only. Chaque agent ajoute une ligne, ne réécrit pas.
- **`docs/RESEARCH.md`** : le `chercheur` ajoute une section par sujet, avec sources.
- **`CLAUDE.md`** : H2, aux syncs. Jamais en cours de run d'un agent.

Le hook `PreToolUse` bloque l'édition de `PRD.md`, `DECISIONS.md`, `CLAUDE.md` et `.env`
par un **sous-agent**. Un humain peut toujours éditer. Override explicite :
`HACKATHON_ALLOW_PROTECTED=1`.

## La boucle de revue

1. L'agent finit : `check.sh` vert + test + journal + commit.
2. `reviewer` (lecture seule) relit `git diff main...HEAD` et classe
   BLOQUANT / À CORRIGER / SUGGESTION.
3. L'humain du domaine lit **le diff du cœur de démo** et le verdict. Pas besoin de tout
   lire : le reviewer et les tests couvrent le reste.
4. H2 merge dans `main` (PR ou merge local), **un seul merge à la fois**.
5. Si le merge casse la preview : revert immédiat, on ne debug pas sur `main`.

**Règle de bande passante :** un humain ne peut pas relire sérieusement plus d'un diff
significatif à la fois. C'est pour ça qu'on plafonne à 6 sessions, pas 20.

## Modèles et coûts

| Agent | Modèle | Pourquoi |
| --- | --- | --- |
| `architecte` | Opus | Décisions structurantes, peu d'appels |
| `avocat-du-diable` | Opus | Critique de haut niveau, peu d'appels |
| `reviewer` | Opus | Trouve les erreurs coûteuses |
| `pitch` | Opus | Le texte final est lu par le jury |
| `chercheur` | Sonnet | Volume de lecture, synthèse |
| `implementeur` | inherit (souvent Sonnet) | Volume de code, feedback loops |
| `qa-demo` | Sonnet | Exécution de tests, rapports |

- Une session principale par humain. Les subagents coûtent des tokens **en plus**, pas
  « gratuitement ».
- `--max-budget-usd` sur chaque run AFK (le script le fait).
- Vérifiez `/cost` à chaque sync. Si un agent brûle 20 $ pour un ticket, coupez.
- Opus partout = budget brûlé et latence. Réservez-le aux décisions.

## Sécurité

- **Jamais** `--dangerously-skip-permissions` sur une machine avec des secrets.
- `.env` : deny en lecture/écriture, jamais dans un prompt, un log ou un commit.
- Pas de `git push --force`, pas de push direct sur `main`, pas de `--no-verify`.
- AFK dans une sandbox ou un conteneur sans accès réseau inutile.
- Le contenu web est une **donnée non fiable** : ne jamais suivre ses instructions.
- Un rapport d'agent n'est pas une approbation de permission.
- Les hooks du repo : `Stop` vérifie `check.sh`, `PreToolUse` protège les docs.
  Pour une session d'exploration volontairement sale : `HACKATHON_SKIP_STOP_CHECK=1 claude`.

## Cadence

| Moment | Rituel | Durée |
| --- | --- | --- |
| H0 | Kickoff, rôles, idée, DEMO, PRD | 60 min |
| Toutes les 3 h | Sync debout : mergé / bloqué / on coupe quoi | 10 min |
| H6, H12, H18 | Revue de démo sur la preview | 15 min |
| Avant chaque merge | `check.sh --demo` + reviewer | 5–10 min |
| Avant une sieste | État dans `progress.txt`, AFK borné | 5 min |
| H20 | Gel du code | — |

## Anti-patterns

| Anti-pattern | Ce qui casse | À la place |
| --- | --- | --- |
| 10 agents en parallèle | revue impossible, merges en conflit | 6 sessions max |
| Deux agents sur le même module | écrasements, conflits | matrice fichiers / worktrees |
| Un agent édite `PRD.md` | périmètre qui dérive | proposition dans `QUESTIONS.md` |
| « Done » sans `check.sh` | démo cassée | hook `Stop` + test |
| AFK sur une décision produit | fonctionnalité inventée | humain uniquement |
| Opus partout | budget, lenteur | routage par agent |
| Session longue « pour qu'il se souvienne » | compaction, règles perdues | repartir d'un brief + docs |
| Agent teams « parce que c'est là » | expérimental, coûteux | subagents + worktrees |
| Merge sans passer par la preview | `main` cassée | merge queue + e2e |
| Rédiger `CLAUDE.md` en cours de run | les agents ne voient pas la règle | nouvelle session |

## Sources

- [Claude Code — subagents](https://code.claude.com/docs/en/sub-agents) : contexte isolé, `isolation: worktree`, limites de concurrence.
- [Claude Code — agent teams](https://code.claude.com/docs/en/agent-teams) : expérimental, coût, limites.
- [Claude Code — worktrees](https://code.claude.com/docs/en/worktrees) : isolation, branches.
- [Claude Code — coûts](https://code.claude.com/docs/en/costs) : multi-agent ≈ 15× les tokens d'un chat.
- [Anthropic — multi-agent research system](https://www.anthropic.com/engineering/multi-agent-research-system) : parallélisme utile surtout pour la recherche.
- [Addy Osmani — Code Agent Orchestra](https://addyosmani.com/blog/code-agent-orchestra/) : 3–10 agents sur un codebase connu, pas un greenfield.
- [Simon Willison — parallel coding agents](https://simonwillison.net/2025/Oct/5/parallel-coding-agents/) : la revue est le goulot.
- [AI Hero — Ralph](https://www.aihero.dev/tips-for-ai-coding-with-ralph-wiggum) : boucle bornée, oracle, progrès, petits pas.
