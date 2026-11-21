# Ticket 89-android-back

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : C Android
- Agent : lead
- Fichiers : `ui/phone/PhoneFlow.kt`, `ui/App.kt`, `gradle/libs.versions.toml`,
  `shared/build.gradle.kts`

## Journal

- 23:50 — ajout de `BackHandler` Compose Multiplatform (`ui-backhandler` 1.12.2) :
  depuis chaque écran téléphone, Retour système va au parent (Clone/Graph → Repos,
  Review/Shared → Graph, Diff → Review) ; depuis Repos, il quitte l’app.
- 23:52 — Settings (téléphone) : Retour ferme l’écran Settings avant de quitter.
- Dépendance `org.jetbrains.compose.ui:ui-backhandler` ajoutée à `commonMain`.

## Vérifications

- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- Android runtime non testé (SDK absent) : vérifier sur l’émulateur.

## Incertain

- `BackHandler` est marqué déprécié par Compose au profit de `NavigationEventHandler`,
  mais c’est l’API stable disponible ; migration à prévoir.
