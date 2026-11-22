# Ticket 234-audit-leftovers

- Statut : en cours
- Propriétaire humain : @pepito2t (H3)
- Piste : A core + B partage (restes de l'audit du 22/11)
- Agent : Claude Code, session humaine
- Model : Fable 5.1
- Fichiers autorisés : server/ (routes, store, tests), review/ReviewResponseParser.kt et son test, jvmTest JGitRepositoryTest.kt

## Journal

- 10:40 — demande H3 : solder les derniers points de l'audit. Serveur : `ignoreUnknownKeys` au décodage (un champ d'une app plus récente ne donne plus 400) ; clé de dépôt : chemin en minuscules pour github.com et gitlab.com seulement, port conservé (deux forges sur une machine = deux listes). Parser : départ au premier `{"` (une accolade dans la prose ne déclenche plus l'appel de réparation), `endLine` borné par le `startLine` corrigé. Test JGit : checkout par-dessus une modification locale en conflit → échec, arbre intact.
- Tests exécutés : `ReviewServerTest` 12, `ReviewResponseParserTest` 5, `JGitRepositoryTest` 44, tous verts.

## Décisions

- Casse du chemin repliée seulement sur les hôtes connus insensibles à la casse ; une forge auto-hébergée garde la sienne.
