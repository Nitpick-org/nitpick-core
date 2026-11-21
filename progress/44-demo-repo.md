# Ticket 44-demo-repo

- Statut : en cours (préparation agent faite ; étapes humaines restantes)
- Propriétaire humain : @dovatgabriel
- Piste : démo (#39)
- Agent : Claude Code (lead)
- Model : opus
- Escalations : aucune
- Fichiers autorisés : `docs/demo/*`, `docs/DEMO.md`, `progress/44-demo-repo.md` ; le repo de
  démo vit hors de ce dépôt (`../pocket-ledger`).

## Journal

- 19:08 — worktree créé.
- 19:20 — `pocket-ledger` créé en local : CLI Kotlin/JVM, SQLite via JDBC, `add`, `list`,
  `report <mois>`. 3 tests verts, CLI vérifié à la main. Un commit propre sur `main`.
- 19:25 — brief Junie (`docs/demo/junie-brief.md`) : 3 commandes, un défaut planté par commit
  (off-by-one de pagination, injection SQL, reader non fermé). `DEMO.md` pointe le repo et la
  branche.

## Décisions

- Kotlin plutôt que TypeScript : le jury JetBrains lit du Kotlin, et le projet ressemble au
  nôtre sans en être une copie.
- Off-by-one sur l'offset de pagination : un défaut visible au premier essai (page 1
  décalée) et réparable en une ligne, donc une suggestion applicable dans Nitpick.
- `main` propre, requêtes préparées partout : l'injection de la branche tranche sur le
  reste du code, comme dans une vraie review.
- `slf4j-nop` en runtime : sqlite-jdbc logue via SLF4J et affichait 3 lignes d'avertissement
  à chaque commande, ce qui gâcherait la démo.

## Vérifications

- `./gradlew test` : 3 tests, 0 échec (rapport `build/test-results/test`).
- `ledger add` ×2, `list`, `report 2026-11` : sortie correcte, aucun warning.

## Incertain

- sqlite-jdbc 3.46.1.0 est une version connue et stable, pas vérifiée comme la dernière.

## Blocages (humains)

- Créer le repo public `Nitpick-org/pocket-ledger` et pousser `main`.
- Lancer le brief dans Air (Junie), relire, pousser la branche.
- Rejouer la review 5 fois ; captures de dogfooding dans `docs/demo/`.

## Suivant

- Dogfooding : Nitpick sur les branches #19, #20, #21 ; garder 2–3 captures.
