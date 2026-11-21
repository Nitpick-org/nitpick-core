# Ticket 64 — Sign in with GitHub / GitLab on Android (OAuth device flow)

- Statut : en cours (code prêt, client ids à renseigner)
- Propriétaire humain : @pepito2t (H3)
- Piste : C android + finitions
- Agent : Claude Code (Fable 5.1), session humaine
- Model : fable
- Escalations : aucune
- Fichiers autorisés : `shared/src/commonMain/kotlin/dev/nitpick/app/settings/ForgeClient.kt`,
  `shared/src/commonTest/.../settings/ForgeClientTest.kt`, `AppContainer.kt`, `ui/App.kt`,
  `ui/phone/PhoneFlow.kt`, `ui/phone/ConnectScreen.kt`, `composeResources/values*/strings.xml`

## Fait

- `ForgeClient` (RFC 8628) : `start(host, clientId)` demande le code, `awaitToken` interroge
  le endpoint token à l'intervalle donné, +5 s sur `slow_down` (ou l'`interval` renvoyé), abandon
  à `expires_in`. Le corps est lu quel que soit le statut HTTP : GitHub répond 200 avec `error`,
  GitLab 400. Le corps n'entre jamais dans une exception (il porte le token).
- Endpoints vérifiés dans la doc officielle (GitHub « Authorizing OAuth apps », GitLab
  « OAuth 2.0 identity provider API ») : `/login/device/code` + `/login/oauth/access_token`
  (scope `repo`), `/oauth/authorize_device` + `/oauth/token` (scopes
  `read_repository write_repository`). `Accept: application/json` sinon GitHub répond en
  form-encoded. Pas de secret client.
- Écran Connect : bouton « Se connecter avec GitHub/GitLab » au-dessus du jeton ; il ouvre le
  navigateur (`LocalUriHandler`, `verification_uri_complete` de GitLab quand il existe), affiche le
  code utilisateur en grand, « Ouvrir le navigateur » et « Annuler » (annule le polling). Le token
  obtenu passe par le même `onConnect(GitConnection)` que le jeton saisi.
- Le jeton saisi à la main reste disponible tel quel.
- `ForgeOAuthClientIds` : ids publics en constantes ; vide = bouton masqué. GitHub renseigné
  (OAuth App « Nitpick », device flow activé, tokens sans expiration).
- Compte affiché : `GitConnection.login` (GitHub `login`, GitLab `username`, via
  `ForgeClient.fetchLogin`) rempli après la connexion ou un jeton saisi ; `@login` remplace
  « Account » dans la barre Repositories, et l'écran Connect dit « Connecté à GitHub en tant
  que @login ». Un échec de la lecture du nom ne bloque pas la connexion.

## Décisions

- Pas de package nouveau : le client vit dans `settings/` à côté de `GitConnection`.
- Les ids clients sont des constantes de code, pas un BuildConfig : un id OAuth est public et
  Gradle KMP n'a pas de BuildConfig sans plugin.
- Codes et token en `remember`, jamais en `rememberSaveable` (même règle que le jeton saisi).
- Le polling compte le temps attendu au lieu d'un `withTimeout` : en temps virtuel de `runTest`
  le timeout sautait pendant l'I/O du MockEngine et rendait les tests non déterministes.

## Non fait, à dire dans la PR

- GitLab auto-hébergé : `GitConnection` n'a pas d'URL de forge (changement de contrat, accord H2).
- Rafraîchissement du token GitLab : `expires_in` 7200 s, pas de `refresh_token` dans le flux
  device selon la doc ; l'utilisateur se reconnecte après 2 h.
- Copier le code dans le presse-papiers (API Compose en transition, aucun usage dans le repo).
- Preuve sur émulateur : impossible sans OAuth App ; recette donnée à H3.

## Preuves

- `check.sh` vert ; `ForgeClientTest` : 6 tests, 0 échec (jvm + androidHostTest).

## Journal

- 23:20 — worktree créé, docs GitHub/GitLab vérifiées.
- 23:35 — client + tests + écran ; check vert.
- 23:45 — client id GitHub, polling tolérant aux coupures, compte connecté affiché.
