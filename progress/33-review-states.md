# Ticket 33-review-states

- Statut : prêt pour PR (desktop ; Android suivra avec #23)
- Propriétaire humain : @dovatgabriel (H1) pour la revue ; implémentation H2
- Piste : B + C (UI partagée)
- Agent : lead
- Fichiers : `shared/src/commonMain/.../ui/repository/{ReviewPanel,RepositoryViewModel,RepositoryScreen}.kt`,
  `composeResources/values{,-fr}/strings.xml`, `commonTest/.../RepositoryViewModelTest.kt`

## Journal

- 18:45 — états typés : `ReviewFailure.MissingKey`, `Network`, `Timeout`, `Other` ; mapping
  depuis `MissingApiKeyException`, `ReviewProviderException` (timeout par message/cause).
- 18:50 — ReviewPanel : progression (4 étapes), clé manquante → Open Settings, réseau/timeout
  → Retry, branche propre → CLEAN, résultat inchangé.
- 18:55 — tests ViewModel : clé manquante, provider/network, timeout distinct.

## Décisions

- L’erreur fournisseur reste reformulée par `KoogLlmClient` ; l’UI ne montre jamais la clé.
- Le bouton « Push N commits » de l’état propre est omis tant que #36 (fetch/pull/push) n’est
  pas là ; l’état indique simplement la branche propre.

## Vérifications

- `:shared:jvmTest --rerun-tasks` : tests ajoutés, 0 échec.
- `:shared:compileKotlinJvm` + `:desktopApp:compileKotlin` verts.
- Android : états partagés prêts, écran Android à brancher avec #23.

## Suivant

- Rejouer les 6 états à l’écran desktop/Android et comparer au handoff.
