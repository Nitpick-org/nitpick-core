# Ticket 38-icons

- Statut : prêt pour PR
- Propriétaire humain : @zooldyyk
- Piste : C (design system)
- Agent : lead
- Model : session principale
- Escalations : aucune
- Fichiers : `shared/src/commonMain/composeResources/drawable/*.svg`,
  `ui/components/{NitpickIcon,NitpickIdeShell}.kt`, `ui/repository/{RepositoryScreen,DiffPanel}.kt`,
  `ui/NitpickAppIcon.kt`, `desktopApp/.../main.kt`, `androidApp/src/main/res/**`

## Journal

- 21:05 — 15 SVG 24 px, trait 1,8, repris des `path` du bloc `ic` de la maquette :
  branch, cloud, tag, chevron, gear, folder, key, wifi-off, clock, check, x, tweezers,
  fetch, pull, push.
- 21:10 — `NitpickIcon` (16 dp, tint du contexte) ; chevrons de sections et icônes
  branche/cloud/tag dans l'arbre ; pince sur le CTA ; engrenage sur Settings ; chevron
  sur le sélecteur de base.
- 21:15 — icône d'app pince teal : Android foreground/background + mipmaps legacy ;
  desktop via `nitpickAppIcon()` (ressource Compose partagée, pas de painterResource
  déprécié).

## Décisions

- Les icônes sont des SVG Compose Resources, pas des ImageVector Kotlin : un seul fichier
  par icône, tint au rendu, pas de dépendance Material Icons.
- Les icônes adjacentes à un texte ont `contentDescription = null` (le texte porte le sens) ;
  `NitpickIcon` rend le paramètre obligatoire pour éviter les icônes seules muettes.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : **139 tests, 0 échec**.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- `:androidApp:assembleDebug` bloqué par l'absence de SDK Android dans cet environnement.

## Incertain

- Rendu visuel 1×/2× et dock macOS non vérifiés à l'écran.
- Les PNG legacy Android sont générés, mais seuls les mipmaps `anydpi-v26` utilisent le
  foreground vector ; à contrôler sur un émulateur.
