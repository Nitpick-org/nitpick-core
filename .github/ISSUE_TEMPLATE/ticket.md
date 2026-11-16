---
name: Ticket (tranche verticale)
about: Un comportement visible de bout en bout, faisable par un agent seul dans un worktree
labels: ticket
---

## Comportement visible

<Ce que l'utilisateur peut faire une fois ce ticket fini. Une phrase.>

## Critères d'acceptation

- [ ] …
- [ ] …
- [ ] …

## Module(s) / fichiers autorisés

- `src/modules/...`
- `tests/...`

## Ne pas toucher

- `docs/PRD.md`, `docs/DECISIONS.md`, `CLAUDE.md`, `.env`
- autres modules : …

## Dépendances

Bloqué par : #

## Risque

haut / moyen / bas

## Critique pour la démo

oui / non — si oui, `./scripts/check.sh --demo` doit passer.

## Challenge sponsor

aucun / <sponsor> — critère du sponsor : …

## Propriétaire humain

@

## Brief agent (8 lignes)

```text
Tu es <rôle>. Lis CLAUDE.md, docs/PRD.md, docs/DECISIONS.md, docs/ARCHITECTURE.md
et progress/<ticket>.md.
Ticket : #<n> — <titre>.
But : <comportement visible>.
Fichiers autorisés : <...>.
Critères d'acceptation : <...>.
Fini = check.sh vert + test + progress/<ticket>.md + commit atomique.
```
