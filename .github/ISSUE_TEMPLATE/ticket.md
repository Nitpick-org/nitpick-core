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

## Piste

A core partagé / B git + desktop / C Android + finitions (`docs/CONTEXT.md` §9)

## Fichiers autorisés

- `shared/src/<sourceSet>/kotlin/dev/nitpick/app/<package>/...`
- `shared/src/<sourceSet>Test/...` ou `androidApp/src/androidTest/...`

## Ne pas toucher

- Les fichiers réservés aux humains (table « Fichiers partagés » d'`AGENTS.md`), `.env`
- Les contrats du §6, sauf accord de H2
- Autres packages : …

## Dépendances

Bloqué par : #

## Risque

haut / moyen / bas

## Critique pour la démo

oui / non — si oui, `./scripts/check.sh --demo` passe et le parcours est rejoué à la main
sur desktop et Android.

## Challenge sponsor

aucun / <sponsor> — critère du sponsor : …

## Propriétaire humain

@

## Brief agent (8 lignes)

```text
Tu es <rôle>. Lis AGENTS.md, docs/CONTEXT.md, docs/DECISIONS.md et progress/<ticket>.md.
Ticket : #<n> — <titre>. Piste : <A|B|C>.
But : <comportement visible>.
Fichiers autorisés : <...>.
Critères d'acceptation : <...>.
Fini = check.sh vert + test + progress/<ticket>.md + commit atomique.
```
