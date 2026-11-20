# Model routing — dépenser les tokens au bon endroit

> Objectif : sur 24 h, avec 2 comptes Ultra, utiliser **le modèle le moins cher qui peut
> finir la tâche**. On ne monte en puissance que sur preuve. On ne descend jamais en
> qualité sur une décision à fort enjeu.

## Les 4 tiers

| Tier | Modèle | Pour | À éviter |
| --- | --- | --- | --- |
| **T0** | Haiku | docs, progress, résumés, changelog, données de test, messages de commit | architecture, code complexe, pitch |
| **T1** | Sonnet | code standard, tests, UI, recherche, QA, docs nuancées | refactor risqué, auth/sécurité, review finale |
| **T2** | Opus | architecture, review, code critique, pitch, avocat du diable, debug bloquant | docs-only, formatage, renommage |
| **T3** | Fable | **manuel uniquement** : idée, modules, pitch final, bug > 30 min | tout le reste, et jamais en AFK |

> Fable a une limite hebdomadaire séparée et consomme plus vite. Il ne doit jamais être
> utilisé par un agent en automatique.

## Table de routage

| Tâche | Agent | Modèle | maxTurns |
| --- | --- | --- | --- |
| Doc factuelle, README, progress, résumé | `scribe` | Haiku | 10–15 |
| Doc publique nuancée (onboarding, guide) | `scribe` | Sonnet | 15–20 |
| Petit code, test, UI simple | `implementer` | Sonnet | 20–30 |
| Code complexe/cross-module, auth/sécurité, ou critique démo avec risque ≥ moyen | `implementer` | Opus | 40–50 |
| Recherche standard | `researcher` | Sonnet | 20–30 |
| Recherche ambiguë / structurante | `researcher` | Opus | 30 |
| Review de code | `reviewer` | Opus | 25 |
| Review docs-only | `scribe` / humain | Haiku | 10 |
| QA standard | `qa-demo` | Sonnet | 25–30 |
| Bug bloquant / flaky | `qa-demo` | Opus | 30 |
| Architecture / tickets | `architect` | Opus | 20 |
| Challenge PRD | `devils-advocate` | Opus | 20 |
| Pitch / PRD | `pitch` (H1 décide) | Opus | 20 |

## Escalade : quand monter

Un tier au-dessus si :

- 2 tentatives échouées sur le même point ;
- `./scripts/check.sh` rouge après 2 corrections ;
- ticket `critique démo` **et** risque `moyen`/`haut` (un critique démo à risque bas reste Sonnet) ;
- changement cross-module, auth, sécurité, données, contrat d'API ;
- l'agent dit « bloqué » ou « incertain » deux fois.

**Haiku → Sonnet** : la doc devient un guide public, ou le résumé doit être exact.
**Sonnet → Opus** : le code touche plusieurs modules, la démo, l'auth, ou les tests
échouent sans explication.
**Opus → Fable** : jamais automatique. Un humain décide, hors AFK.

## Dé-escalade : quand descendre

- docs-only, formatage, renommage, données de test, résumé ;
- diff < 50 lignes, un seul fichier, déjà couvert par un test ;
- tâche répétitive (10e ticket du même type) avec un pattern connu.

## Comment override dans Claude Code

Le modèle se choisit **au moment d'invoquer l'agent**, pas dans son prompt :

```text
Utilise implementer avec le modèle sonnet pour le ticket #12.
Utilise scribe pour mettre à jour README.md.
Utilise researcher avec le modèle opus pour trancher l'API sponsor.
Utilise reviewer sur le diff.
```

- `haiku`, `sonnet`, `opus`, `fable` sont des alias acceptés.
- Le modèle par défaut de l'agent reste dans son frontmatter ; l'override ponctuel ne le
  change pas.
- Un agent **ne peut pas changer son propre modèle en cours de run**. S'il demande une
  escalade, l'orchestrateur le relance avec le modèle supérieur (nouvelle invocation,
  même ticket).

