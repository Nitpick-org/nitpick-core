# Ticket 119 — Pushed confirmation, review-aware commit message, suggestions that fix stale comments

- Statut : fait, à merger
- Propriétaire humain : @pepito2t (H3)
- Piste : A core (prompt) + B/C UI desktop et téléphone
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Fichiers autorisés : `ui/repository/{RepositoryViewModel,SyncStatus,PushedBanner,CommitMessage,ReviewPanel}.kt`,
  `ui/phone/PhoneReviewScreen.kt`, `review/{ReviewPrompt,ReviewResponseParser}.kt`, `domain/Review.kt`
  (champ optionnel `title`), strings EN/FR, `docs/demo/android-09*`, `android-10*`, `docs/DEMO.md`

## Fait

- `RepositoryUiState.lastPushedBranch` : posé au succès d'un push ou d'un publish, effacé au
  commit, fetch, pull, checkout et au début du push suivant. `PushedBanner` (check qui « pop »
  en spring, ligne qui fond en 400 ms) dans le bas de l'écran Review téléphone et du panneau
  Review desktop ; `syncStatusMessage` dit « Pushed to origin/branch » dans la barre de statut
  desktop et sur le Graph téléphone.
- `commitMessageFor(applied)` : `fix:`/`refactor:` + titres des nits appliqués (champ `title`
  demandé au modèle, repli sur la première phrase du message), 72 caractères max. Recalculé à
  chaque Apply tant que l'utilisateur n'a pas touché le message.
- Prompt : « widen startLine..endLine to include a comment the fix makes wrong ». Sur
  l'émulateur le modèle a préféré des nits séparés pour les commentaires périmés : résultat
  équivalent, les commentaires faux sont corrigés.

## Preuves

- `check.sh` vert ; `CommitMessageTest` 3, `ReviewPromptTest` 5, `ReviewResponseParserTest` 3,
  `RepositoryViewModelTest` (lastPushedBranch après push, message `fix: wrong lane`).
- Émulateur : second passage sur `pepito2t/OpenTab` (`perf(mru)` fautif) → 3 nits dont 2 sur
  les commentaires périmés → sujet « fix: correct eviction comment, restore mru.count base for
  unseen ranks » → push → bannière, commit `f91a419` sur GitHub.
- Non vérifié à l'écran : desktop (même composable, même `syncStatusMessage`).

## Journal

- 00:50 — ticket, worktree, lecture du push/commit/prompt.
- 01:00 — implémenté, check vert, rejoué sur l'émulateur avec captures.
