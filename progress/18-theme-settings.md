# Ticket 18-theme-settings

- Statut : prêt pour review (PR)
- Propriétaire humain : @thomas
- Piste : C android+finitions
- Agent : Claude Fable 5.1 (session interactive)
- Model : fable
- Escalations : aucune
- Fichiers autorisés : shared/src/commonMain/kotlin/dev/nitpick/app/{ui,settings}/**,
  shared/src/commonMain/kotlin/dev/nitpick/app/AppContainer.kt,
  shared/src/commonMain/composeResources/**, shared/src/jvmSharedMain/.../settings/**,
  shared/src/{commonTest,jvmTest}/.../settings/**, desktopApp/.../main.kt,
  androidApp/.../MainActivity.kt, gradle/libs.versions.toml, shared/build.gradle.kts

## Fait

- `Settings` est `@Serializable` (contrat inchangé).
- `FileSettingsStore(file)` dans `jvmSharedMain` : JSON, `Dispatchers.IO`, fichier absent ou
  corrompu → `Settings()`, `ignoreUnknownKeys` pour les champs futurs.
- `AppContainer(settingsStore)`, créé par `main()` (`~/.nitpick/settings.json`) et
  `MainActivity` (`filesDir/settings.json`).
- `NitpickTheme(themeMode)` : Material 3 clair/sombre, SYSTEM suit `isSystemInDarkTheme()`,
  accent unique teal (`#0F766E` clair, `#5EEAD4` sombre).
- `SettingsViewModel` (StateFlow<Settings>, `update { copy }` qui sauvegarde) +
  `SettingsScreen` : thème, fournisseur, modèle, clé API masquée.
- 12 strings EN/FR dans `composeResources/values{,-fr}/strings.xml`, zéro `Text("…")` dans `ui/`.
- `App(container)` : thème + écran Réglages comme seul écran ; le boilerplate du wizard
  (Greeting, logo, previews) est supprimé.
- Tests : `FileSettingsStoreTest` (jvmTest, 4 tests : aller-retour, absent, corrompu, dossier
  parent créé) et `SettingsTest` (commonTest, défauts). Rapport : 4 + 1 exécutés, 0 échec.

## Décisions

- Un seul `FileSettingsStore` en `jvmSharedMain` plutôt que l'expect/actual + SharedPreferences
  du §5 : `java.io.File` existe sur les deux cibles, donc zéro code plateforme et un seul test.
  Écart noté dans `docs/QUESTIONS.md`.
- Pas de test du ViewModel : il faudrait `kotlinx-coroutines-test` + `Dispatchers.setMain` pour
  trois lignes de logique ; le store et les défauts sont testés comme le ticket l'exige.
- Première frame avec le thème par défaut avant la lecture du fichier : accepté.

## Incertain / blocages

- Aucun. Android rejoué sur l'émulateur `nitpick` (API 36, arm64) : tap Dark →
  `files/settings.json` = `{"themeMode": "DARK"}` en `-rw-------`, force-stop + relance → Dark
  toujours sélectionné et thème sombre appliqué (captures dans le scratchpad de session).

## Suivant

- #15 branche le layout desktop et déplace l'écran Réglages derrière une navigation.

## Journal

- 15:45 — branche `ticket/18-theme-settings`, plan.
- 16:40 — implémentation, `check.sh` vert (1er run : test non-void, corrigé).
- 16:50 — desktop lancé, toggle Sombre → `~/.nitpick/settings.json` écrit, fermeture OK.
- 17:20 — review ponytail : écritures sérialisées sur un seul thread (`limitedParallelism(1)`),
  fichier écrit via `.tmp` + rename, lecture réservée au propriétaire, chargement initial par
  `compareAndSet` ; test permissions ajouté (5 tests store).
- 17:50 — rebase sur `main` (#27 et #29 mergés) : `AppContainer` porte git + settings, `App`
  garde `RepositoryScreen` sous `NitpickTheme` avec un bouton « Réglages » et un retour ;
  29 strings EN/FR. Règle force push assouplie pour les branches de PR (règles d'équipe et
  liste de refus Claude).
- 18:15 — émulateur installé (`sdkmanager emulator` + image android-36 arm64, AVD `nitpick`),
  persistance Android vérifiée ; libellés des boutons segmentés sur une ligne (auto-size, sans
  coche) pour que « Anthropic » tienne sur téléphone.
- 18:40 — modèle : liste par fournisseur (`LlmProvider.knownModels`, champ éditable via
  `ExposedDropdownMenuBox`) ; changer de fournisseur sélectionne son premier modèle. Ids
  Anthropic vérifiés (opus/sonnet/haiku 5.5), OpenAI/Google/Ollama non vérifiés (`shortcut:`).
