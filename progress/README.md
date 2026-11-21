# progress/ — journaux par ticket (sans conflit)

## Pourquoi ce dossier

Plusieurs agents travaillent en parallèle dans des worktrees différents. S'ils écrivent
tous dans `progress.txt`, chaque merge crée un conflit. Un fichier par ticket supprime
le problème : deux agents ne touchent jamais le même fichier.

## Règle

- Un agent écrit **uniquement** dans `progress/<ticket>.md` (créé par `scripts/new-worktree.sh`).
- `progress.txt` est le **journal de synthèse** : seuls les humains (idéalement
  l'intégrateur·rice) l'écrivent, à chaque sync, en 3 à 5 lignes.
- `docs/DECISIONS.md`, `docs/PRD.md` et `CLAUDE.md` ne sont jamais édités par un agent
  sans demande humaine explicite.
- `docs/QUESTIONS.md` est append-only : chaque agent ajoute une ligne, ne réécrit pas
  les lignes des autres.

## Format d'une entrée

```md
## 14:32 — ticket 12-graph-lanes (piste A)

- Fait : algorithme de lanes + 6 tests (merge, branche orpheline, octopus).
- Décisions : une lane par parent non encore placé, réutilisée dès qu'elle se libère.
- Modèle : sonnet.
- Escalades : sonnet → opus (2 échecs sur la validation).
- Fichiers : shared/src/commonMain/kotlin/dev/nitpick/app/graph/*, shared/src/commonTest/.../graph/*.
- Incertain : couleurs des lanes non testées en thème sombre.
- Blocage : aucun.
- Suivant : brancher sur `GitRepository.log()` réel (piste B).
```

Ce journal par ticket **est** le journal d'agent du §11 de `docs/CONTEXT.md` : les champs
agent, piste, tâche, fichiers, décisions et incertain y figurent. `docs/AGENT_LOG.md` n'en est
que la compilation, faite par un humain aux syncs.

## À la fin

- Fusionner les informations utiles dans `progress.txt` (synthèse) et `docs/DECISIONS.md`
  (décisions durables).
- Supprimer `progress/` et `progress.txt` après l'événement : ce sont des artefacts de session.
