# Ticket 22-review-loop

- Statut : prêt pour PR, branché sur l’agent Koog mergé dans #51
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
  Apply/Dismiss, barre de commit. Le CTA lance la review.
- 18:25 — `RepositoryViewModel` : diff au chargement, review, apply, dismiss, commit,
  rechargement du graph/diff.
- 18:35 — rebase sur #51 : `AppContainer.reviewAgent` (LlmReviewAgent + KoogLlmClient) est
  passé au ViewModel. Le vrai agent Koog est donc le chemin de production ; `LocalReviewAgent`
  ne reste qu’un fallback de test/dev.

## Décisions

- `diffAgainst` compare la base au **working tree** (pas seulement HEAD), pour que le diff
  se rafraîchisse après Apply.
- L'agent local est explicitement un **fallback** ; le contrat `ReviewAgent` ne change pas
  quand Koog arrive.
- Un chemin de suggestion reste validé par snippet avant écriture.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : **71 tests, 0 échec** (JGit, anchoring, prompt,
  parser, local review, diff, graph, settings, VM).
- `:shared:compileKotlinJvm` et `:desktopApp:compileKotlin` verts.
- Check complet non lancé : pas de SDK Android dans cet environnement.

## Incertain / blocage

- #20 est mergé (#51) : le prompt, le parseur, l’ancrage et `KoogLlmClient` sont dans main.
  La PR #50 ne fait que les brancher à l’UI ; il reste à faire un appel réel avec une clé.
- Pas de preuve Air pour ce ticket ; à faire par H2/H1 avant la démo.
- L'UI Apply/Commit n'a pas été rejouée à la main sur un écran (pas de GUI ici).

## Suivant

1. Faire un appel réel avec une clé (Anthropic/OpenAI) et capturer la boucle.
2. Rejouer la boucle sur le repo de démo et capturer (Air + desktop).
3. Ajouter le mode « Review en cours » / erreurs du handoff (#33).
