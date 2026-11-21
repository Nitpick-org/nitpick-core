# Brief Junie : branche à défauts de `pocket-ledger` (#44)

À coller dans Air, dans un clone de `pocket-ledger` positionné sur `main`. Junie produit la
branche que Nitpick relit pendant la démo (points 3 et 4 du script). La session Air sert
aussi de capture pour #39.

## Le prompt

> Crée la branche `feature/search-pages-import` depuis `main` et ajoute trois commandes au
> CLI `ledger`, **un commit par commande**, avec des messages conventionnels réalistes.
> C'est un repo de démo pour un outil de code review : chaque commit contient
> **volontairement** le défaut décrit ci-dessous. Écris-le comme un développeur pressé
> l'écrirait, sans commentaire qui le signale, et sans le corriger.
>
> 1. `ledger list --page <n>` : pages de 10 dépenses, numérotées à partir de 1, avec
>    `LIMIT ? OFFSET ?` dans `ExpenseStore.page(page: Int)`. **Défaut** : l'offset vaut
>    `page * PAGE_SIZE`, donc la page 1 saute les 10 premières dépenses (off-by-one).
>    Commit : `feat: paginate the expense list`.
> 2. `ledger search <text>` : les dépenses dont le marchand contient le texte, dans
>    `ExpenseStore.search(text: String)`. **Défaut** : la requête est construite par
>    concaténation, `"... WHERE merchant LIKE '%$text%'"`, avec `createStatement()`
>    (injection SQL). Commit : `feat: search expenses by merchant`.
> 3. `ledger import <file.csv>` : importe un export bancaire `date,merchant,category,amount`
>    (en-tête ignoré) avec `parseCents` pour les montants. **Défaut** :
>    `File(path).bufferedReader()` n'est jamais fermé, pas de `use {}` (ressource non
>    fermée). Commit : `feat: import expenses from a bank CSV export`.
>
> Le reste du code est propre : mêmes conventions que `ExpenseStore` (requêtes préparées
> partout ailleurs), README mis à jour avec les trois commandes, `./gradlew test` vert.
> Aucun secret, aucun token, aucun fichier `.db` commité.

## Ce que l'humain vérifie avant de pousser

- 3 commits sur la branche, un défaut par commit, aucun commentaire qui le trahit.
- `./gradlew test` vert ; `git ls-files` ne montre ni `.db` ni `.env`.
- Nitpick sur la branche contre `main` trouve **au moins l'off-by-one**, avec une suggestion
  applicable (`(page - 1) * PAGE_SIZE`). Sinon on ajuste le code, pas le prompt de review.

## Fallback sans Air

Même prompt dans Claude Code, noté dans `docs/AGENT_LOG.md` (`CONTEXT.md` §11).
