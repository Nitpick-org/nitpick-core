# Ticket 230-audit-share

- Statut : en cours
- Propriétaire humain : @pepito2t (H3)
- Piste : B partage (serveur Ktor + client)
- Agent : Claude Code, session humaine ; audit par un sous-agent Explore en lecture seule
- Model : Fable 5.1
- Fichiers autorisés : server/, share/, domain/ShareResponse.kt, settings/Settings.kt, ui/settings/{SettingsFields,SettingsDialog,SettingsScreen}.kt, ui/repository/{RepositoryViewModel,ShareReview}.kt, strings EN/FR, tests associés

## Journal

- 09:10 — audit ponytail (demande H3), volet partage : 7 constats vérifiés dans le code ; issue #230.
- 09:30 — corrigé : badge « Shared » posé seulement sur la review envoyée (course Share puis Review), refus HTTP distingué de « injoignable » (`ShareFailure.Rejected(status)`, phrase EN/FR), deux formes d'URL de plus normalisées sur une seule clé, token dans la query string retiré côté client, lecture du store au démarrage qui ne met en quarantaine que les erreurs JSON (sauvegarde datée), `ShareResponse` une seule fois dans `domain/`.
- 09:40 — mode LAN : le serveur refuse de démarrer sur un bind non-loopback sans `NITPICK_TOKEN` et exige `Authorization: Bearer` sur les routes `/reviews` ; les apps envoient le token depuis un nouveau réglage « Token du serveur » (desktop et téléphone). Localhost inchangé.
- Tests ajoutés (tous exécutés) : token exigé en mode LAN (401 / 201), formes d'URL, 413 porté par l'exception et absent quand injoignable, en-tête Bearer envoyé seulement si un token est réglé, query string retirée, refus → `Rejected(413)`, review relancée pendant l'envoi → pas de badge.

## Décisions

- Le token est un champ texte simple dans les réglages, pas un champ masqué : secret de démo partagé par l'équipe, jamais un identifiant personnel.
- `Settings.serverToken` a une valeur par défaut vide : un fichier de réglages existant se charge tel quel.
