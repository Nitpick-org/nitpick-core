# Script de démo — 3 minutes, sans slides

> À écrire **avant** le code. Si ce script ne tient pas debout, l'idée n'est pas prête.
> Le règlement interdit les slides ; images et vidéos de secours sont autorisées.

## La phrase que le jury doit retenir

> « Pour <personne> qui <douleur>, <produit> fait <bénéfice> en <temps>.
> La nouveauté, c'est <W>. »

## Script 3 minutes

> Le créneau officiel est 3 minutes. Visez **2:45** : la marge absorbe un imprévu.
> Enregistrez aussi une **vidéo de secours de 2 minutes**.

| Temps | Contenu | Qui | Ce qu'on voit |
| --- | --- | --- | --- |
| 0:00–0:20 | Le problème, une personne précise | H1 | Le produit à l'écran, pas de deck |
| 0:20–0:35 | L'avant, ce que ça coûte | H1 | Une capture ou une phrase, pas un deck |
| 0:35–2:05 | **Démo live**, un seul flux | H3 | L'écran réel, pas de montage |
| 2:05–2:25 | Nouveauté + ce qui est réutilisé | H2 | Une phrase, pas un diagramme |
| 2:25–2:40 | Impact, qui d'autre, échelle | H1 | Un chiffre crédible ou une citation |
| 2:40–2:45 | La phrase à retenir | H1 | Écran final |

### Règles de la démo live

- **Une seule histoire.** Pas de tour de toutes les features.
- **Le aha en premier.** La technique après.
- **Zéro temps mort.** Précharger, seeder, cacher.
- **Données réalistes.** Pas de « test 123 ».
- **Honnêteté.** Dire ce qui est mocké.
- **Pas de slides.** Une image de secours, oui ; un deck, non.

## Version 30 secondes

> « Pour <personne> qui <douleur>, nous avons construit <produit> qui <bénéfice>.
> La nouveauté, c'est <W>. On l'a démontré en <temps> avec <preuve>. »

## Partagé vs spécifique — le chiffre à montrer

```bash
./scripts/count-lines.sh
```

Relevé du 22/11, 02:10 sur `main` : **8 198 / 8 282 lignes de production = 99,0 % partagées**
(`commonMain` + `jvmSharedMain`) ; 84 lignes plateforme ; 2 947 lignes de tests exclues.
Relancer `./scripts/count-lines.sh` juste avant la démo et coller le chiffre à jour.

## Ce qu'on a créé vs réutilisé

- **Créé pendant l'événement** : …
- **Réutilisé** : …
- **Mocké** : …

> Le règlement demande de le dire clairement. Le jury apprécie l'honnêteté ; il pardonne
> un mock, pas un mensonge.

## Q&A — 1 minute

Préparer 10 réponses de 2 phrases :

1. Qu'avez-vous vraiment construit pendant l'événement ?
2. Qu'avez-vous réutilisé ?
3. Pourquoi personne ne l'a déjà fait ?
4. Comment ça marche, en une phrase ?
5. Combien ça coûte par utilisateur ?
6. Qu'est-ce qui casse à l'échelle ?
7. Quel modèle économique ?
8. Quelle prochaine étape avec 1 semaine ?
9. Pourquoi vous ?
10. Quelle limite éthique/légale ?

## Plan B

| Panne | Réaction | Préparé ? |
| --- | --- | --- |
| API externe down | ouvrir une review déjà partagée sur le serveur, sinon la vidéo de secours | [ ] |
| Réseau lent | utiliser la vidéo de secours | [ ] |
| Base vide | `reset demo` / seed | [ ] |
| Écran qui ne s'affiche pas | capture PNG plein écran | [ ] |
| Démo qui crash | vidéo de 2 min, on continue à parler | [ ] |

- **Vidéo de secours** : à enregistrer avant 11:00 (aucun lien pour l'instant).
- **Captures** : `docs/demo/` (dogfooding sur nos branches d'agents)
- **Parcours Android rejoué** (22/11 00:45, émulateur API 36, compte GitHub connecté par le
  device flow, repo `pepito2t/OpenTab`, branche `perf/thumbnail-lookup` avec un lock non
  relâché et un off-by-one d'éviction) : `docs/demo/android-01-repos-signed-in.png` →
  `android-02-clone-form` → `android-03-branch-checkout` → `android-04-review-two-bugs` (les
  2 bugs trouvés) → `android-05-nit-suggestion` (bottom sheet, suggestion) →
  `android-06-suggestions-applied` → `android-07-push-after-commit` → `android-08-pushed-graph`
  (↑0 ↓0). Commit `cb55041` « Apply review suggestions » visible sur GitHub. Second passage
  (01:00) : `android-09-commit-message` (sujet proposé depuis les nits appliqués) et
  `android-10-pushed-banner` (confirmation animée après le push), commit `f91a419`.
- **Données de démo** : le parcours capturé utilise `pepito2t/OpenTab`
  (branche `perf/thumbnail-lookup`, deux bugs plantés). `pocket-ledger` +
  `docs/demo/junie-brief.md` restent le plan B si ce repo est recréé ; il n'est pas dans
  ce dépôt. Préparer les clones locaux sur le laptop et l'émulateur avant la scène.
- **URL de secours** : aucune ; la démo est locale (desktop + Android + serveur Ktor sur le laptop).

## Répétition

| Passage | Heure | Durée | Qui | Problème | Corrigé |
| --- | --- | --- | --- | --- | --- |
| 1 | | | | | [ ] |
| 2 | | | | | [ ] |
| 3 | | | | | [ ] |
| 4 | | | | | [ ] |
| 5 | | | | | [ ] |

Objectif : **5 passages**, dont 1 avec une panne simulée. Chronométrer. Si > 2:45,
couper.

## Checklist avant de monter sur scène

- [ ] Laptop chargé, câble, adaptateur.
- [ ] Serveur Ktor lancé, `adb reverse tcp:8080 tcp:8080` en place, une review partagée de secours.
- [ ] Review partagée de secours prête (plan B LLM).
- [ ] Vidéo de secours accessible hors ligne.
- [ ] Téléphone en silencieux.
- [x] Android rejoué à la main : Repos → Graph → Nitpick branch → nit en bottom sheet → Apply → Stage & commit → le bouton **Push** apparaît après le commit → push visible sur GitHub (22/11 00:45, captures `docs/demo/android-*.png`).
- [ ] Les 3 savent qui parle quand.
- [ ] La phrase de nouveauté est écrite sur un post-it.
