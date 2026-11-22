# Ticket 122 — Phone UI/UX pass

- Statut : en cours (vérification émulateur)
- Propriétaire humain : @pepito2t (H3)
- Piste : C android + finitions
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Fichiers autorisés : `ui/phone/*`, `ui/components/NitBottomSheet.kt`, `ui/repository/CommitGraph.kt`
  (paramètre `compactTime`), `ui/repository/CommitTimeFormat*.kt`, `composeResources/drawable/ic_github.xml`,
  `ic_gitlab.xml`

## Fait

- Bottom sheet d'un nit : le corps défile, Dismiss / Apply restent sous le pouce (sur une longue
  suggestion ils partaient hors écran, vu au dogfooding).
- Lignes de commits : date compacte sur le téléphone (`HH:mm` aujourd'hui, `d MMM` cette année,
  `MMM yyyy` avant) ; la date pleine mangeait le sujet sur 393 dp.
- Barre du haut : actions en icônes 44 dp (engrenage Settings, nuage Shared) au lieu de texte ;
  titre + sous-titre sur Review (« Review » / « branche vs main ») au lieu d'un titre tronqué.
- Icônes sur les boutons : fetch / pull / push sur la carte de branche, pince sur « Nitpick
  branch », check sur « Stage & commit », push sur « Push », nuage sur « Clone repo ».
- Marques GitHub / GitLab (Simple Icons, CC0, en vector XML) sur le choix de forge, le bouton
  « Se connecter avec… » et à côté de `@login` dans la barre Repositories.
- Graph : la ligne de statut sync a un séparateur et de l'air, elle ne colle plus à la liste.
- Carte repo : le chemin interne `/data/user/0/…` est retiré, nom + chevron.
- Résumé de review en `body` au lieu de `section` (4 lignes grasses pesaient).

## Non fait

- Icône « merge » : aucune action de merge dans l'app, rien à illustrer.
- Carte repo avec branche, dernière review et badge (handoff §4.1) : il faudrait stocker plus
  qu'un chemin dans `Settings.recentRepositories` (contrat), à part.
- Settings et Diff téléphone non revus.

## Preuves

- `check.sh` vert (UI seulement, pas de test ajouté : pas d'infra de test Compose).
- Émulateur : captures `u0*` du scratchpad (voir journal).

## Journal

- 01:20 — ticket, worktree, relecture des écrans et du handoff §4.
- 01:40 — changements, check vert.
