# Playbook — 24 h à 3 humains + agents

> Le déroulé opérationnel. Les faits de l'événement sont dans `docs/LAUZHACK.md`,
> la stratégie dans `docs/STRATEGY.md`, les agents dans `docs/AGENT-OPS.md`.

## Les rôles

| Rôle | Possède | Ses agents | Tranche seul sur |
| --- | --- | --- | --- |
| **H1 — Produit & Pitch** | PRD, DEMO.md, périmètre, fit challenge, test e2e | `chercheur`, `avocat-du-diable`, `pitch` | Périmètre, ordre des tickets, narrative |
| **H2 — Architecte & Intégration** | Interfaces, `main`, file de merge, CI, stack, DECISIONS | `architecte`, `reviewer`, `implementeur` | Structure du code, ce qui est mergé |
| **H3 — Builder & Démo** | UI, intégrations risquées, déploiement, mocks, vidéo | `implementeur`, `qa-demo` | Rendu visuel, plan B de démo |

Tout le monde code et relit. Les rôles disent **qui tranche**.

## Avant H0 (J-5 → J-1)

| Jour | Objectif | Porte |
| --- | --- | --- |
| J-5 (lun) | Lire `LAUZHACK.md` et `STRATEGY.md`, appel équipe 60 min, rôles, repo GitHub public | Rôles + repo créés |
| J-4 (mar) | Choisir la stack, bootstrap, hello world déployé, CI verte | URL de preview qui répond |
| J-3 (mer) | Installer Claude Code + skills, tester agents/worktree/AFK, `doctor.sh` | 7 agents visibles, check.sh vert |
| J-2 (jeu) | Répétition 2 h sur une idée bidon : idée → DEMO → PRD → 3 tickets → 2 agents → démo | La répétition tourne |
| J-1 (ven) | Shortlist de 3 idées, clés API, quotas, valises, sommeil | `doctor.sh` sans échec |

Détail : `docs/CHECKLIST-AVANT.md`.

## Le déroulé des 24 h

| Heure | Phase | Humains | Agents | Porte de sortie |
| --- | --- | --- | --- | --- |
| 8:00–9:30 | Check-in | ID, table, Wi-Fi, prises | — | Installés avant 9:30 |
| 10:20–11:40 | Tech talks | H1 note les challenges ; H2/H3 préparent le repo | `chercheur` en veille | Challenges compris |
| 11:40–12:00 | Team building | Choisir l'idée, la cible, la phrase de nouveauté | — | Idée + challenge choisis |
| **12:00–13:00** | **Kickoff** | DEMO.md, PRD, modules, tickets | `architecte` propose, `avocat` challenge | Démo écrite en 5 points, 8–12 tickets |
| 13:00–16:00 | Squelette | 2–3 builders, worktrees | `implementeur` | **Démo bout en bout sur la preview, même moche** |
| 16:00–19:00 | Cœur | Le aha d'abord | `implementeur`, `qa-demo` | Le aha est visible en < 30 s |
| 19:00–22:00 | Deuxième feature | Profondeur ou 2ᵉ cas d'usage | `implementeur`, `reviewer` | Démo complète |
| 22:00–23:00 | Gel v0.1 | Tag, vidéo de secours, soumission brouillon | `qa-demo` | v0.1 enregistrée |
| 23:00–07:00 | Nuit | Sommeil en shifts de 4 h, 2 éveillés | AFK borné, low-risk uniquement | Personne ne casse `main` |
| 07:00–09:30 | Intégration | Corriger, merger, répéter | `qa-demo`, `reviewer` | e2e vert |
| **09:30–10:30** | **Gel final** | Plus de feature, seulement des bugs | — | Code gelé |
| 10:30–11:30 | Soumission | Formulaire, README, vidéo | `pitch` | **Soumis avant 12:00** |
| 11:30–12:00 | Répétition | 5 passages de 3 min, Q&A | — | Tout le monde prêt |
| 12:30–15:00 | Judging | Démo 3 min + Q&A 1 min | — | Rester jusqu'à la clôture |

