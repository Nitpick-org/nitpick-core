# Ticket #80 — Surface review tool activity in the Review tab

- Statut : fait, `check.sh` vert, tests exécutés (jvmTest 151, 0 échec, dont les 2 nouveaux)
- Piste : review (C) + UI
- Agent : lead (Fable), 21/11 soir
- Fichiers : `review/{ReviewAgent,LlmReviewAgent,LocalReviewAgent}.kt`,
  `ui/repository/{RepositoryViewModel,ReviewPanel}.kt`, `ui/phone/PhoneReviewScreen.kt`,
  strings EN/FR, `RepositoryViewModelTest.kt`, `ShareReviewViewModelTest.kt`

## Fait

- `ReviewAgent.review` prend un `onToolUse: (ReviewToolUse) -> Unit = {}` (noms et chemins
  seulement, jamais de contenu ni de clé). `LlmReviewAgent` le passe à `ReviewToolbox` ;
  le paramètre de constructeur disparaît.
- `RepositoryUiState.reviewToolUses` : rempli à la fin de la review, vidé au lancement
  d'une nouvelle review, à l'ouverture d'un dépôt, au checkout, au pull qui avance la
  branche, et à l'ouverture d'une review partagée (ses outils ont tourné ailleurs).
- UI : `ToolActivityLine` dans la carte résumé — desktop (ReviewPanel) et téléphone
  (PhoneReviewScreen). « Read N files · M tool calls », chemins lus dans le tooltip
  (long-press sur téléphone). Rien ne s'affiche sans appel d'outil. Strings EN + FR.

## Décisions / à valider

- **Contrat §6 touché (accord H2 à confirmer)** : paramètre optionnel ajouté à
  `ReviewAgent.review`. Rétrocompatible (valeur par défaut), mais c'est un contrat.
- Pas de pluriels composeResources : le style maison (`review_summary`) ne les utilise pas.
- La trace console promise par #43 n'existe pas sur main (seul le callback existe) :
  rien à préserver, rien de supprimé.

## Incertain

- Le tooltip sur téléphone demande un long-press ; si la démo veut les chemins visibles
  d'office sur Android, c'est une ligne à ajouter (liste en `codeSmall` sous le compteur).

## Suivant

- Rejouer une vraie review (clé API) et vérifier la ligne sur desktop et sur ART.
