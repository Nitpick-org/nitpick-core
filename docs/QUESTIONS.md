# Questions agents → humains

> Les agents écrivent ici quand personne n'est disponible. **Append-only** : on ajoute une
> ligne, on ne réécrit pas celles des autres. Les humains répondent à chaque sync.

## Priorités

- **P0** : bloque un ticket critique démo. Réponse attendue < 30 min.
- **P1** : bloque un ticket non critique. Réponse au prochain sync.
- **P2** : curiosité / amélioration. Réponse si le temps le permet.

## File

| Heure | Prio | Agent / ticket | Question | Options | Recommandation | Réponse humaine |
| --- | --- | --- | --- | --- | --- | --- |
| | | | | | | |
| 13:05 | P1 | 13-structure | `GitRepositoryFactory` et `SettingsStore` en interface plutôt qu en `expect` (§6) ? | expect/actual avec actual vide / interface injectée via AppContainer | interface : un expect sans actual ne compile pas, et la DI manuelle du §5 suffit | |

## À répondre avant H0

| # | Question | Options | Recommandation | Qui |
| --- | --- | --- | --- | --- |
| Q-001 | Quelle stack ? | Next.js / SvelteKit / Symfony+Turbo | celle que les 3 maîtrisent | H2 |
| Q-002 | Quel challenge sponsor viser ? | aucun / 1 / 2 | 1 principal, annoncé aux tech talks | H1 |
| Q-003 | Qui tient le formulaire de soumission ? | H1 / H2 | H1, backup H2 | H1 |
| Q-004 | Usage payant après la limite hebdo Ultra ? | non / oui plafonné 50 $ / oui 100 $ | non, sauf si un compte est à sec avant 23:00 | H2 |
| Q-005 | Repo public dès maintenant ? | oui/non | oui, pas de secret dans l'historique | H2 |
| Q-006 | Clé API de l'app : crédits Ultra ou autre ? | crédits Ultra / clé perso / sponsor | crédits Ultra (`APP_ANTHROPIC_API_KEY`) | H2 |

## Écarts entre docs (relevés le 21/11 à H0+1, à trancher)

En attendant la réponse, les agents appliquent l'ordre d'autorité d'`AGENTS.md` :
règlement LauzHack, puis `docs/CONTEXT.md`, puis le reste.

