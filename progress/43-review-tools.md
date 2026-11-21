# Ticket 43-review-tools

- Statut : preuve réelle acquise ; correctif du tool loop en PR
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
- 22:05 — preuve réelle via le settings de l'app (clé jamais lue par l'agent) :
  `read_file Pricing.kt` déclenché, puis la review a répondu. Deux bugs de boucle révélés :
  le bloc `Reasoning` d'Anthropic était réinjecté dans le tour suivant, et les arguments
  de `toolResult` étaient dans le mauvais ordre (`id`/`tool`/`output`). Corrigés ; ids
  d'outils normalisés en `tool_N`.
- 19:55 — auto-review : `read_file` lisait encore les fichiers ignorés/binaires et
  `.env` ; seuls les fichiers suivis et reviewable passent maintenant, `.git` est
  case-insensitive, les listings comptent dans le budget. Tests `.env`, `.png`, NUL,
  `.GIT` et chemin tool-capable ajoutés. **119 tests, 0 échec**.

## Décisions

- Les outils restent **read-only** et passent par `GitRepository` ; aucun accès filesystem
  direct.
- Le mode sans outils reste le défaut : un client non `ToolCallingLlmClient` ne voit pas la
  section Tools et n’appelle jamais `completeWithTools`.
- 6 appels / 64 Ko, 4 rounds : assez pour une signature ou un test voisin, sans exploser le
  coût ni le temps de review.
- Le plafond est en octets (`encodeToByteArray`), pas en caractères.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : **119 tests, 0 échec**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- `./scripts/check.sh` complet non lancé (pas de SDK Android ici).

## Incertain

- **Preuve réelle** : `TOOL_USE read_file Pricing.kt` obtenu le 21/11 à 22:05 avec le
  settings de l'app (clé non exposée). Le test manuel reste à consigner dans le ticket.
- Le loop Koog lui-même n’est couvert que par le chemin `ToolCallingLlmClient`/toolbox, pas
  par un faux `LLMClient` ; l’appel réel reste la preuve attendue.
- Le support tool-calling varie selon le fournisseur ; le repli sans outils couvre le cas.