## Budgets indicatifs

| Tier | Tokens par tâche | Signal d'alerte |
| --- | --- | --- |
| T0 | < 20 k | un doc qui prend 50 k = mauvais prompt |
| T1 | < 100 k | 2 échecs = escalade, pas 5 essais |
| T2 | < 250 k | au-delà, découper le ticket |
| T3 | manuel | jamais en AFK |

Le budget hebdomadaire des comptes Ultra est dans `docs/ULTRA.md`. Vérifie `/usage` à
chaque sync.

### AFK

`ralph-once.sh` et `afk-ralph.sh` forcent un modèle explicite : `RALPH_MODEL` /
`AFK_MODEL`, défaut **Sonnet**. Fable est refusé. Sans ce garde-fou, un `claude -p` sans
`--model` prend le modèle par défaut de la machine — et un AFK peut brûler le budget en
quelques secondes.

## Journalisation

Dans `progress/<ticket>.md` :

```md
- Model: sonnet
- Escalations: sonnet → opus (2 échecs sur le parsing CSV)
- Why: le ticket est critique démo et le test e2e échoue
```

À la rétro, si un doc a consommé Opus, c'est un bug de routage.

## Automatisation

- **Skill `/route-task`** : classifie la tâche et rend une décision
  `agent / model / maxTurns / escalade`. Claude peut l'invoquer seul quand la description
  correspond.
- **CLAUDE.md** : règle « classer avant de déléguer, commencer bas, escalader sur preuve ».
- **`scribe`** : agent docs-only sur Haiku. C'est le défaut pour README, progress, résumés.
- **`Explore` / `Plan`** : reconnaissance codebase read-only, pas un agent custom.
- **`Stop` + `check.sh`** : empêchent de déclarer « fini » trop tôt.

## Exemples

| Demande | Route |
| --- | --- |
| « Mets à jour le README avec le dry run » | `scribe` / Haiku / 10 |
| « Réécris l'onboarding pour qu'il soit limpide » | `scribe` / Sonnet / 20 |
| « Ajoute le parser CSV + tests » | `implementer` / Sonnet / 25 ; Opus si 2 échecs |
| « Pourquoi le test e2e est flaky ? » | `qa-demo` / Sonnet / 20 ; Opus si non reproductible |
| « Review la PR auth » | `reviewer` / Opus / 25 |
| « Trouve le pricing de l'API sponsor » | `researcher` / Sonnet / 20 |
| « Découpe le PRD en modules » | `architect` / Opus / 20 |
| « Écris le pitch de 2:45 » | `pitch` / Opus / 20 ; Fable manuel si bloqué |

## Anti-patterns

| Anti-pattern | Coût | Correction |
| --- | --- | --- |
| Opus pour un README | 5–10× le coût | `scribe` / Haiku |
| Sonnet pour une review sécurité | risque | `reviewer` / Opus |
| Fable par défaut | limite séparée vidée | manuel, décisions clés |
| 5 essais Sonnet au lieu d'une escalade | temps + tokens | 2 échecs → Opus |
| Re-lire tout le repo à chaque tour | contexte saturé | cibler ticket + `progress/` |
| Sous-agent pour une décision produit | dérive | humain |
| Agent qui s'auto-attribue Opus | pas de contrôle | orchestrateur décide |

## Sources

- [Claude Code — subagents](https://code.claude.com/docs/en/sub-agents) : modèle par agent, `model: inherit`, override par invocation.
- [Claude Code — modèles](https://code.claude.com/docs/en/model-config) : alias et effort.
- [Claude Code — coûts](https://code.claude.com/docs/en/costs) : multi-agent ≈ 15× un chat.
- [AI Hero — Ralph](https://www.aihero.dev/tips-for-ai-coding-with-ralph-wiggum) : petits pas, feedback loops.
