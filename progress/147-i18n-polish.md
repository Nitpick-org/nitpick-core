# Ticket 147 (part) — i18n polish: plurals and label/detail format

- Statut : points 2 et 3 faits ; 1, 4, 5 restent post-freeze
- Propriétaire humain : @pepito2t (H3)
- Piste : A core (strings) + UI
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Fichiers autorisés : strings EN/FR, `ui/repository/{SyncStatus,ReviewPanel,ShareReview}.kt`,
  `ui/phone/PhoneReviewScreen.kt`, `CountStringsTest.kt`

## Fait

- `review_files_with_nits` et `shared_row` deviennent des `<plurals>` (« 1 nit », « 1 fichier
  avec nits ») ; `CountStringsTest` les couvre dans les deux langues.
- `label_detail` (« %1$s: %2$s » en EN, « %1$s : %2$s » en FR) remplace les quatre jointures
  `+ ": " +` en Kotlin (statut sync, erreur de push téléphone, outils utilisés).

## Reste (post-freeze)

- Texte d'exception brut dans l'UI (1 h), compteurs de sévérité sur le résumé (1 h), captures
  desktop clair/sombre (15 min, à faire avec l'app desktop ouverte).

## Journal

- 02:35 — worktree, pluriels, format label/detail, check vert.
