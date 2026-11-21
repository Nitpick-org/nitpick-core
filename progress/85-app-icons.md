# Ticket 85 — Tweezers app icon, light and dark variants

- Statut : en cours
- Propriétaire humain : @pepito2t (H3)
- Piste : C android + finitions (touche aussi desktopApp)
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Escalations : aucune
- Fichiers autorisés : `docs/design/icon/`, `shared/src/commonMain/composeResources/drawable/ic_app_*.png`,
  `shared/src/commonMain/kotlin/dev/nitpick/app/ui/NitpickAppIcon.kt`, `ui/theme/NitpickTokens.kt`
  (champ `isDark`), `androidApp/src/main/res/{drawable,mipmap-*}`, `desktopApp/dock-icon.png`

## Fait

- `docs/design/icon/` versionné : les 11 fichiers fournis par H3 (tuile claire `#0F766E`, tuile
  sombre `#1E1F22`), source de vérité.
- In-app (fenêtre desktop, états vides desktop et téléphone) : `ic_app_light.png` /
  `ic_app_dark.png`, choisis par `NitpickTheme.colors.isDark`, donc par le réglage de thème de
  l'app, pas par le système.
- Dock macOS : `dock-icon.png` régénéré depuis la tuile claire avec la marge ~18 % de 9f1c847.
- Launcher Android : retour aux vecteurs de #74 (fond `#0F766E`, pince + point en
  `ic_launcher_foreground.xml`), PNG de premier plan supprimés ; les mipmaps legacy (< API 26,
  jamais servis avec minSdk 26) régénérés depuis la tuile claire pour ne laisser aucune loupe.

## Décisions

- Remplace la loupe de cfcb228 (H2, 22:23) sur demande explicite de H3 : à synchroniser avec H2.
- PNG plutôt que SVG pour l'icône in-app : `painterResource` ne rend pas les SVG sur Android
  (doc officielle des ressources Compose), et `ReposScreen` tourne sur le téléphone.
- Launcher et dock gardent la tuile claire : ni Android ni le dock macOS ne changent d'icône
  avec le thème. Non fait, à ouvrir si utile : `<monochrome>` (icône thématisée Android 13+),
  `nativeDistributions.iconFile` pour les builds packagés.

## Preuves

- `check.sh` vert ; `NitpickAppIconTest` exécuté (1 test, jvm + androidHostTest).
- APK installé sur l'émulateur `nitpick` : tiroir d'applications avec la nouvelle tuile.
- Non vérifié à l'écran : l'état vide in-app (l'émulateur a déjà des dépôts) et la fenêtre desktop.

## Journal

- 22:59 — worktree créé.
- 23:00 — assets posés, vecteurs launcher, `isDark`, test ; check vert ; capture émulateur.
