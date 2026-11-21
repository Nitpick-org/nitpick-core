# Ticket 89-android-back

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : C Android
- Agent : lead
- Fichiers : `ui/phone/PhoneFlow.kt`, `ui/App.kt`, `gradle/libs.versions.toml`,
  `shared/build.gradle.kts`

## Journal

- 23:50 — ajout de `BackHandler` Compose Multiplatform (`ui-backhandler` 1.12.2) :
  Clone/Graph → Repos, Review/Shared → Graph, Diff → Review ; depuis Repos, l'app se ferme.
- 23:52 — Settings (téléphone) : Retour ferme Settings avant de quitter.
- Dépendance `org.jetbrains.compose.ui:ui-backhandler` ajoutée à `commonMain`.

## Vérifications

- `:shared:jvmTest` + `:desktopApp:compileKotlin` verts.
- Android runtime non testé (SDK absent) : vérifier sur l’émulateur.

## Incertain

- `BackHandler` est déprécié au profit de `NavigationEventHandler` ; l’API stable actuelle
  reste utilisée, migration à prévoir.
