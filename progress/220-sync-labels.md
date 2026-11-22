# Ticket 220-sync-labels

- Statut : en cours
- Propriétaire humain : @
- Piste : <A core | B git+desktop | C android+finitions>
- Agent : @implementer
- Model : <haiku|sonnet|opus>
- Escalations :
- Fichiers autorisés : shared/src/<sourceSet>/<...>

## Journal

- 03:28 — worktree créé.


- 04:20 — demande H3 : Fetch / Pull / Push changeaient de libellé en cours d'action et décalaient la barre. Libellés fixes sur desktop et téléphone ; « Share review » aussi (`SyncIcon` vers le haut pendant l'envoi). Les chaînes `*_in_progress` restent pour la boîte Clone et le bouton bas du téléphone, qui ne décalent rien.
- 04:25 — le rebond au clic s'applique aussi aux icônes passées par `leading` (`Box(pop.modifier)`), donc aux icônes de sync. Mouvement de sync plus visible : la flèche parcourt 6 dp en fondu et repart, au lieu d'un va-et-vient de 3 dp.
- 04:50 — rejoué sur desktop avec le dépôt de test servi par `git daemon` sur 127.0.0.1 : Fetch (icône qui tourne, libellé fixe), Push (1 commit arrivé sur le dépôt nu, « Push 1 » → « Push »), Fetch puis Pull (« Pull 1 », le clone avance d'un commit, 7 commits affichés), Settings (ouverture, Cancel). Le premier essai avec `origin` en `10.0.2.2` (alias émulateur) expirait : erreur d'environnement, pas de l'app.
- Deux sorties propres de l'app (code 0, aucune trace) pendant ces essais, non reproduites une fois la fenêtre activée avant chaque clic ; `main` ne les montre pas non plus. Noté, pas attribué.
