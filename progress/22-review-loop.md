# Ticket 22-review-loop

- Statut : prêt pour PR (boucle locale), vrai agent Koog encore à brancher (#20)
- Propriétaire humain : @zooldyyk
- Piste : B + A (desktop + core)
- Agent : lead, sans session Air (à compléter pour le bonus)
- Model : session principale
- Escalations : aucune
- Fichiers : `shared/src/jvmSharedMain/.../JGitRepository.kt`, `commonMain/review/*`, `commonMain/ui/repository/*`, `commonMain/composeResources/values*`, tests jvm/common

## Journal

- 17:30 — JGit : `diffAgainst` (base → working tree), `writeFile`, `stage` (add/rm),
  `commit`. Tests sur dépôt temporaire, y compris une modification non commitée.
- 17:45 — `ReviewAnchoring` : applique une suggestion en vérifiant `originalSnippet`,
  recale si les lignes ont bougé, refuse si le snippet a disparu. Tests.
- 18:00 — `LocalReviewAgent` : fallback déterministe (println, TODO/FIXME, `!!`, lignes
  > 120) derrière `ReviewAgent`. Ce n'est pas le modèle final : il permet de construire et
  démontrer la boucle sans clé API.
- 18:15 — UI : onglets Diff et Review, liste des fichiers/hunks/lignes, cartes de nits,
  Apply/Dismiss, barre de commit. Le CTA lance la review locale.
- 18:25 — `RepositoryViewModel` : diff au chargement, review, apply, dismiss, commit,
  rechargement du graph/diff.

## Décisions

- `diffAgainst` compare la base au **working tree** (pas seulement HEAD), pour que le diff
  se rafraîchisse après Apply.
- L'agent local est explicitement un **fallback** ; le contrat `ReviewAgent` ne change pas
  quand Koog arrive.
- Un chemin de suggestion reste validé par snippet avant écriture.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : JGit 15 tests, anchoring 4, local review 1,
  diff parser 9, graph 8, settings 8, VM 4 — **0 échec**.
- `:shared:compileKotlinJvm` et `:desktopApp:compileKotlin` verts.
- Check complet non lancé : pas de SDK Android dans cet environnement.

## Incertain / blocage

- **#20 non fait** : pas de Koog, pas d'appel réel ; l'agent local ne remplace pas le
  critère « shared AI logic ».
- Pas de preuve Air pour ce ticket ; à faire par H2/H1 avant la démo.
- L'UI Apply/Commit n'a pas été rejouée à la main sur un écran (pas de GUI ici).

## Suivant

1. Brancher `KoogReviewAgent` derrière `ReviewAgent` (#20), avec les settings.
2. Rejouer la boucle sur le repo de démo et capturer (Air + desktop).
3. Ajouter le mode « Review en cours » / erreurs du handoff (#33).
