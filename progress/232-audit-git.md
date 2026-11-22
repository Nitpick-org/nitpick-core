# Ticket 232-audit-git

- Statut : en cours
- Propriétaire humain : @pepito2t (H3)
- Piste : A core (git) + réglages
- Agent : Claude Code, session humaine ; audit par un sous-agent Explore en lecture seule, deux sondes contre le jar JGit 6.10.1
- Model : Fable 5.1
- Fichiers autorisés : jvmSharedMain git/{JGitRepository,JGitRepositoryFactory}.kt, jvmSharedMain settings/FileSettingsStore.kt, ui/repository/{RepositoryViewModel,RepositoryScreen}.kt, ui/phone/PhoneGraphScreen.kt, androidApp MainActivity.kt, docs/IDEAS.md, tests associés

## Journal

- 09:50 — audit ponytail (demande H3), volet git et réglages : 10 constats ; issue #232. Vérifié dans le code : seul `org.eclipse.jgit` est sur le classpath (pas de module SSH), le `DiffFormatter` cite les chemins, `FileSettingsStore` rend des réglages par défaut sur une erreur de parsing.
- 10:10 — corrigé : remote SSH refusé par une phrase sur fetch, pull, push, publish et clone ; `setQuotePaths(false)` ; `coerceInputValues` + fichier illisible déplacé en `.bak` + fichier temporaire en 0600 avant écriture ; HEAD détaché sans tracking et sans Publish ; Pull inactif sans upstream (desktop et téléphone) ; lectures git avant `_state.update` ; un seul `AppContainer` par processus sur Android.
- Tests ajoutés (tous exécutés) : remote SSH → phrase attendue sur fetch et clone ; `café.kt` revient tel quel dans le diff et se lit ; HEAD détaché → tracking nul et publish refusé ; fichier de réglages corrompu gardé en `.bak` ; valeur d'enum inconnue → les autres champs survivent.

## Décisions

- Pas d'ajout du module SSH de JGit cette nuit : non testé sur Android (dex), prompts de clé d'hôte et de passphrase à faire. Idée notée dans `docs/IDEAS.md` ; les dépôts de démo sont des clones HTTPS.
- Pull sur une branche divergée avec arbre propre : garde le merge (le doc du view model parle des conflits, pas des merges).
- `GitRepository.close()` à la réouverture : changement de contrat, un seul dépôt ouvert en démo ; pour H2.
