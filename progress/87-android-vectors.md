# Ticket 87-android-vectors

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : C (Android)
- Agent : lead
- Fichiers : `shared/src/commonMain/composeResources/drawable/ic_*.xml`,
  suppression des `ic_*.svg`

## Journal

- 23:20 — #87 reproduit : Compose Resources ne décode pas SVG sur Android
  (`Android platform doesn't support SVG format`). Conversion des 15 icônes UI en
  `VectorDrawable` XML 24 dp, même `pathData`, trait 1,8, bouts ronds.
- Les app icons `ic_app_light/dark.png` restent des PNG (desktop/dock), hors du chemin
  Android.

## Décisions

- Un seul format pour les icônes UI : XML vectoriel, décodé par Compose sur desktop et
  Android. Plus aucun SVG dans `composeResources`.
- Aucun changement de code : `painterResource` accepte déjà les VectorDrawables.

## Vérifications

- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- `:androidApp:assembleDebug` non lancé (SDK absent) ; validation Branches à faire sur
  l'émulateur.

## Incertain

- Rendu exact des paths (couleur/trait) à vérifier sur Android ; le tint est appliqué par
  `NitpickIcon`.
