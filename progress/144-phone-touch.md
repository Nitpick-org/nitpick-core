# Ticket 144-phone-touch

- Statut : en cours
- Propriétaire humain : @pepito2t (H3)
- Piste : C android+finitions
- Agent : Claude Code, session humaine
- Model : Fable 5.1
- Escalations : aucune
- Fichiers autorisés : shared/src/commonMain/.../ui/phone/{PhoneFlow,ReposScreen,PhoneReviewScreen,ConnectScreen}.kt

## Journal

- 02:20 — trouvé par la lane UX de l'audit (#130) : `NitpickToolbarAction` fait 28 dp (`sizes.control`) ; sur le téléphone Back, Re-run et l'action compte, plus le bouton Show/Hide du token (texte + 4 dp), sont sous les 44 dp du handoff §0.5.
- 02:30 — `PhoneTouchTarget = 44.dp` dans `PhoneFlow.kt` ; `Modifier.heightIn(min = PhoneTouchTarget)` sur les trois actions (la hauteur 28 dp de `NitpickToolbarAction` est appliquée après, la contrainte externe gagne) et sur le toggle du token (`wrapContentHeight` pour garder le texte centré). La barre du téléphone fait 48 dp, les actions tiennent.
- Mesuré sur l'émulateur (420 dpi, uiautomator) : Back 48 dp, action compte 43,4 dp (arrondi de 44), Settings 44 dp.

## Incertain

- Rien.
