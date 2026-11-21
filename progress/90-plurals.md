# Ticket 90-plurals

- Statut : en cours
- Propriétaire humain : @pepito2t (H3), pris à H1 pour le laisser sur les P0 démo
- Piste : C android+finitions
- Agent : Claude Code, session humaine
- Model : Fable 5.1
- Escalations : aucune
- Fichiers autorisés : composeResources/values{,-fr}/strings.xml, les 4 écrans qui lisent ces chaînes, un test commonTest

## Journal

- 00:05 — worktree créé.
- 00:15 — `status_commits`, `commit_bar_applied` passent en `<plurals>` ; `review_nits_on_files` portait deux quantités (nits, fichiers) qu'un seul `<plurals>` ne peut pas accorder : scindé en `review_nits` + `review_changed_files`, concaténés dans `ReviewPanel`. Lecture avec `pluralStringResource` (Compose resources, un seul chiffre à accorder par chaîne).
- Test : `CountStringsTest` (jvmTest) lit les deux formes EN/FR dans les fichiers `strings.xml`. Le lookup Compose (`getPluralString`) exige un affichage sur desktop et `ResourceEnvironment` est interne : pas testable en headless.

- 00:40 — review ponytail : `diff_files` (« 1 files », en-tête du diff desktop) passe aussi en pluriel ; `review_summary` (« %1$d nits · %2$d bug ») n'avait plus aucun appelant, supprimé en EN et FR.

## Incertain

- Le français a la même règle `one`/`other` que l'anglais pour ces mots ; aucune autre langue n'est fournie.
