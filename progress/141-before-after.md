# Tickets 141 + 142 — Before/after in the nit card, line number never truncated

- Statut : fait (non vu à l'écran : émulateur fermé)
- Propriétaire humain : @pepito2t (H3)
- Piste : B desktop + C android (même composant)
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Fichiers autorisés : `ui/components/NitpickReview.kt` (`ReviewCommentCard`), ses appelants
  `ui/repository/ReviewPanel.kt` et `ui/components/NitBottomSheet.kt`

## Fait

- `ReviewCommentCard` prend `original` (le `originalSnippet` du nit) et rend le bloc « Suggested
  change » comme un diff : lignes `-` sur `diffRemovedBg`, lignes `+` sur `diffAddedBg`, police
  code. Desktop et téléphone passent `comment.originalSnippet`.
- L'ancre : le chemin et `:line` sont deux textes ; le chemin rétrécit avec une ellipse, le numéro
  de ligne reste toujours visible (#142).

## Non fait

- Pas de test : composant Compose pur, pas d'infra de test UI dans le repo. À regarder sur
  l'émulateur ou le desktop avant merge.

## Journal

- 02:15 — worktree, carte avant/après, check vert.
