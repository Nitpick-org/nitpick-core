# Audit 2 — partage, dry run et capacité Ultra

> Deuxième revue, le 16 novembre 2026, après l'ajout de `docs/ULTRA.md`, du dry run et du
> starter. Objectif : vérifier que le repo peut être partagé aux 2 autres, testé en
> situation réelle, et que les agents sont réglés pour 2 comptes Ultra.

## Verdict

La base est maintenant **partageable et testable** : onboarding, dry run, starter exécutable,
scripts de capacité Ultra, garde-fous API key. Le dry run a été validé localement
(installation, typecheck, 6 tests, build, 2 tests e2e).

Deux choses restent **non vérifiables d'ici** : les réglages GitHub (collaborateurs,
protection de branche, Actions) et le comportement réel des agents Claude Code. Le dry run
est justement là pour ça.

## Ce qui a été corrigé depuis l'audit 1

| # | Constat | Correction |
| --- | --- | --- |
| 1 | Impossible de partager proprement le repo | `docs/ONBOARDING.md` : accès, clone, hooks, `.env`, Ultra, skills, `doctor.sh`, dépannage |
| 2 | Aucun test « situation réelle » | `docs/DRY-RUN.md` + starter Vite/React + `dry-run/TICKETS.md` + `dry-run/RETRO.md` |
| 3 | Le starter n'était pas exécutable | `scripts/bootstrap-dry-run.sh` : branche jetable, copie, install, commit |
| 4 | `new-worktree.sh` ne savait pas partir d'une branche de dry run | `WT_FROM` ajouté (`WT_FROM=dry-run/base`) |
| 5 | Le modèle de `implementer` était décrit « souvent Sonnet » | Corrigé : inherit, donc Opus sur H2/H3 Ultra |
| 6 | Les sessions Ultra n'étaient pas intégrées à la concurrence | Table `Total Ultra` dans `AGENT-OPS` (8 sessions, 3 écrivains) |
| 7 | Roster non optimisé pour Ultra | Section « Roster optimization for Ultra » : plus de lecteurs, jamais plus d'écrivains |
| 8 | `doctor.sh` ne vérifiait pas onboarding/dry run | Ajout des docs et du starter |
| 9 | README sans section partage | Section « Partager avec les 2 autres » + liens |
| 10 | Checklist J-5 disait « repo public » et « 3 membres » | Corrigé : repo privé, 2 invitations, public avant soumission |
| 11 | Pas de routage modèle : la doc pouvait partir sur Opus | `docs/MODEL-ROUTING.md`, skill `/route-task`, agent `scribe` (Haiku docs) |

## Constats encore ouverts

| # | Priorité | Constat | Action | Propriétaire |
| --- | --- | --- | --- | --- |
| 1 | P0 | Pas de `package.json` à la racine : `check.sh` et la CI échouent | Choisir la stack, bootstrap, déployer | H2, J-4 |
| 2 | P0 | Le repo est privé ; les 2 autres ne sont pas encore invités | Settings → Collaborators | H2, J-5 |
| 3 | P0 | Protection de `main` et CI verte non vérifiées | Activer la protection après le premier check vert | H2, J-4 |
| 4 | P0 | Pas de déploiement/preview | Déployer le hello world | H2, J-4 |
| 5 | P1 | `gh` n'est pas authentifié localement | `gh auth login` pour issues/PR/automatisation | H2 |
| 6 | P1 | Pas de GitHub Project / labels | Créer le board et les labels `ticket`, `bug`, `demo`, `P0` | H1 |
| 7 | P1 | Le dry run n'a pas encore été joué par les 3 humains | Le planifier à J-3 et remplir `dry-run/RETRO.md` | tous |
| 8 | P1 | Pas de test réel des 8 agents | Le dry run est le test ; noter les frictions | tous |
| 9 | P2 | Le starter utilise `lint = tsc --noEmit` | Acceptable pour le dry run ; la vraie stack aura un vrai linter | H2 |
| 10 | P2 | Le repo devra être public pour la soumission | Le rendre public à J-1, ou fournir un lien public | H2 |

## Audit des agents (Ultra)

### Ce qui est bon

- 8 agents, un rôle clair, pas de doublon ; `scribe` (Haiku) sort la doc des modèles chers.
- `maxTurns` sur chaque agent, tools restrictifs pour les lecteurs, `disallowedTools` sur
  `reviewer`.
- Modèle routé : Opus pour les décisions, Sonnet pour le volume, Haiku pour la doc,
  `implementer` en inherit (Opus sur H2/H3).
