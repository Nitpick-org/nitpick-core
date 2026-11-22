# Ticket 212-desktop-icons

- Statut : en cours
- Propriétaire humain : @pepito2t (H3)
- Piste : C finitions desktop
- Agent : Claude Code, session humaine
- Model : Fable 5.1
- Fichiers autorisés : ui/components/{NitpickButton,NitpickIdeShell,NitpickLayout}.kt, ui/repository/{RepositoryScreen,ReviewPanel,DiffPanel,ShareReview}.kt, drawable/ic_share.xml

## Journal

- 03:20 — `icon: DrawableResource?` sur `NitpickButton`, `NitpickToolbarAction` et l'action de `NitpickEmptyState` : l'icône prend la couleur du texte du bouton, donc une seule ligne par site d'appel au lieu d'une lambda `leading` avec le tint à la main.
- 03:25 — barre d'outils : dossier (Open), nuage (Clone), fetch, pull, push ; écran vide : deux boutons « Open repository… » (primaire) et « Clone repository… » (secondaire) ; panneaux : coche sur Stage & commit et Apply all, partage sur Share review, flèches circulaires sur Retry et Refresh.
- 03:35 — vérifié à l'écran sur desktop (écran vide, barre d'outils, carte de review avec « Share review »). Stage & commit et Apply all non vus : il faut une suggestion applicable ; même code que les autres.

## Décisions

- Pas de nouveau glyphe pour Retry/Refresh : `ic_fetch` est déjà une double flèche circulaire.
- Les `leading` existants (engrenage, pince, chevron) restent : pas de refactor au-delà du ticket.

- 03:50 — demande H3 en cours de route : « Open… » / « Clone… » dans la barre (`toolbar_open`, `toolbar_clone`, les libellés longs restent sur l'écran vide) ; `rememberIconPop()` dans `NitpickIcon.kt` : au clic l'icône se contracte puis revient par un ressort, branché dans `NitpickButton` et `NitpickToolbarAction`. Vu à l'écran : Fetch cliqué, pas de plantage ; le rebond lui-même ne se capture pas en screenshot.
