# Dry run — tickets du mini-projet « Pulse »

> Projet fictif, jetable, pour tester la base et les agents en situation réelle.
> **Ne pas merger cette branche dans `main`.** Guide : `docs/DRY-RUN.md`.

## Le scénario

**Pulse** : un ingénieur d'astreinte colle un CSV de métriques et voit immédiatement les
valeurs aberrantes, avec leur z-score. Pas de compte, pas de base de données, pas d'API
externe. Le « aha » doit être visible en moins de 20 secondes.

**Challenge sponsor fictif — Acme Telemetry** : « Montrez une alerte explicable, pas une
boîte noire. » Critère : le z-score et la ligne fautive sont visibles à l'écran.

## Contraintes

- 2 h 30 maximum, 3 humains, 2 comptes Ultra.
- Pas de nouvelle dépendance au-delà du starter.
- Une seule intégration : aucune (tout est local).
- La démo doit tenir en 3 minutes, sans slides.

## Tickets

### #01 — Le module d'analyse

- **Comportement visible** : à partir d'un CSV `timestamp,value`, le module retourne les
  lignes avec un z-score et un drapeau `isAnomaly`.
- **Critères d'acceptation** :
  - [ ] `parseCsv` accepte un CSV valide et rejette les colonnes manquantes.
  - [ ] `parseCsv` rejette une valeur non numérique avec un message clair.
  - [ ] `detectAnomalies` flague la valeur aberrante avec un seuil de 1,5.
  - [ ] Un écart-type nul ne produit aucune anomalie.
  - [ ] Tests dans `src/modules/anomaly/index.test.ts`.
- **Fichiers autorisés** : `src/modules/anomaly/**`.
- **Dépendances** : aucune.
- **Risque** : bas.
- **Critique démo** : oui.

### #02 — L'écran de démo

- **Comportement visible** : l'utilisateur colle un CSV, clique sur « Analyze » et voit le
  nombre d'anomalies + un tableau avec les lignes surlignées.
- **Critères d'acceptation** :
  - [ ] Le sample est préchargé.
  - [ ] « Analyze » affiche « 2 anomalies » sur le sample.
  - [ ] Les deux lignes aberrantes sont surlignées.
  - [ ] « Reset sample » réinitialise l'écran.
  - [ ] Un CSV invalide affiche une erreur lisible.
- **Fichiers autorisés** : `src/App.tsx`, `src/styles.css`.
- **Dépendances** : #01.
- **Risque** : bas.
- **Critique démo** : oui.

### #03 — Le test de démo et le script

- **Comportement visible** : le parcours de `docs/DEMO.md` est joué par Playwright ; le
  script de 3 minutes est écrit et chronométré.
- **Critères d'acceptation** :
  - [ ] `pnpm e2e` passe sur le sample.
  - [ ] Un CSV invalide affiche l'erreur dans le test.
  - [ ] `docs/DEMO.md` contient la version 2:45 et la phrase de nouveauté.
  - [ ] La démo a été répétée 2 fois.
- **Fichiers autorisés** : `e2e/**`, `docs/DEMO.md`, `playwright.config.ts`.
- **Dépendances** : #02.
- **Risque** : bas.
- **Critique démo** : oui.

## Ordre conseillé

1. H2 : `WT_FROM=dry-run/base ./scripts/new-worktree.sh 01-anomaly` → agent `implementer`.
2. H3 : attend #01, puis `WT_FROM=dry-run/base ./scripts/new-worktree.sh 02-ui` → agent
   `implementer` + `qa-demo`.
3. H1 : écrit `docs/DEMO.md` en parallèle, puis #03 avec H3.
4. H2 : `reviewer` sur chaque diff, merge après `./scripts/check.sh --demo`.
5. Tous : démo de 3 minutes, puis `dry-run/RETRO.md`.

## Brief agent (à coller)

```text
Tu es implementer. Lis CLAUDE.md, docs/PRD.md, docs/DEMO.md, docs/ARCHITECTURE.md
et progress/<ticket>.md.
Ticket : #01 — module d'analyse.
But : parser un CSV timestamp,value et flaguer les anomalies par z-score.
Fichiers autorisés : src/modules/anomaly/**.
Critères d'acceptation : voir dry-run/TICKETS.md.
Fini = check.sh vert + test + progress/<ticket>.md + commit atomique.
```
