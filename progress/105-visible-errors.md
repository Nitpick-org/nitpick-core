# Audit 105-visible-errors

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : B/C
- Agent : lead
- Fichiers : `ui/repository/RepositoryViewModel.kt`, `ui/phone/PhoneReviewScreen.kt`,
  `commonTest/.../RepositoryViewModelTest.kt`

## Journal

- 00:40 — `actionError` est rendu sur le téléphone entre les nits et la barre commit ;
  il est effacé au début d’une review ou d’un checkout.
- 00:42 — Apply : le `writeLock` couvre la mise à jour d’état et vérifie `appliedIds`.
  Commit : le lock couvre stage/commit/log/état et vérifie `appliedFiles` ; un second clic
  ne crée plus de commit vide.
- 00:44 — `pushError` effacé au début d’un pull/fetch ; `refs()`/`trackingStatus()` du commit
  lus hors du bloc `_state.update`.

## Vérifications

- `:shared:jvmTest` + `:desktopApp:compileKotlin` verts.
- Tests ajoutés : apply deux fois = un patch, commit deux fois = un commit.

## Incertain

- Le rendu `actionError` téléphone n’est pas couvert par un test Compose.
