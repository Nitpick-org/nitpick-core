# Ultra — exploiter les 2 abonnements Claude Ultra

> Deux membres de l'équipe ont un abonnement **Claude Ultra**. Ce document dit comment
> transformer cette capacité en travail utile **sans** casser les règles d'`AGENT-OPS.md` :
> Ultra lève la contrainte de tokens, pas la contrainte de **revue humaine**.

## Les faits (vérifiés le 16/11/2026)

| Fait | Conséquence pour nous |
| --- | --- |
| Ultra = **10× l'usage hebdomadaire de Pro**, **200 $/mois**. | Les comptes Ultra portent les escouades lourdes. |
| Compte **vérifié** = **pas de limite de 5 h**. Un compte non vérifié en a une temporaire. | Vérifier **avant** J-1 (voir plus bas), sinon on se bloque en pleine nuit. |
| Une **limite hebdomadaire** couvre tous les modèles, avec une **limite séparée pour Fable**. Fable peut utiliser jusqu'à **50 %** de la limite hebdo et consomme plus vite. | Fable est réservé aux décisions à fort enjeu (voir « Modèles »). |
| Claude Code **partage** les limites avec claude.ai. | Pas de longues discussions sur claude.ai pendant les 24 h : ça mange le budget de code. |
| Les limites se remettent à zéro **une fois par semaine** (date visible dans Settings > Usage). | Arriver à H0 avec un maximum de marge ; un reset pendant le hack est un bonus. |
| Si `ANTHROPIC_API_KEY` est définie dans le shell, Claude Code utilise **la clé API** au lieu de l'abonnement. | Facturation API par erreur. La variable est renommée `APP_ANTHROPIC_API_KEY` ; `doctor.sh`, `ralph-once.sh` et `afk-ralph.sh` refusent de tourner si elle est présente. |
| Ultra inclut **200 $/mois de crédits API** (Console). | Les utiliser pour les appels IA **de l'application** (clé dans `.env` sous `APP_ANTHROPIC_API_KEY`) ou l'Agent SDK avec clé API, **pas** pour Claude Code interactif. |

## Qui pilote quoi

| Humain | Plan | Rôle (`AGENT-OPS.md`) | Charge agents |
| --- | --- | --- | --- |
| **H1** | Pro / autre | Produit & Pitch | 1 session principale + 1 scout. Peu de tokens, beaucoup de décisions. |
| **H2** | **Ultra** | Architecte & Intégration | 1 principale + 2 parallèles (builder en worktree + `reviewer`). AFK sur son shift de sommeil. |
| **H3** | **Ultra** | Builder & Démo | 1 principale + 2–3 parallèles (builders en worktrees + `qa-demo`). AFK sur son shift de sommeil. |

> Hypothèse : H1 n'est pas sur Ultra. Si c'est faux, rééquilibrer : H1 prend les
> `researcher` / `devils-advocate` en parallèle et un AFK de nuit.

**Règle d'or :** les tâches **gourmandes en tokens** (recherche large, gros refactor, AFK,
QA répétée) partent sur un compte Ultra. Les tâches **gourmandes en jugement** (PRD, périmètre,
pitch) restent chez l'humain concerné, quel que soit son plan.

## Concurrence : ce qui change, ce qui ne change pas

- **Ne change pas :** un humain relit sérieusement **un** diff significatif à la fois.
  Le plafond d'écrivains reste celui d'`AGENT-OPS.md`.
- **Change :** on peut ajouter des **lecteurs** (scouts read-only, QA, recherche) sans peur
  du coût. Plafond total porté de 6 à **8 sessions**, dont au plus 3 écrivains.

| Phase | Écrivains | Lecteurs / QA | Total max | Sur Ultra |
| --- | --- | --- | --- | --- |
| Kickoff | 1 | 3 | 4 | `researcher` ×2 en parallèle (concurrents, API) |
| Squelette | 2 | 2 | 4 | les 2 builders |
| Cœur | 3 | 4–5 | 8 | builders + `reviewer` + `qa-demo` en continu |
| Nuit | AFK ×2 | 0–1 | 3 | 1 AFK par compte Ultra, sur son shift de sommeil |
| Durcissement | 1–2 | 3 | 5 | `qa-demo` en boucle sur `DEMO.md` |
| Gel | 0 | 1–2 | 2 | `reviewer` + `qa-demo` |

**Agent teams :** toujours désactivées. Ultra supprime l'argument du coût, pas celui de
l'isolation (pas de worktree par coéquipier) ni de la revue.

## Budget hebdomadaire (par compte Ultra)

