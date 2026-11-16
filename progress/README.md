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
## 14:32 — ticket 12-login

- Fait : formulaire + validation + redirection.
- Décisions : session cookie plutôt que JWT (plus simple pour la démo).
- Fichiers : src/modules/auth/*, tests/auth.test.ts.
- Blocage : clé API sponsor absente → mock utilisé, question dans QUESTIONS.md.
- Suivant : brancher le vrai provider si la clé arrive.
```

## À la fin

- Fusionner les informations utiles dans `progress.txt` (synthèse) et `docs/DECISIONS.md`
  (décisions durables).
- Supprimer `progress/` et `progress.txt` après l'événement : ce sont des artefacts de session.
