# Ticket 13-structure

- Statut : fini (build vert en local), à merger sur main
- Propriétaire humain : @thomas
- Piste : transverse (contrats du §6, avant les pistes A/B/C)
- Agent : Claude Fable 5.1 (session interactive)
- Model : fable
- Escalations : aucune
- Fichiers autorisés : shared/src/commonMain/kotlin/dev/nitpick/app/{domain,git,review,settings,ui}, shared/src/commonTest, gradle/libs.versions.toml, shared/build.gradle.kts, desktopApp/.../main.kt et androidApp/.../MainActivity.kt (import de App uniquement)

## Journal

- 12:50 — branche `ticket/13-structure` créée dans le checkout principal (le worktree
  atterrirait hors du dossier de travail de la session).
- 13:05 — contrats du §6 écrits dans `commonMain` : `domain` (Commit, Ref, WorkingTreeStatus,
  FileDiff/Hunk/DiffLine, ReviewResult/ReviewComment/Severity, Credentials, PullResult),
  `git` (GitRepository, GitRepositoryFactory), `review` (ReviewAgent), `settings` (Settings,
  SettingsStore). `App.kt` déplacé dans `ui/`. Un test commonTest : aller-retour JSON de
  `ReviewResult`.
- Décisions :
  - `GitRepositoryFactory` et `SettingsStore` sont des interfaces, pas des `expect` : un
    `expect` sans `actual` ne compile pas, et l'implémentation arrive dans les tickets JGit et
    réglages. Elles seront injectées via `AppContainer` (§5, DI manuelle). Proposé dans
    `docs/QUESTIONS.md`.
  - `Branch` et `Suggestion` (§6) ne sont pas des classes séparées : `Ref` porte un `RefType`
    (LOCAL_BRANCH, REMOTE_BRANCH, TAG) et la suggestion est le champ `suggestion` de
    `ReviewComment`, comme l'impose le contrat §6.
  - Pas de package `graph` ni `diff` pour l'instant : git ne versionne pas un dossier vide et
    il n'y a pas encore de contrat inter-pistes à y mettre. Leur premier ticket les crée.
  - kotlinx.serialization 1.13.0 (dernière release Maven Central) ajouté : `@Serializable`
    sur `ReviewResult` fait partie du contrat (sortie structurée Koog).
- Incertain : rien côté contrats. Avertissement Gradle « SDK XML version 4 » (cmdline-tools
  plus récents qu'AGP) : bénin.
- Blocage : aucun.
- Suivant : merger `ticket/13-structure` sur main, puis lancer #15, #16, #17 et #18 (frontière
  sans bloqueur) dans quatre worktrees.
- 13:40 — spec publiée (#14) et 9 tickets créés en sub-issues de #13 avec dépendances
  natives GitHub : #15 open repo desktop (B) · #16 lanes (A, multi-agent) · #17 parseur de
  diff (A) · #18 thème + EN/FR + réglages (C) · #19 diff vs base desktop (B, ← #15 #17) ·
  #20 agent Koog (A, ← #17) · #21 Android clone + écrans (C, ← #15) · #22 Nitpick this branch
  desktop (B+A, ← #19 #20, critique démo) · #23 boucle review Android + push (C, ← #22 #21,
  critique démo). Lot 2 (au sync 17h) : fetch/pull/checkout, outils readFile/listFiles pour
  l'agent, script de comptage des lignes partagées.
- 13:45 — toolchain installée en local sans sudo (validé par Thomas) : `brew install
  openjdk@21`, `brew install --cask android-commandlinetools`, puis `sdkmanager
  platform-tools platforms;android-37.0 build-tools;37.0.0`. `local.properties` (ignoré par
  git) pointe sur `/opt/homebrew/share/android-commandlinetools`. `JAVA_HOME` doit être
  exporté pour `gradlew` : `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`.
- 14:20 — `./scripts/check.sh` vert (253 s au premier run) : `:shared:allTests` (JVM + Android
  host, dont `ReviewResultSerializationTest` sur les deux), `:desktopApp:assemble`,
  `:androidApp:assembleDebug`. AGP 9.1.1 exige aussi `build-tools;36.0.0`, installé.
  `brew link --force openjdk@21` rend `java` visible aux hooks git et Claude.