> Une porte non franchie à l'heure → on **coupe du périmètre**, on ne décale pas la suivante.
> Si l'intégration la plus risquée n'est pas démontrable à **19:00 samedi**, on la coupe.

## Kickoff H0 — minute par minute

1. **12:00–12:10** — Rôles, repo, board, canaux. Vérifier que `main` est protégée et que
   la preview répond.
2. **12:10–12:40** — 3 idées par humain, 2 min chacune, scoring
   (`docs/STRATEGY.md`). Garder 2, trancher.
3. **12:40–13:00** — Écrire `docs/DEMO.md` en 5 points. Si ce n'est pas écrivable,
   l'idée n'est pas prête : reprendre l'idée n°2.
4. **13:00–13:20** — `/to-spec` → `docs/PRD.md` (ou rédaction directe), puis
   `avocat-du-diable` sur le PRD. Couper ce qu'il signale.
5. **13:20–13:40** — `architecte` → modules + tickets. H2 valide, complète
   `ARCHITECTURE.md` et `CLAUDE.md` (commandes, état).
6. **13:40** — Premiers worktrees : `./scripts/new-worktree.sh <ticket>`, puis `claude`.
   Mode surveillé (`ralph-once.sh`) tant que le prompt n'est pas fiable.

## Skills AI Hero

Installer une fois par repo :

```bash
claude plugin install mattpocock-skills@claude-plugins-official
# ou, pour des fichiers éditables :
npx skills@latest add mattpocock/skills
```

