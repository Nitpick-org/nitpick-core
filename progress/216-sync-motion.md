# Ticket 216 — Animated sync icons

- Statut : fait
- Propriétaire humain : @pepito2t (H3)
- Piste : B desktop + C android (même composant)
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Fichiers autorisés : `ui/components/SyncIcon.kt`, `ui/phone/PhoneGraphScreen.kt`, `ui/repository/RepositoryScreen.kt`

## Fait

- `SyncIcon(resource, motion, active, tint)` : immobile tant que `active` est faux (aucune frame
  dépensée) ; sinon une transition infinie : Fetch tourne (900 ms, linéaire), Pull descend et
  remonte de 3 dp (450 ms, aller-retour), Push pareil vers le haut.
- Téléphone : la carte de branche branche `isFetching` / `isPulling` / `isPushing` ; le bouton actif
  reste à pleine opacité pendant l'opération. Desktop : les trois actions de la barre reçoivent
  l'icône animée en `leading` (elles n'avaient pas d'icône).

## Non fait

- Pas de test (animation) ; non vu à l'écran : émulateur fermé.

## Journal

- 03:10 — composant, câblage, check vert.
