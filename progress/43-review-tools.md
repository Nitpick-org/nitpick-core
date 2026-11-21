# Ticket 43-review-tools

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : A core partagé
- Agent : lead
- Model : session principale
- Escalations : aucune
- Fichiers : `shared/src/commonMain/.../review/{ReviewToolbox,ToolCallingLlmClient,KoogLlmClient,LlmReviewAgent,ReviewPrompt}.kt`,
  `commonTest/.../review/ReviewToolboxTest.kt`

## Journal

- 19:25 — `ReviewToolbox` : `list_files` + `read_file`, chemins relatifs uniquement
  (absolu, `..`, `.git` refusés), plafond 6 appels et 64 Ko lus par review.
- 19:30 — `ToolCallingLlmClient` ; `KoogLlmClient` fait une boucle native Koog
  (`ToolDescriptor` + `MessagePart.Tool.Call` + `PromptBuilder.toolResult`), 4 rounds max.
  Si le premier appel avec outils échoue, repli sur le prompt sans outils.
- 19:35 — `LlmReviewAgent` passe la toolbox seulement si le client supporte les outils ;
  le prompt ajoute la section Tools dans ce cas. Tests : 6 cas de garde-fous.
- 19:38 — **118 tests JVM, 0 échec** ; desktop compile.

## Décisions

- Les outils restent **read-only** et passent par `GitRepository` ; aucun accès filesystem
  direct.
- Le mode sans outils reste le défaut : un client non `ToolCallingLlmClient` ne voit pas la
  section Tools et n’appelle jamais `completeWithTools`.
- 6 appels / 64 Ko, 4 rounds : assez pour une signature ou un test voisin, sans exploser le
  coût ni le temps de review.
- Le plafond est en octets (`encodeToByteArray`), pas en caractères.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : **118 tests, 0 échec**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- `./scripts/check.sh` complet non lancé (pas de SDK Android ici).

## Incertain

- **Appel réel non rejoué** : il faut une clé et un diff qui référence un autre fichier pour
  prouver qu’un `read_file` est déclenché. À faire avant de fermer #43.
- Le support tool-calling varie selon le fournisseur ; le repli sans outils couvre le cas.
