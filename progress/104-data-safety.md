# Audit 104-data-safety

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk (reprise de l’audit)
- Piste : A/B/C
- Agent : lead
- Fichiers : `git/JGitRepository.kt`, `settings/{FileSettingsStore,SettingsViewModel}.kt`,
  `gradle/libs.versions.toml`, tests JGit/ViewModel

## Journal

- 00:30 — pull : si des fichiers suivis sont modifiés, `FastForwardMode.FF_ONLY`. Une
  divergence lève « commit or discard » avant tout reset ; le `reset --hard` n’est plus
  atteint avec un arbre sale.
- 00:32 — `minSdk` 26 → **33** (JGit/Java APIs utilisées : `readNBytes`, `transferTo`,
  `ByteArrayOutputStream.toString(Charset)`).
- 00:34 — `SettingsViewModel.saveDraft` copie aussi `gitConnection` et `serverUrl` ; seul
  `recentRepositories` reste celui du store.
- 00:35 — `FileSettingsStore` remplace `File.renameTo` par `Files.move(REPLACE_EXISTING,
  ATOMIC_MOVE)`, avec repli non atomique.

## Vérifications

- Nouveaux tests : pull FF avec fichier sale non lié, pull divergent avec fichier sale
  conservé, sauvegarde du compte Git/URL serveur.
- `:shared:jvmTest` + `:desktopApp:compileKotlin` verts.
- `assembleDebug`/instrumentés non lancés (SDK absent).

## Incertain

- `minSdk` 33 exclut les appareils < Android 13 ; décision H1 déjà actée.