- Hooks `Stop` et `PreToolUse` : pas de « done » sans `check.sh`, pas d'édition des docs
  pivots par un sous-agent.
- Garde-fou `ANTHROPIC_API_KEY` dans `doctor.sh`, `ralph-once.sh`, `afk-ralph.sh`.
- AFK borné par `--max-turns` et `--max-budget-usd`, avec variables d'environnement validées.
- Routage documenté (`docs/MODEL-ROUTING.md`) + skill `/route-task` : escalation après
  2 échecs, Fable manuel, jamais d'Opus pour un README.

### Ce qu'il ne faut pas faire

- **Ajouter des agents écrivains code.** Le goulot est la revue humaine ; 8 sessions,
  3 écrivains code max. `scribe` est docs-only et ne compte pas dedans. Ultra donne des
  lecteurs en plus, pas des mergeurs en plus.
- **Activer les agent teams.** Expérimental, pas d'isolation worktree par coéquipier.
- **Mettre Fable par défaut.** Limite séparée, consommation rapide, réservé aux décisions
  à fort enjeu.
- **Utiliser `ANTHROPIC_API_KEY`.** En `-p`, Claude Code la prend systématiquement ; les
  scripts refusent, c'est voulu.
- **Créer un agent `scout` maison.** Les agents intégrés Explore/Plan font déjà la
  reconnaissance read-only.

### Optimisations recommandées

1. **Deux lecteurs en parallèle par humain Ultra** au kickoff : `researcher` (concurrents,
  API) + Explore/Plan (codebase). H1 reste sur le PRD.
2. **QA en continu** sur le compte H3 : `qa-demo` après chaque merge, pas seulement à H18.
3. **Skills préchargés** (`tdd`, `code-review`, `research`, `codebase-design`) seulement
  après installation et test ; ne pas les hardcoder avant.
4. **Subagents en arrière-plan** pour les longs QA/recherches si la version les supporte ;
  jamais pour écrire.
5. **Un AFK par compte Ultra**, sur le shift de son propriétaire, avec un ticket low-risk
  et un oracle déterministe.
6. **Router avant de déléguer** : `scribe`/Haiku pour la doc, Sonnet pour le standard,
  Opus pour le critique ; escalader après 2 échecs, jamais Fable en automatique.

## Audit du dry run

### Validé localement

- `pnpm install` : OK.
- `pnpm typecheck` : OK.
- `pnpm test` : 6 tests, OK.
- `pnpm build` : OK.
- `pnpm e2e` : 2 tests, OK (Chromium installé dans un cache local pour le test).
- Le sample produit bien **2 anomalies** ; le CSV invalide affiche une erreur.

### Limites connues

- Le starter est un **Vite/React jetable**, pas la stack du hackathon.
- `lint = tsc --noEmit` : ce n'est pas un vrai linter, c'est un placeholder pour le test.
- `pnpm` peut afficher `Ignored build scripts: esbuild` ; `pnpm rebuild esbuild` suffit.
- Playwright doit installer Chromium une fois (`pnpm exec playwright install chromium`).
- Le dry run ne teste ni le déploiement, ni la DB, ni les APIs sponsors, ni la nuit.

### Ce que le dry run doit produire

- Un `dry-run/RETRO.md` rempli.
- 3 frictions concrètes, avec un correctif et un propriétaire.
- 3 tickets dans le vrai repo, corrigés avant H0.
- Une démo de 3 minutes qui tient en 2:45.

## Les 5 actions qui restent

1. **Partager** : inviter les 2 autres, protéger `main`, vérifier les Actions.
2. **Stack** : choisir, bootstrap, déployer, `check.sh` + CI verts.
3. **Dry run** : le jouer à 3, remplir la rétro, corriger les 3 frictions.
4. **Agents** : tester les 8 agents en réel, vérifier les hooks et le refus
   `ANTHROPIC_API_KEY`.
5. **Event** : clés, reçus, repo public, `doctor.sh` sans échec, sommeil.

## Méthode de review

- Lecture complète des docs et scripts.
- Exécution de `doctor.sh`, `check.sh`, des scripts de worktree et des garde-fous API key.
- Installation et test du starter dans un dossier temporaire (typecheck, tests, build, e2e).
- Vérification des faits Ultra contre les pages officielles (plan, crédits API, Fable,
  variables d'environnement Claude Code).
- Limites : pas d'accès aux réglages GitHub (repo privé, `gh` non authentifié), pas de
  session Claude Code réelle pour les 8 agents ; le dry run couvre ce point.
