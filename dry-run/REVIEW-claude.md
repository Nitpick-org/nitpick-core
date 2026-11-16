# Review de la base — Claude

> Review faite **sans jouer le dry run à 3** : lecture des scripts, hooks, CI et starter,
> plus `typecheck`, `test` et `e2e` du starter dans une copie hors du repo. Tout ce qui
> demande des humains, des agents en réel ou un bootstrap est marqué « non testé ».

## 0. Contexte

| Champ | Valeur |
| --- | --- |
| Review de | Claude (Opus 5.5), session de Thomas |
| Date | 2026-11-16 |
| Commit testé | `22d0758` |
| OS / Node / pnpm / Claude Code | macOS 27.0 / v22.22.3 / 11.28.5 / 2.1.309 |
| Compte | non applicable |
| Temps total passé | environ 30 min |
| Dry run fait dans | copie du starter dans le scratchpad, sans bootstrap ni commit |
| Branche | `main` (pas de `dry-run/base`) |
| `./scripts/check.sh` final | non testé (pas de `package.json` à la racine). Équivalent starter : typecheck ✅, test ✅ (6/6) |
| `./scripts/check.sh --demo` final | non testé. Équivalent starter : e2e ✅ (2/2, 3,4 s) après installation de Chromium |
| Démo 3 min tenue | non testé |

## 1. Verdict en 30 secondes

| Dimension | Poids | Note /5 | Points |
| --- | --- | --- | --- |
| Onboarding | 4 | non testé | — |
| Dry run | 5 | non testé | — |
| Docs | 3 | 4 | 2,4 |
| Agents / modèles | 4 | non testé | — |
| Confiance pour H0 | 4 | 3 | 2,4 |
| **Total** | **20** | | **partiel, non comparable** |

- **GO / NO-GO pour H0** : GO, à condition de corriger les bugs 1 à 4 avant le bootstrap de la stack (J-4).
- **Les 3 choses à corriger avant H0** :
  1. La CI casse dès que la stack arrive : conflit de version pnpm, et Chromium non installé (bugs 1 et 2).
  2. Le guard des fichiers protégés ne s'applique pas aux boucles AFK/Ralph (bug 3).
  3. La regex `.env` du pre-commit bloque `.env.example`, et ce contrôle est sauté avant la stack (bug 4).
- **La chose qui te ferait gagner le plus de temps** : ne formater que le fichier édité dans le hook PostToolUse, au lieu de tout le repo à chaque Edit (bug 6).
- **La chose la plus solide de la base** : les scripts défensifs (`new-worktree.sh`, `afk-ralph.sh`, `ralph-once.sh`), qui valident les noms, refusent `main`, un arbre sale et `ANTHROPIC_API_KEY`, et ont des budgets bornés.

## 2. Onboarding

Non testé : l'onboarding demande un clone neuf par un humain.

### Signaux faibles

- J'ai supposé que : Claude Code envoie bien `agent_type` aux hooks pour un sous-agent. Tout `guard-protected.sh` en dépend, et ça n'a pas été vérifié en réel.
- J'ai cherché sans trouver : une étape `playwright install` dans la CI.

## 3. Dry run

| Métrique | Valeur |
| --- | --- |
| Temps jusqu'au premier `check.sh` vert | non testé (équivalent starter : environ 2 min, installation comprise) |
| Temps jusqu'au premier commit d'agent | non testé |
| Temps jusqu'au premier merge | non testé |
| Temps jusqu'à la première démo | non testé |
| Tickets terminés | non testé |
| Conflits de merge | non testé |
| `check.sh` vert au premier essai | non testé (starter : ✅) |
| `check.sh --demo` vert | non testé (starter : ❌ au premier essai, Chromium absent ; ✅ ensuite) |
| Démo 3 min | non testé |
| Nombre d'agents utilisés | 0 |

- Ce qui a cassé : e2e au premier lancement, `Executable doesn't exist at …/chrome-headless-shell`. C'est une limite déjà connue (`docs/AUDIT-2.md`), mais la CI va heurter le même mur.
- Ce que tu as dû faire à la main alors que ça devait être automatique : `pnpm exec playwright install chromium`.

### Top 3 pertes de temps

Non testé : il faut le chronométrage du dry run réel.

