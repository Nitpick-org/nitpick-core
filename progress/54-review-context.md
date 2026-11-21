# Ticket 54-review-context

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : A core partagé
- Agent : audit du 21/11 → lead
- Model : session principale
- Escalations : aucune
- Fichiers : `shared/src/commonMain/.../review/ReviewContext.kt`, `commonTest/.../review/ReviewContextTest.kt`

## Journal

- 18:05 — audit : la review prenait les 20 premiers fichiers alphabétiques ; deux sidecars
  Godot `.import` passaient avant le code source.
- 18:10 — `selectReviewDiffs` trie par nombre de lignes ajoutées/supprimées, puis par chemin
  (déterministe) ; `buildReviewContext` l'utilise. Les assets/imports sont filtrés par
  `isReviewable`.
- 18:15 — tests : 25 diffs dont 5 gros fichiers en fin d'alphabet ; les 5 sont retenus ;
  `hero.png.import` et `logo.png` ne sont pas reviewable.

## Décisions

- Le tri est **décroissant sur les lignes changées**, pas sur la taille du fichier, pour
  rester aligné sur le prompt (diff + contenu complet).
- Les assets ne sont pas envoyés au modèle : la review doit voir le code, pas les sidecars.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : **74 tests, 0 échec**.
- `:desktopApp:compileKotlin` non relancé après ce changement (commonMain pur) ; tests JVM OK.

## Incertain

- Le tri des fichiers modifiés ne change pas le diff affiché, seulement le contexte du modèle.
- Les fichiers `.import` peuvent contenir des métadonnées utiles ; on les ignore volontairement
  pour le budget de prompt.

## Suivant

- Rejouer la review réelle sur le repo de démo (2 sidecars ne doivent plus sortir).
