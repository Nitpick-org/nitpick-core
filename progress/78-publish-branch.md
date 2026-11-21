# Ticket 78-publish-branch

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : B + C (git / toolbar desktop + Graph Android)
- Agent : lead
- Fichiers : `git/{GitRepository,JGitRepository}.kt`, `ui/repository/{RepositoryViewModel,RepositoryScreen}.kt`,
  `ui/phone/{PhoneFlow,PhoneGraphScreen}.kt`, `ui/App.kt`, strings, `jvmTest/.../JGitRepositoryTest.kt`

## Journal

- 23:30 — `publishBranch` : push explicite `refs/heads/<branch>:refs/heads/<branch>` vers
  `origin`, vérifie les statuts JGit, puis écrit `branch.<name>.remote/merge` pour créer
  l’upstream. Test bare : nouvelle branche `feature`, upstream `origin/feature`, ahead/behind
  à 0, ref visible dans le bare.
- 23:35 — VM `publishBranch` (isPushing/pushError), toolbar desktop et Android : Publish
  remplace Push quand la branche n’a pas d’upstream ; action seulement si `origin` existe.

## Décisions

- Le bouton n’apparaît plus sans `origin` : pas de bouton mort.
- Publish utilise le même `isPushing`/`pushError` que Push : une seule opération réseau.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : tests JGit verts.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- Android non rejoué (SDK absent).

## Incertain

- Un remote `origin` sans branche distante mais avec un autre remote n’est pas publié ;
  hors scope.