## 4. Docs et repo

| Dimension | Note /5 | Commentaire |
| --- | --- | --- |
| Clarté | 4 | Les en-têtes de scripts disent quoi, pourquoi et comment |
| Exactitude | 3 | Message de nettoyage faux dans `bootstrap-dry-run.sh` (bug 8) ; `CLAUDE.md` promet un blocage que le guard n'assure pas en AFK |
| Complétude | non noté | La prose des docs n'a pas été relue en entier |
| Cohérence FR / EN | 3 | Scripts en anglais, messages des hooks en français, docs en français |
| Longueur / navigabilité | non noté | |

- Lien mort / commande fausse / contradiction : `bootstrap-dry-run.sh:65-72` (« Next » crée `../wt-01-anomaly`, « Cleanup » supprime `.worktrees/wt-01-anomaly`).
- Ce qu'on peut supprimer : `scripts/demo-check.sh`, qui fait doublon avec `check.sh --demo` et n'est cité que dans `docs/PLAYBOOK.md`.
- Un nouveau collaborateur comprendrait-il le repo en < 30 min ? Non testé.

## 5. Agents et modèles

Aucun agent lancé en réel : tous les agents sont non testés.

- Hooks `Stop` / `PreToolUse` : lus, pas déclenchés. Le `PreToolUse` a deux trous : les sessions `claude -p` (bug 3) et les commandes Bash (bug 5).
- Ce qui manque dans les agents : non testé.

## 6. Bugs et frictions

| # | Sévérité | Où | Repro | Attendu | Obtenu | Correctif proposé |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | P0 | `.github/workflows/ci.yml:23-27` | Une stack avec `packageManager: pnpm@11.28.5` à la racine, puis une PR | CI qui installe | `pnpm/action-setup@v4` : « Multiple versions of pnpm specified » (non exécuté, comportement connu de l'action) | Supprimer `version: 11` |
| 2 | P0 | `.github/workflows/ci.yml:56-62` | Un script `e2e` présent, puis une PR | e2e vert | `Executable doesn't exist` (reproduit en local, commit `22d0758`) | Ajouter un step `pnpm exec playwright install --with-deps chromium` |
| 3 | P0 | `scripts/guard-protected.sh:13-14` + `afk-ralph.sh:89`, `ralph-once.sh:69` | Boucle AFK, l'agent édite `docs/PRD.md` | Blocage | Le guard laisse passer (pas d'`agent_type` en `claude -p`) ; lecture du code, non déclenché | `export HACKATHON_HEADLESS=1` dans les scripts et test de cette variable dans le guard |
| 4 | P0 | `.githooks/pre-commit:10-19` | Modifier `.env.example`, puis commiter (stack présente) | Commit accepté | La regex matche `.env.example` (prouvé : `printf '.env.example' \| grep -Eq '(^\|/)\.env($\|\.)'`) ; avant la stack, `.env` forcé passe | Monter le bloc au-dessus du test `package.json`, exclure `.env.example` |
| 5 | P1 | `.claude/settings.json:38-59` | Un sous-agent lance `grep . .env` ou `sed -i docs/PRD.md` | Refus | Autorisé : les `deny` ne visent que `cat`, `head`, `tail` et `Read`/`Edit`/`Write` | Matcher `Bash` dans le guard, qui refuse `.env` (sauf `.env.example`) et les fichiers protégés |
| 6 | P1 | `.claude/settings.json:80` | Une stack présente, n'importe quel Edit | Formater le fichier édité | `prettier --write .` sur tout le repo, erreurs masquées par `\|\| true` | `jq -r .tool_input.file_path \| xargs pnpm exec prettier --write --ignore-unknown` |
| 7 | P2 | `check.sh:29-40`, `demo-check.sh:24-29`, `doctor.sh:103`, `ci.yml:58` | Pas de lockfile | Même gestionnaire de paquets partout | `check.sh` choisit pnpm, `demo-check.sh` choisit npm | Supprimer `demo-check.sh`, ajouter `--url` à `check.sh` |
| 8 | P2 | `scripts/bootstrap-dry-run.sh:65-72` | Suivre « Next » puis « Cleanup » | Worktree supprimé | `../wt-01-anomaly` reste | Préfixer « Next » par `WT_BASE="$PWD/.worktrees"` |
| 9 | P2 | `ci.yml:53-62` | Une PR avec un script `e2e` | Une seule passe de vérification | typecheck, lint et test lancés deux fois | Lancer seulement `check.sh --demo` quand `e2e` existe |
| 10 | P2 | `dry-run/starter/src/modules/anomaly/index.ts:24`, `App.tsx:76` | Ligne CSV `2026-…,` ; ou deux timestamps identiques | Erreur ; ou lignes distinctes | Valeur 0 et fausse anomalie ; ou collision de `key` React | Rejeter `""` avant `Number` ; utiliser l'index comme clé. Bons tickets de dry run |

