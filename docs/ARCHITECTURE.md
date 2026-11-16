# Architecture — modules profonds et tickets parallèles

> But : que 3 humains et 6 agents puissent travailler sans se marcher dessus, et qu'un
> humain puisse expliquer le cœur au jury en 2 phrases.

## Principes

1. **Modules profonds** : beaucoup de comportement derrière une interface simple.
   Pas 30 petits fichiers interconnectés.
2. **5 à 8 modules maximum.** Un module = une responsabilité nommable.
3. **`src/modules/<nom>/index.ts` est la seule interface publique.** Personne n'importe
   `src/modules/<nom>/internal/**` depuis l'extérieur.
4. **Les tests verrouillent le comportement à l'interface**, pas l'implémentation.
5. **Une dépendance va dans un sens.** Pas de cycle entre modules.
6. **L'UI compose les modules**, elle ne contient pas la logique métier.

## Arborescence cible

```text
src/
  modules/
    <module>/
      index.ts          # interface publique : types + fonctions/classes exportées
      <module>.test.ts  # tests de l'interface
      internal/         # implémentation privée (jamais importée dehors)
  app/                  # routes / pages / composition UI
  lib/                  # infra partagée : env, db, http, llm
  components/           # UI réutilisable, sans logique métier
```

## Contrat d'interface

```ts
// src/modules/example/index.ts
export type ExampleInput = { /* ... */ };
export type ExampleResult = { /* ... */ };

export interface ExampleModule {
  run(input: ExampleInput): Promise<ExampleResult>;
}

export function createExampleModule(deps: {
  db: Database;
  llm: LlmClient;
}): ExampleModule;
```

- L'interface est **petite** : 1 à 3 fonctions publiques.
- Les dépendances externes sont injectées, pas importées en dur.
- Les erreurs sont explicites (`Result` ou exceptions typées), pas des `any`.
- Un mock de l'interface doit suffire pour tester l'UI.

## Carte des modules (à remplir à H0)

| Module | Responsabilité (1 phrase) | Interface (fonctions) | Propriétaire | Critique démo |
| --- | --- | --- | --- | --- |
| `capture` | Prend une entrée utilisateur | `start()`, `stop()` | H3 | oui |
| `analysis` | Transforme l'entrée en résultat | `analyze(input)` | H2 | oui |
| `render` | Affiche le résultat | `render(result)` | H3 | oui |
| `persistence` | Sauvegarde/rejoue une démo | `save()`, `load()` | H2 | non |
| `sponsor` | Encapsule l'API sponsor | `call()` + mock | H2 | oui |
| `demo` | Seed, reset, mode mock | `seed()`, `reset()` | H1 | oui |

Règles :

- 5 à 8 lignes. Si vous en avez 12, fusionnez.
- Un module peut être **mocké** sans changer l'UI.
- Le module `demo` est un vrai module : c'est lui qui sauve la démo.

## Tickets en tranches verticales

Un ticket = **un comportement visible de bout en bout**, pas une couche technique.

```md
## #12 — L'utilisateur voit le résultat en < 2 s

**Comportement visible** : ...
**Critères d'acceptation** :
- [ ] ...
- [ ] ...
**Modules / fichiers autorisés** : src/modules/analysis/**, src/app/result/**
**Dépendances** : #10
**Risque** : haut / moyen / bas
**Critique démo** : oui / non
**Propriétaire** : H2
**Brief agent** : ...
```

### Découper

- D'abord le **chemin de démo** (le aha).
- Ensuite les **cas limites** qui menacent la démo (API down, données vides).
- Enfin le polish.

### Ne pas découper

- « Faire l'API » puis « faire l'UI » : ce sont des couches, pas des tranches.
- « Refactorer le module » sans comportement visible : à faire seulement si ça débloque
  un ticket de démo.
- « Ajouter une feature » qui n'est pas dans le PRD : `docs/IDEAS.md`.

## Matrice de collision (à remplir à H0)

| Ticket | `capture` | `analysis` | `render` | `sponsor` | `demo` |
| --- | --- | --- | --- | --- | --- |
| #10 squelette | x |  | x |  | x |
| #11 aha |  | x | x | x |  |
| #12 cas d'erreur |  | x | x |  | x |

Deux tickets qui partagent une case **ne tournent pas en parallèle**. Ils se suivent, ou
l'un des deux est découpé.

## Anti-patterns

| Anti-pattern | Symptôme | Correction |
| --- | --- | --- |
| God module | un module de 1500 lignes | découper par responsabilité |
| `utils/` fourre-tout | tout le monde importe tout | `lib/` par domaine, interfaces explicites |
| Import interne | `import ... from '../analysis/internal'` | passer par `index.ts` |
| Cycle | A importe B, B importe A | extraire un module ou inverser la dépendance |
| UI qui calcule | logique métier dans un composant | déplacer dans le module |
| Tests d'implémentation | tests qui cassent à chaque refactor | tester l'interface |
| Module par écran | 10 modules UI | modules par capacité métier |

## Ce que le jury doit entendre

En 30 secondes :

> « On a <N> modules. Le cœur, c'est <module>. Il prend <entrée> et produit <sortie>.
> Le reste (UI, stockage, sponsor) est autour. On peut le remplacer par un mock pour la démo. »

Si vous ne pouvez pas dire ça, l'architecture est trop compliquée.

## Sources

- [AI Hero — How To Make Codebases AI Agents Love](https://www.aihero.dev/how-to-make-codebases-ai-agents-love) : modules profonds, grey box, tests.
- [A Philosophy of Software Design](https://web.stanford.edu/~ouster/cgi-bin/book.php) — John Ousterhout : deep modules.
