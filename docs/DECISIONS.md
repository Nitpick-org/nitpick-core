# Décisions

> Une ligne par décision durable : quoi, pourquoi, qui. Les agents proposent ; H2 (ou le
> propriétaire du domaine) tranche et écrit. Jamais de débat dans ce fichier.

## À trancher avant H0

| # | Décision | Options | Recommandation | Qui | Deadline |
| --- | --- | --- | --- | --- | --- |
| D-001 | Stack | Next.js / SvelteKit / Symfony+Turbo | celle que les 3 savent déboguer | H2 | J-4 |
| D-002 | Idée | shortlist de 3 | scoring `STRATEGY.md` | H1 | H0 |
| D-003 | Challenge sponsor | 0 / 1 / 2 | 1 principal | H1 | 11:40 sam |
| D-004 | Rôles | H1/H2/H3 | produit / archi / builder | tous | J-5 |
| D-005 | Sommeil | shifts 3 h | 00/03/06 | tous | J-1 |
| D-006 | Repo public | oui/non | oui (lien de soumission) | H2 | J-1 |
| D-007 | Budget API | plafond | à définir | H2 | J-1 |
| D-008 | Qui soumet | H1/H2 | H1, avec H2 en backup | tous | H0 |

## Décisions prises

| Heure | Décision | Pourquoi | Qui |
| --- | --- | --- | --- |
| 12:28 | Projet `shared/` + `desktopApp/` + `androidApp/` (wizard), pas de `composeApp/` ; source set intermédiaire `jvmSharedMain` | C'est ce que le wizard génère ; une seule implémentation JGit pour desktop et Android (Q-011) | H2 |
| 12:33 | JGit 6.10.1, pas la 7 | JGit 7 utilise des records Java que le dexer refuse sous l'API 34 (Q-010) | H2 |
| 15:37 | Un seul `FileSettingsStore` JSON en `jvmSharedMain`, pas d'expect/actual ni SharedPreferences | `java.io.File` existe sur les deux cibles : zéro code plateforme, un seul test (Q-014, #18) | H2 |
| 16:09 | DA JetBrains New UI (`DESIGN-SYSTEM.md`), accent unique teal `#0F766E` / `#5EEAD4`, l'ancien bleu passe en lane 5 | Outil de dev aligné sur l'IDE du challenge ; le teal distingue Nitpick (#32) | H2, H3 |
| 16:28 | JetBrains Air + Junie/Codex/Claude comme process de développement ; Koog reste le moteur du produit, aucune intégration runtime d'Air | Bonus « process agentique » du challenge sans dépendance dans l'app (Q-008) | H2 |
| 16:50 | Une branche de PR se rebase sur `main` et se pousse en `--force-with-lease` ; le force push reste interdit sur `main` | Plus de branches `-rebased` ni de PR dupliquées (#28) | H2 |
| 17:40 | Backend Kotlin **local** en SHOULD : module `server/` Ktor, cache et partage des `ReviewResult` entre desktop et Android, sans déploiement, sans LLM ni clé côté serveur, optionnel dans les réglages | Cinquième bonus du jury sans toucher au pitch « review en local » ni sortir Koog de `commonMain` ; après la porte 02:00 | H2 |

## Format d'une ligne

```md
| 14:32 | Session cookie plutôt que JWT | Plus simple pour la démo, pas de refresh | H2 |
```

Si une décision mérite plus qu'une ligne, écris un court paragraphe sous le tableau et
lie-le au ticket concerné. Les agents ne modifient pas ce fichier.
