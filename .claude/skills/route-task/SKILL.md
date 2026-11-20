---
name: route-task
description: Choisit l'agent et le modèle le moins cher pour une tâche, avec escalade sur preuve. À utiliser avant de déléguer à un subagent, quand une tâche bloque, ou pour trancher entre Haiku, Sonnet, Opus et Fable.
---
# Router une tâche

Tu reçois une tâche (ticket, doc, bug, recherche, review, QA). Tu choisis **le modèle le
moins cher qui peut la finir**, pas le plus puissant.

## Sortie attendue

```text
Route: <agent> / <model> / maxTurns <n>
Pourquoi: <1 phrase>
Escalader si: <2 conditions concrètes>
Budget: <tokens ou $ indicatif>
```

## Table de routage

| Tâche | Agent | Modèle | maxTurns |
| --- | --- | --- | --- |
| Doc, README, progress, résumé, changelog | `scribe` | `haiku` | 10–15 |
| Petit code, test, UI simple, fix local | `implementer` | `sonnet` | 20–30 |
| Code complexe/cross-module, auth/sécurité, ou critique démo **avec risque ≥ moyen** | `implementer` | `opus` | 40–50 |
| Recherche standard (API, pricing, concurrent) | `researcher` | `sonnet` | 20–30 |
| Recherche ambiguë, décision structurante | `researcher` | `opus` | 30 |
| Revue de code | `reviewer` | `opus` | 25 |
| Revue docs-only / diff trivial | `scribe` ou humain | `haiku` | 10 |
| QA standard, parcours de démo | `qa-demo` | `sonnet` | 25–30 |
| Bug bloquant, non reproductible, flaky | `qa-demo` | `opus` | 30 |
| Architecture, interfaces, tickets | `architect` | `opus` | 20 |
| Challenge du PRD / du plan | `devils-advocate` | `opus` | 20 |
| PRD, pitch, README de soumission | `pitch` (H1 décide) | `opus` | 20 |
| Décision à très fort enjeu (idée, modules, pitch final) | humain | `fable` manuel | — |

## Escalade

Escalade d'un tier (Haiku → Sonnet → Opus) si :

- 2 tentatives échouées sur le même point ;
- `./scripts/check.sh` rouge après 2 corrections ;
- ticket `critique démo` **et** risque `moyen`/`haut` (un critique démo à risque bas reste Sonnet) ;
- changement cross-module, auth, sécurité, données, contrat d'API ;
- l'agent dit explicitement « bloqué » ou « incertain » deux fois.

Journalise dans `progress/<ticket>.md` : `Model:`, `Escalations:`, `Why:`.

## Dé-escalade

Redescends si :

- la tâche est docs-only, du formatage, un renommage, des données de test, un résumé ;
- le diff fait < 50 lignes, un seul fichier, déjà couvert par un test.

## Règles dures

- **Fable n'est jamais automatique.** Un humain le choisit, hors AFK.
- **Un agent ne change pas son propre modèle en cours de run.** C'est l'orchestrateur qui
  le relance avec un modèle supérieur.
- **Pas de sous-agent pour une décision produit.** Les idées viennent des humains.
- **Pas de relecture du repo entier.** Cible les fichiers du ticket et `progress/<ticket>.md`.
- **Pas de boucle infinie.** Si le budget est dépassé, arrête et demande.
