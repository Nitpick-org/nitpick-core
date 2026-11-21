# Questions agents → humains

> Les agents écrivent ici quand personne n'est disponible. **Append-only** : on ajoute une
> ligne, on ne réécrit pas celles des autres. Les humains répondent à chaque sync.

## Priorités

- **P0** : bloque un ticket critique démo. Réponse attendue < 30 min.
- **P1** : bloque un ticket non critique. Réponse au prochain sync.
- **P2** : curiosité / amélioration. Réponse si le temps le permet.

## File

| Heure | Prio | Agent / ticket | Question | Options | Recommandation | Réponse humaine |
| --- | --- | --- | --- | --- | --- | --- |
| | | | | | | |

## À répondre avant H0

| # | Question | Options | Recommandation | Qui |
| --- | --- | --- | --- | --- |
| Q-001 | Quelle stack ? | Next.js / SvelteKit / Symfony+Turbo | celle que les 3 maîtrisent | H2 |
| Q-002 | Quel challenge sponsor viser ? | aucun / 1 / 2 | 1 principal, annoncé aux tech talks | H1 |
| Q-003 | Qui tient le formulaire de soumission ? | H1 / H2 | H1, backup H2 | H1 |
| Q-004 | Usage payant après la limite hebdo Ultra ? | non / oui plafonné 50 $ / oui 100 $ | non, sauf si un compte est à sec avant 23:00 | H2 |
| Q-005 | Repo public dès maintenant ? | oui/non | oui, pas de secret dans l'historique | H2 |
| Q-006 | Clé API de l'app : crédits Ultra ou autre ? | crédits Ultra / clé perso / sponsor | crédits Ultra (`APP_ANTHROPIC_API_KEY`) | H2 |

## Écarts entre docs (relevés le 21/11 à H0+1, à trancher)

En attendant la réponse, les agents appliquent l'ordre d'autorité d'`AGENTS.md` :
règlement LauzHack, puis `docs/CONTEXT.md`, puis le reste.

| # | Écart | Options | Recommandation | Qui |
| --- | --- | --- | --- | --- |
| Q-007 | `CONTEXT.md` §13 prévoit deux slides (partagé vs spécifique, process agentique) ; le règlement LauzHack interdit les slides (images et vidéos de secours autorisées). | écran « About » dans l'app + sortie du script de comptage / images de secours plein écran / garder les slides | écran dans l'app + sortie du script en live ; aucune slide | H1 |
| Q-008 | `CONTEXT.md` §2 parle d'agents dans « JetBrains Air » ; le talk JetBrains parle de JetBrains AI et de Junie (bonus ~10–15 %, à vérifier) ; l'équipe travaille avec Claude Code. | Claude Code seul / Claude Code + au moins une vraie tâche Junie loggée / tout dans Junie | Claude Code + une tâche Junie réelle dans `AGENT_LOG.md` ; confirmer le nom au stand | H1 |
| Q-009 | Planning : `CONTEXT.md` §10 (portes 13/17/21 h, sommeil 02–07 h, ≥ 3 h chacun) contre `PLAYBOOK.md`/`STRATEGY.md` (squelette déployé à 16:00, shifts de 4 h à 22/02/06 h). | `CONTEXT.md` / `PLAYBOOK.md` / mélange | portes et fenêtre de sommeil de `CONTEXT.md` ; rotation à fixer en sync | tous |
| Q-010 | JGit 6.10.1 au lieu de la dernière stable (`CONTEXT.md` §5) : JGit 7 ne se dexe pas sous l'API 34 (commit `df36830`). | valider / JGit 7 avec minSdk 34 | valider, l'inscrire dans `DECISIONS.md` | H2 |
| Q-011 | Le wizard génère `shared/` + `desktopApp/` + `androidApp/`, pas le `composeApp/` du §6 ; le source set s'appelle `jvmSharedMain`. | valider et corriger le §6 / revenir à `composeApp` | valider, corriger le §6, l'inscrire dans `DECISIONS.md` | H2 |
| Q-012 | `STACK.md`, `ARCHITECTURE.md` (arborescence), `PRD.md`, la toolchain d'`ONBOARDING.md`, `DRY-RUN.md` et `PRE-EVENT-CHECKLIST.md` décrivent encore la base TypeScript. | bandeau « obsolète » en tête / déplacer dans `docs/archive/` / laisser | bandeau en tête (`AGENTS.md` les marque déjà comme historiques) | H2 |
| Q-013 | `DEMO.md` et `GLOSSARY.md` sont encore des templates. | les remplir depuis `CONTEXT.md` §13 et §1 / attendre le feature freeze | les remplir maintenant : DEMO par H1, glossaire (nit, base, lane, ancrage, suggestion) par H2 | H1, H2 |

## Règles

- Une question = une décision à prendre, pas un sujet de discussion.
- Toujours proposer 2–3 options et **ta** recommandation.
- Si la réponse ne change pas le résultat, choisis et note l'hypothèse dans le commit.
- Ne jamais mettre un secret dans une question.
