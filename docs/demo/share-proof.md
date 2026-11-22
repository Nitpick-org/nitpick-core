# Preuve : partage d'une review desktop → Android (#170)

22/11 02:05, `main` à `3b14570`, serveur Ktor sur `localhost:8080`, émulateur `nitpick-qa` (API 36) avec
`adb reverse tcp:8080 tcp:8080`. Le dépôt relu est le dépôt de test `remote.git` servi en
`git://10.0.2.2/remote.git` ; desktop et téléphone l'ont cloné depuis la même URL.

| Étape | Résultat | Capture |
| --- | --- | --- |
| Desktop : Nitpick branch sur `feature/search-pages-import` vs `main` | 2 nits (`lastPage` division par zéro, `toInt()` sans numéro de ligne) | `share-desktop-shared.png` |
| Desktop : **Share review** | Badge « Shared » ; `GET /reviews?repoUrl=git://10.0.2.2/remote.git` renvoie la review `05bc388` avec 2 nits | idem |
| Android : Shared reviews → Refresh | Ligne « feature/search-pages-import · 05bc388 · 2 nits » | |
| Android : ouvrir la review | Écran Review avec le badge **Shared review**, 2 fichiers, aucun appel au modèle (voir ci-dessous) | `share-android-opened.png` |
| Android : nit `src/Search.kt:17` → Apply suggestion | « Applied », « 1 suggestion applied », bouton Stage & commit ; `require(size > 0)` présent dans le fichier sur l'appareil | `share-android-applied.png` |

Temps total du parcours : moins d'une minute une fois le serveur lancé. La liste du téléphone montrait
aussi une entrée `ae8778a` : c'est un JSON envoyé à la main avec `curl` pendant la QA du 21/11, pas un
partage desktop.

« Aucun appel au modèle » vient du code, pas de la capture : `RepositoryViewModel.openSharedReview`
ré-ancre les commentaires reçus (`reanchor`) et n'appelle jamais l'agent de review.