Objectif : ne **jamais** atteindre la limite avant dimanche 12:00. Lire le % consommé avec
`/usage` dans Claude Code (ou Settings > Usage) **à chaque sync de 3 h**.

| Bloc | Heures | Budget cumulé max | Si dépassé |
| --- | --- | --- | --- |
| Avant H0 (J-5 → J-1) | — | **≤ 25 %** de la semaine | Plus de tests d'agents jusqu'à H0 |
| Kickoff + squelette | 12:00–16:00 | ≤ 40 % | Passer les scouts en Sonnet |
| Cœur + 2ᵉ feature | 16:00–23:00 | ≤ 65 % | Plus de scouts parallèles, 1 builder max |
| Nuit (AFK) | 00:00–07:00 | ≤ 80 % | AFK limité à 2 itérations |
| Intégration + gel | 07:00–12:00 | ≤ 95 % | Uniquement correctifs bloquants |

Si un compte Ultra est presque à sec, l'autre reprend ses tickets. Si les deux le sont,
on bascule **les sessions** sur le compte de H1, jamais sur une clé API dans le shell.

> Après la limite hebdomadaire, Claude peut continuer en **usage payant** si c'est activé
> sur le compte. Décidez à J-1 si vous l'autorisez, et avec quel plafond (voir Q-004).

## Modèles

| Usage | Modèle | Pourquoi |
| --- | --- | --- |
| Sessions principales H2/H3 | Opus | Ultra le permet ; meilleure qualité de code et moins d'allers-retours |
| `researcher`, `qa-demo` | Sonnet | volume, lecture, exécution de tests |
| `architect`, `devils-advocate`, `reviewer`, `pitch` | Opus | inchangé (voir les agents) |
| **Fable** | à la main, via `/model` | **seulement** : choix d'idée à H0, découpage en modules, bug bloquant > 30 min, relecture finale du pitch. Limite séparée et consommation rapide : ne jamais le mettre en défaut ni dans un AFK. |

## AFK de nuit avec Ultra

Le compte qui lance `afk-ralph.sh` est **celui connecté sur la machine** qui le lance.

1. Avant son shift de sommeil, l'humain Ultra choisit 1 ticket **low-risk** avec oracle
   (`AGENT-OPS.md` → « Quand utiliser l'AFK »).
2. `./scripts/new-worktree.sh <ticket>` puis, dans le worktree :
   `AFK_OK=1 ./scripts/afk-ralph.sh 6`.
3. Machine branchée, veille désactivée (`caffeinate -dimsu` sur macOS), Wi-Fi stable.
4. Au réveil : `git log`, `progress/<ticket>.md`, `check.sh --demo`, puis PR.

Plafonds par défaut des scripts (surchargeables) : `AFK_MAX_TURNS=60` et
`AFK_BUDGET_USD=4` par itération ; `RALPH_MAX_TURNS=40` et `RALPH_BUDGET_USD=3` pour
`ralph-once.sh`. Le budget en $ est une **estimation** côté abonnement :
c'est un garde-fou contre la boucle folle, pas une facture.

## À faire avant H0 (chaque compte Ultra)

- [ ] **Compte vérifié :** Settings > Usage. Pas de barre « Current session » = vérifié.
- [ ] **Jour et heure du reset hebdomadaire** notés dans `docs/DECISIONS.md`.
- [ ] `/usage` à J-1 au soir : **≥ 75 %** de la semaine disponible.
- [ ] `echo $ANTHROPIC_API_KEY` vide dans le shell (et pas dans `~/.zshrc`).
- [ ] `claude` connecté avec le compte Ultra (`/status`).
- [ ] Décision sur l'usage payant après limite (oui/non, plafond).
- [ ] Crédits API Ultra : créer une clé pour l'app si on en a besoin, la mettre dans `.env`
      sous `APP_ANTHROPIC_API_KEY`.

## Sources

- [Claude Help Center — plan Ultra](https://support.claude.com/en/articles/17728952)
- [Claude Help Center — crédits API mensuels](https://support.claude.com/en/articles/17154008-monthly-api-credits-for-max-ultra-and-team-plans)
- [Claude Help Center — Fable sur votre plan](https://support.claude.com/en/articles/15424964-claude-fable-models-on-your-plan)
- [Claude Help Center — Claude Code avec un plan](https://support.claude.com/en/articles/11145838-use-claude-code-with-your-pro-max-or-ultra-plan)
- [Claude Monitor — limites Ultra](https://claude-monitor.com/claude-ultra-plan-usage-limits)
- [Claude Code — coûts](https://code.claude.com/docs/en/costs)