Puis, dans une session : `/setup-matt-pocock-skills` (une fois par repo, configure
l'issue tracker, les labels et le dossier de docs).

| Moment | Skill | Usage |
| --- | --- | --- |
| Idée | `/grill-with-docs` (ou `/grill-me` si pressé) | Faire poser les questions, écrire le glossaire/ADR |
| Spec | `/to-spec` | Transformer la conversation en PRD |
| Tickets | `/to-tickets` | Découper en tranches avec dépendances |
| Implémentation | `/implement` + `/tdd` | TDD, une tranche à la fois |
| Revue | `/code-review` | Standards + spec, en parallèle |
| Debug | `/diagnosing-bugs` | Boucle de diagnostic |
| Rétro | `/retro` | Améliorer le repo après une session |

> Les anciens noms `/to-prd` et `/to-issues` ne sont plus les noms actuels. Utilisez
> `/to-spec` et `/to-tickets`.

### Mapping AI Hero → ce repo

| Phase AI Hero | Artefact | Où |
| --- | --- | --- |
| 1. Idée | 3 idées, scoring, phrase de nouveauté | `docs/STRATEGY.md`, `docs/IDEAS.md` |
| 2. Recherche | cache sourcé, API, pricing | `docs/RESEARCH.md`, `docs/RESEARCH-HACKATHON.md` |
| 3. Prototype | route jetable pour trancher l'UX | `src/app/prototype/` (à supprimer) |
| 4. PRD | destination, user stories, hors-périmètre | `docs/PRD.md` |
| 5. Kanban | tickets, dépendances, critique démo | GitHub Issues, `.github/ISSUE_TEMPLATE/ticket.md` |
| 6. Exécution | worktrees, Ralph, check.sh | `scripts/`, `.claude/agents/` |
| 7. QA | e2e, QA de démo, review | `docs/DEMO.md`, `scripts/demo-check.sh`, `qa-demo` |

Le principe AI Hero qui compte le plus ici : **modules profonds** (beaucoup de logique
derrière une interface simple) et **tickets en tranches verticales**. Voir
`docs/ARCHITECTURE.md`.

## Rituels

- **Sync toutes les 3 h** (10 min, debout) : mergé / bloqué / on coupe quoi.
  À 15:00, 18:00, 21:00, 00:00, 07:00.
- **Revue de démo à 16:00, 22:00 et 09:00** : on joue la démo sur la preview.
  Visez **2:45**, pas 3:00 ; enregistrez une vidéo de secours de 2 min.
- **Avant une sieste** (5 min) : état dans `progress.txt`, puis
  `AFK_OK=1 ./scripts/afk-ralph.sh 4`.
- **Désaccord** : un argument d'une phrase chacun, le propriétaire du domaine tranche,
  une ligne dans `DECISIONS.md`.
- **Après chaque correction d'agent** : une ligne dans `CLAUDE.md` (« Leçons apprises »)
  ou `/retro` si le problème est structurel.

## Sommeil

3 personnes, **4 h de sommeil chacune**, toujours 2 éveillées. Pas de all-nighter : la
démo finale se joue sur la lucidité, pas sur l'héroïsme.

| Créneau | Dort | Éveillés |
| --- | --- | --- |
| 22:00–02:00 | H3 | H1, H2 |
| 02:00–06:00 | H2 | H1, H3 |
| 06:00–10:00 | H1 | H2, H3 |

- H3 dort pendant le gel v0.1 : H1/H2 enregistrent la vidéo de secours.
- H2 dort 02:00–06:00 : H1/H3 font l'intégration de nuit ; H2 reprend le merge à 06:00.
- H1 dort 06:00–10:00 : H2/H3 font l'intégration du matin ; H1 revient pour la répétition.
- Micro-siestes de 20 min autorisées pour les éveillés.
- Personne ne conduit la démo finale avec moins de 4 h de sommeil.
- La zone de sommeil est accessible avec navettes ; prévoir 20 min de trajet.

## Communication

- Un canal (Discord/Slack), trois fils : `#decisions`, `#bloquants`, `#demo`.
- Les statuts vivent dans les GitHub Issues/Projects, pas dans le chat.
- `progress.txt` = synthèse ; `progress/<ticket>.md` = détail.
- Une question sans réponse → `docs/QUESTIONS.md`, priorité P0/P1/P2.

## Brief d'agent

Voir `docs/AGENT-OPS.md` (8 lignes + garde-fous). Résumé :

```text
Tu es <rôle>. Lis CLAUDE.md, docs/PRD.md, docs/DECISIONS.md, docs/ARCHITECTURE.md
et progress/<ticket>.md. Ticket #<n>. Fichiers autorisés : <...>.
Critères d'acceptation : <...>. Fini = check.sh vert + test + journal + commit.
```

## Pièges

| Piège | Garde-fou |
| --- | --- |
| Périmètre trop large | Squelette à 16:00, coupe à 19:00 |
| Trop d'agents | 6 sessions max, 1 principale + 1–2 subagents par humain |
| Agents qui s'écrasent | 1 ticket = 1 worktree = 1 zone de fichiers |
| Contexte saturé | Sessions courtes, brief, `progress/<ticket>.md` |
| Victoire déclarée trop tôt | `check.sh` + hook `Stop` + reviewer |
| Code inexplicable au jury | Chaque humain comprend le cœur différenciant |
| Démo fragile | Gel 09:30, mocks, vidéo, données seedées |
| Quota épuisé | `/cost`, `--max-budget-usd`, AFK borné |
| Soumission incomplète | Formulaire rempli à 23:00, soumis avant 12:00 |
| Slides | Interdites : images/vidéos de secours uniquement |

## Sources

- [LauzHack — détails](https://lauzhack.com/details)
- [LauzHack — règles](https://lauzhack.com/pdf/rules.pdf)
- [AI Hero — 7 phases](https://aihero.dev/my-7-phases-of-ai-development)
- [AI Hero — skills](https://www.aihero.dev/skills)
- [Claude Code — subagents](https://code.claude.com/docs/en/sub-agents)