> P0 = bloque la démo ou l'onboarding. P1 = ralentit vraiment. P2 = polish.

## 7. Risques pour le vrai hackathon

| Risque | Probabilité | Impact | Mitigation |
| --- | --- | --- | --- |
| Démo | non évalué | | |
| Timing | Moyenne | Moyen | CI rouge au bootstrap (bugs 1 et 2) : la corriger avant J-4 |
| Coordination 3 humains | non évalué | | |
| Quota Ultra | Faible | Élevé | Bien couvert : les scripts refusent `ANTHROPIC_API_KEY` et les budgets sont bornés |
| Stack / déploiement | Moyenne | Élevé | Pas encore de stack ni de preview (`docs/AUDIT-2.md` #1 et #4) |
| Agent AFK qui modifie le PRD | Faible | Élevé | Bug 3 |

## 8. Recommandations

### À faire avant H0 (P0)

1. Corriger la CI : supprimer `version: 11`, ajouter `playwright install` (bugs 1 et 2).
2. Faire respecter le guard en `claude -p` (bug 3).
3. Corriger le contrôle `.env` du pre-commit (bug 4).

### Quick wins (P1)

1. Formater seulement le fichier édité (bug 6).
2. Ajouter un matcher `Bash` au guard (bug 5).

### À ignorer / plus tard (P2)

1. Fusionner `demo-check.sh` dans `check.sh` (bugs 7 et 9).
2. Corriger le message de nettoyage du bootstrap (bug 8).

## 9. Questions / décisions

| Question | Options | Ta recommandation |
| --- | --- | --- |
| Le guard doit-il bloquer aussi en session humaine headless ? | a) seulement les sous-agents ; b) les sous-agents et `claude -p` | b, puisque `claude -p` n'a pas d'humain dans la boucle |
| Garder `demo-check.sh` ? | a) garder ; b) fusionner dans `check.sh --url` | b, une seule source de vérité |

## 10. Preuves

- Commandes lancées : `git pull --ff-only origin main` ; dans une copie du starter : `pnpm install --ignore-scripts`, `pnpm run typecheck` (1,2 s), `pnpm run test` (6/6), `pnpm run e2e` (❌, puis ✅ 2/2 après `pnpm exec playwright install chromium`) ; test de la regex du pre-commit.
- Branche / commit : `main` @ `22d0758`.
- Ce que tu n'as pas testé : onboarding, bootstrap `dry-run/base`, `check.sh` à la racine, worktrees, les 8 agents, les hooks déclenchés en réel, la CI GitHub, la démo chronométrée, la prose des docs.

---

## Le message à renvoyer

```text
Review de Claude — commit 22d0758

Note : partielle (lecture du code + tests du starter, dry run non joué) — GO conditionnel

Top 3 à corriger :
1. CI : supprimer `version: 11` (conflit avec packageManager) + installer Chromium
2. guard-protected : bloquer aussi en `claude -p` (AFK peut éditer PRD/DECISIONS)
3. pre-commit : la regex .env bloque .env.example ; contrôle sauté avant la stack

Le plus gros gain de temps :
- Hook prettier : formater le fichier édité, pas tout le repo à chaque Edit

Bugs P0 :
- Les 4 ci-dessus (CI x2, guard AFK, pre-commit .env)

Ce qui manque dans les agents :
- non testé (aucun agent lancé en réel)

Ce que je n'ai pas testé :
- onboarding, bootstrap, worktrees, agents, hooks en réel, CI GitHub, démo chronométrée
```
