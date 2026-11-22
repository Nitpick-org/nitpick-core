# Ticket 213 — Phone: gestures and rotation

- Statut : fait, à vérifier sur l'émulateur (fermé au moment du commit)
- Propriétaire humain : @pepito2t (H3)
- Piste : C android
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Fichiers autorisés : `androidApp/src/main/AndroidManifest.xml`, `ui/phone/{PhoneGraphScreen,PhoneReviewScreen}.kt`,
  `ui/components/NitBottomSheet.kt`

## Fait

- Rotation : le verrou portrait du manifest est retiré. Le flux téléphone est déjà choisi par
  `cloneRoot` (pas par la largeur), les ViewModels survivent à la recréation et les états d'écran
  sont en `rememberSaveable` (écran courant, onglet, nit ouvert). Le jeton saisi dans Connect reste
  volontairement hors du bundle.
- Pull-to-refresh sur la liste des commits = Fetch (`PullToRefreshBox` Material3, indicateur lié à
  `isFetching`, ignoré pendant une autre sync).
- Swipe horizontal dans la sheet d'un nit : vers la gauche → nit suivant, vers la droite → précédent
  (seuil 120 px, un scroll vertical ne déclenche rien). Le geste retour système suivait déjà
  `PhoneScreen.parent` ; la sheet se ferme au glissement vers le bas (Material3).

## Non fait

- Vérification à l'écran en paysage (carte de branche, liste, sheet, réglages) : émulateur fermé.

## Journal

- 03:20 — worktree, manifest, pull-to-refresh, swipe, check vert.
