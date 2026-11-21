# Ticket 104-audit-data-safety

- Statut : prêt pour PR
- Propriétaire humain : @dovatgabriel
- Piste : B git+desktop, C android (minSdk)
- Agent : Claude Code, session principale (audit qualité du 22/11)
- Model : Fable, choisi à la main par H1
- Escalations : aucune
- Fichiers : `jvmSharedMain/.../git/JGitRepository.kt`,
  `jvmSharedMain/.../settings/FileSettingsStore.kt`,
  `commonMain/.../ui/settings/SettingsViewModel.kt`, `gradle/libs.versions.toml`, leurs tests,
  `README.md`, `docs/QUESTIONS.md` (Q-020)

## Journal

- 00:23 — worktree créé depuis `main` à jour (`f61a807`).
- 00:26 — pull : test rouge d'abord. Avec `b.txt` modifié sans commit et un conflit sur
  `a.txt`, le `reset --hard` remettait `b.txt` à HEAD (lu « 1 » au lieu de « work in
  progress »). Correction : fast-forward seulement quand l'arbre a des modifs ; un historique
  divergent rend `ABORTED` sans rien toucher.
- 00:28 — minSdk 26 → 33 : JGit 6.10 appelle `readNBytes`, `transferTo` et
  `toUnmodifiableList`, tous datés API 33 dans `api-versions.xml`. `DiscardOutputStream`
  remplacé par `OutputStream.nullOutputStream()`.
- 00:30 — `saveDraft` jetait le compte Git (#91) et l'URL du serveur : il prend maintenant
  tout le brouillon, sauf les repos récents.
- 00:31 — `FileSettingsStore.save` : `Files.move` à la place de `File.renameTo`.
- 00:33 — le contrôle « push rejeté » de `push` et `publishBranch` devient `requireAccepted`.

## Décisions

- Pull sur arbre modifié : fast-forward seulement plutôt qu'un refus sec, pour ne pas casser
  le pull fast-forward qui marchait déjà sans danger.
- minSdk 33 décidé par H1 (#104) ; l'écart avec `CONTEXT.md` §14 est noté en Q-020.
- `saveDraft` : seul `recentRepositories` change sous un dialog ouvert, donc lui seul est
  repris des réglages courants.

## Vérifications

- `JGitRepositoryTest` 34 tests, `SettingsViewModelTest` 5, `FileSettingsStoreTest` 7, 0 échec.
- `./scripts/check.sh` vert à chaque commit (hook pre-commit).

## Incertain

- Aucun appareil sous l'API 33 : on ne sait pas si D8 rattrape une partie des appels de JGit.
  Le téléphone de démo doit être en Android 13 ou plus.
- Windows : le correctif `Files.move` n'a pas tourné sur une machine Windows.
- Tests sur ART (`connectedDebugAndroidTest`) non relancés : aucun émulateur démarré.

## Suivant

- Relecture humaine de la PR, puis parcours `docs/DEMO.md` sur le téléphone de démo.
