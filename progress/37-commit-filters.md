# Ticket 37-commit-filters

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : B desktop
- Agent : lead
- Fichiers : `shared/src/commonMain/.../ui/repository/{CommitFilters,RepositoryScreen}.kt`,
  `commonTest/.../ui/repository/CommitFiltersTest.kt`, `composeResources/values{,-fr}/strings.xml`

## Journal

- 19:00 — modèle pur `CommitFilters` + `filterCommits` : texte/hash, branche (ancêtres du
  tip), auteur exact, date (today/week/month) avec `now` injecté.
- 19:05 — UI : champ « Text or hash » + dropdowns Branch/User/Date + Clear sous l'en-tête
  Commits. Filtre actif → **graphe masqué**, liste simple (message, auteur, hash, date) ;
  filtre vide → graphe complet.
- 19:08 — tests 5 cas : texte/sujet, hash, auteur, branche, date ; 100 tests JVM verts.

## Décision

- Option la moins chère recommandée par l'issue : **masquer le graphe** quand un filtre est
  actif. Les lanes ne sont pas recalculées sur une liste partielle (elles dessineraient des
  arêtes vers des lignes absentes).

## Vérifications

- `:shared:jvmTest --rerun-tasks` : **100 tests, 0 échec**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- `./scripts/check.sh` complet non lancé (pas de SDK Android ici).

## Incertain

- Le `now` de la date est figé à l'ouverture de l'écran ; suffisant pour une session de démo.
- Branche distante : on prend la première ref locale, sinon distante, de même nom.
