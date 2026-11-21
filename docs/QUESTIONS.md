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
| 13:05 | P1 | 13-structure | `GitRepositoryFactory` et `SettingsStore` en interface plutôt qu en `expect` (§6) ? | expect/actual avec actual vide / interface injectée via AppContainer | interface : un expect sans actual ne compile pas, et la DI manuelle du §5 suffit | |

## À répondre avant H0

| # | Question | Options | Recommandation | Qui |
| --- | --- | --- | --- | --- |
| Q-001 | Quelle stack ? | Next.js / SvelteKit / Symfony+Turbo | celle que les 3 maîtrisent | H2 |
| Q-002 | Quel challenge sponsor viser ? | aucun / 1 / 2 | 1 principal, annoncé aux tech talks | H1 |
| Q-003 | Qui tient le formulaire de soumission ? | H1 / H2 | H1, backup H2 | H1 |
| Q-004 | Usage payant après la limite hebdo Ultra ? | non / oui plafonné 50 $ / oui 100 $ | non, sauf si un compte est à sec avant 23:00 | H2 |
| Q-005 | Repo public dès maintenant ? | oui/non | oui, pas de secret dans l'historique | H2 |
| Q-006 | Clé API de l'app : crédits Ultra ou autre ? | crédits Ultra / clé perso / sponsor | crédits Ultra (`APP_ANTHROPIC_API_KEY`) | H2 |

## Règles

- Une question = une décision à prendre, pas un sujet de discussion.
- Toujours proposer 2–3 options et **ta** recommandation.
- Si la réponse ne change pas le résultat, choisis et note l'hypothèse dans le commit.
- Ne jamais mettre un secret dans une question.