| # | Écart | Options | Recommandation | Qui |
| --- | --- | --- | --- | --- |
| Q-007 | `CONTEXT.md` §13 prévoit deux slides (partagé vs spécifique, process agentique) ; le règlement LauzHack interdit les slides (images et vidéos de secours autorisées). | écran « About » dans l'app + sortie du script de comptage / images de secours plein écran / garder les slides | écran dans l'app + sortie du script en live ; aucune slide | H1 |
| Q-008 | `CONTEXT.md` §2 parle d'agents dans « JetBrains Air » ; le talk JetBrains parle de JetBrains AI et de Junie (bonus ~10–15 %, à vérifier) ; l'équipe travaille avec Claude Code. | Claude Code seul / Claude Code + au moins une vraie tâche Junie loggée / tout dans Junie | Claude Code + une tâche Junie réelle dans `AGENT_LOG.md` ; confirmer le nom au stand | H1 |
| Q-009 | Planning : `CONTEXT.md` §10 (portes 13/17/21 h, sommeil 02–07 h, ≥ 3 h chacun) contre `PLAYBOOK.md`/`STRATEGY.md` (squelette déployé à 16:00, shifts de 4 h à 22/02/06 h). | `CONTEXT.md` / `PLAYBOOK.md` / mélange | portes et fenêtre de sommeil de `CONTEXT.md` ; rotation à fixer en sync | tous |
| Q-010 | JGit 6.10.1 au lieu de la dernière stable (`CONTEXT.md` §5) : JGit 7 ne se dexe pas sous l'API 34 (commit `df36830`). | valider / JGit 7 avec minSdk 34 | valider, l'inscrire dans `DECISIONS.md` | H2 |
| Q-011 | Le wizard génère `shared/` + `desktopApp/` + `androidApp/`, pas le `composeApp/` du §6 ; le source set s'appelle `jvmSharedMain`. | valider et corriger le §6 / revenir à `composeApp` | valider, corriger le §6, l'inscrire dans `DECISIONS.md` | H2 |
| Q-012 | `STACK.md`, `ARCHITECTURE.md` (arborescence), `PRD.md`, la toolchain d'`ONBOARDING.md`, `DRY-RUN.md` et `PRE-EVENT-CHECKLIST.md` décrivent encore la base TypeScript. | bandeau « obsolète » en tête / déplacer dans `docs/archive/` / laisser | bandeau en tête (`AGENTS.md` les marque déjà comme historiques) | H2 |
| Q-013 | `DEMO.md` et `GLOSSARY.md` sont encore des templates. | les remplir depuis `CONTEXT.md` §13 et §1 / attendre le feature freeze | les remplir maintenant : DEMO par H1, glossaire (nit, base, lane, ancrage, suggestion) par H2 | H1, H2 |
| Q-014 | `CONTEXT.md` §5 prévoit un expect/actual `SettingsStore` (fichier desktop, SharedPreferences/DataStore Android) ; #18 livre un seul `FileSettingsStore` JSON en `jvmSharedMain` (`java.io.File` existe sur les deux cibles). | valider et corriger le §5 / écrire l'actual Android en SharedPreferences | valider : zéro code plateforme, un seul test ; `DECISIONS.md` | H2 |
| Q-015 | `CONTEXT.md` §10 ajoute « Air installé + premier agent loggé » à la porte 12h–13h, déjà passée ; `AGENT_LOG.md` est vide et #39 fixe la première capture Air à 17:30. | laisser la porte 12h–13h en échec / déplacer la preuve Air à 17:30 (#39) / la mettre à la porte 21:00 | 17:30 via #39, et corriger le tableau du §10 | H2 |
| Q-016 | Slides : `LAUZHACK.md` et `DEMO.md` disent « sans slides » ; `CONTEXT.md` §10 (09h30–11h) et §13 (points 6 et 7) et #39 (« ajouter une slide ») en prévoient encore. Q-007 n'a pas été appliquée au texte. | écran About dans l'app / images plein écran (captures Air, sortie du script de comptage) / garder le mot « slide » | images plein écran + sortie du script en live ; remplacer « slide » dans §10, §13 et #39 | H1 |
| Q-017 | `CONTEXT.md` §11 demande aux agents d'écrire dans `AGENT_LOG.md` à chaque tâche ; `AGENTS.md` et l'en-tête d'`AGENT_LOG.md` disent l'inverse (agents → `progress/<ticket>.md`, un humain compile). Avec Air, qui note les sessions « Junie via Air » ? | agents écrivent / humain compile aux syncs / l'humain qui lance la session Air note la ligne à la fin | l'humain qui lance la session Air, à la fin de la session ; aligner le §11 | H2 |
| Q-018 | Porte 21:00 (boucle review desktop) : #19 (`diffAgainst` encore `TODO`), #20 (Koog : rien d'écrit, pas d'entrée dans `RESEARCH.md`, lib absente du catalogue Gradle) et #22 sont tous les trois sur la même personne à 16:45. | tout garder / répartir #19 à H3 maintenant / couper #34–#38 en WON'T ce soir | #19 à H3 (brief dans l'issue), #20 puis #22 par H2, #33 réduit à « clé manquante » + chargement, #34–#38 après la porte Android | H2 |
| Q-019 | H1 (21/11) : on vise le maximum de points, donc les 5 features bonus. Le backend Kotlin passe de WON'T à MUST (`CONTEXT.md` §7.1 : serveur Ktor de partage de reviews, piste D après la porte de 21:00). Qui porte la piste D, et le contrat `SharedReview`/`ReviewShareClient` du §6 est-il validé ? | H1 via Air / H3 après la boucle Android / couper si la porte de 21:00 glisse | H1 via Air (module isolé, peu de couplage) ; H2 valide le contrat et l'inscrit dans `DECISIONS.md` ; mettre à jour l'arborescence d'`AGENTS.md` | H2 |
| Q-020 | minSdk : #109 l'a passé de 26 à 33 parce que JGit 6.10 appelle des méthodes de l'API 33 (`InputStream.readNBytes`, `transferTo`, `Collectors.toUnmodifiableList`, vérifié dans le jar), mais `CONTEXT.md` §14 parle encore de minSdk 26 et seul un émulateur API 36 a tourné. Le téléphone de démo est-il en Android 13 ou plus (`adb shell getprop ro.build.version.sdk` ≥ 33) ? Sinon l'app ne s'installe plus. | garder 33 / revenir à 26 avec desugaring, à prouver sur un émulateur API ≤ 32 | garder 33 ; vérifier le téléphone, corriger le §14 et l'inscrire dans `DECISIONS.md` | H2 + H3 |
| Q-021 | JetBrains (22/11, réponse au stand) : pas de clés API fournies aux équipes ; Junie Lite est gratuit avec un compte JetBrains jusqu'à fin d'année. La clé Anthropic de l'app reste la nôtre (Q-006). | rien à changer / passer la preuve Junie de #39 par Junie Lite / le dire dans `DEMO.md` « réutilisé » | Junie Lite pour la preuve #39, une ligne dans `DEMO.md` ; scope inchangé | H1 |

## Règles

- Une question = une décision à prendre, pas un sujet de discussion.
- Toujours proposer 2–3 options et **ta** recommandation.
- Si la réponse ne change pas le résultat, choisis et note l'hypothèse dans le commit.
- Ne jamais mettre un secret dans une question.
