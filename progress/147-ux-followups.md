# Ticket 147-ux-followups

- Statut : en cours
- Propriétaire humain : @pepito2t (H3)
- Piste : C finitions
- Agent : Claude Code, session humaine
- Model : Fable 5.1
- Escalations : aucune
- Fichiers autorisés : ui/components/NitpickReview.kt, ui/repository/{ReviewPanel,RepositoryViewModel}.kt, ui/phone/PhoneReviewScreen.kt, strings EN/FR, docs/demo/*.png

## Journal

- 02:30 — état sur `main` : les points 2 (pluriels `shared_row`, `review_files_with_nits`) et 3 (« : » en dur) sont déjà corrigés par d'autres commits ; les compteurs par sévérité existent déjà sur desktop mais pas sur le téléphone ; le badge reste en ton Info.
- 02:40 — point 4 : `SeverityCounters(comments)` partagé dans `NitpickReview.kt` (bugs d'abord), utilisé par la carte résumé desktop et téléphone ; badge « AI review » en ton `Ai`, « Shared review » reste Info.
- 02:45 — point 1, la part qui touche la démo : une réponse du modèle qui n'est pas une review valide (`ReviewResponseException`) devient `ReviewFailure.InvalidAnswer`, affichée par une phrase EN/FR au lieu du texte de l'exception. Les autres messages (JGit, apply, clone) restent tels quels : ce sont nos propres phrases anglaises ou le détail JGit, utiles tels quels et hors budget pour cette nuit.
- Test : `anInvalidModelAnswerIsShownAsItsOwnFailureNotRawText`.

## Incertain

- Point 5 (captures desktop apply/commit/push clair + sombre) : fait dans cette PR si le desktop se lance ; les trous de `DEMO.md` (phrase, créé vs réutilisé) sont du contenu pitch pour H1 (#163).
