# Tickets 143 + 144 — Phone touch targets

- Statut : 144 fait ; 143 fermé « non reproductible » depuis #133 (sheet pleine hauteur, insets M3)
- Propriétaire humain : @pepito2t (H3)
- Piste : C android
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Fichiers autorisés : `ui/phone/{PhoneFlow,PhoneReviewScreen,ReposScreen,ConnectScreen}.kt`

## Fait

- `PhoneTextAction` : `NitpickToolbarAction` avec `heightIn(min = 44.dp)` ; utilisé pour Back,
  Re-run et le compte `@login`. Settings et Shared étaient déjà des icônes 44 dp (#133).
- Bascule Afficher / Masquer du jeton : boîte de 48 dp cliquable, texte centré.

## Non fait

- Pas de test (UI). Le `heightIn(min)` externe l'emporte sur le `height(28.dp)` interne du contrôle
  desktop, c'est le contrat des SizeNode Compose ; à vérifier d'un coup d'œil sur l'émulateur.

## Journal

- 02:25 — worktree, 143 fermé avec preuve, 144 fait, check vert.
