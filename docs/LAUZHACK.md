# LauzHack 2026 — faits utiles, règles et logistique

> Vérifié le 16 novembre 2026 sur les sources officielles. Les points incertains sont
> marqués **« à vérifier »**. Ne pas confondre avec les éditions précédentes.

## En bref

| | |
| --- | --- |
| Événement | LauzHack 2026, 10ᵉ édition |
| Dates | Samedi 21 et dimanche 22 novembre 2026 |
| Lieu | Bâtiment **BC**, EPFL, Lausanne |
| Durée de hack | **24 h** : samedi 12:00 → dimanche 12:00 |
| Équipe | Jusqu'à **4 personnes** (3, c'est très bien) |
| Thème | Libre, ou un des challenges sponsors |
| Prix | Top 3 global + favori des organisateurs + au moins un gagnant par challenge sponsor |
| Démo | **3 minutes**, puis **1 minute de Q&A**, **sans slides** |
| Soumission | [app.lauzhack.com](https://app.lauzhack.com), avant **12:00 dimanche** |

## Programme officiel

### Samedi 21 novembre

| Heure | Événement | Ce que fait l'équipe |
| --- | --- | --- |
| 8:00–9:30 | Check-in + petit-déjeuner | Arriver **avant 9:30**, ID valide, choisir une bonne table, tester Wi-Fi et prises |
| 10:00–10:15 | Cérémonie d'ouverture | Écouter les annonces, noter les challenges |
| 10:20–11:40 | Tech talks sponsors | **Une personne prend des notes** ; les autres préparent le repo et les questions |
| 11:40–12:00 | Team building | Finaliser l'idée, la cible, la phrase de nouveauté |
| **12:00** | **Hacking starts** | H0 : PRD, DEMO, squelette |
| 12:00–13:00 | Lunch | Manger tôt, pas de réunion longue |
| 18:30–19:30 | Dîner | Sync de 10 min, puis on repart |

### Dimanche 22 novembre

| Heure | Événement | Ce que fait l'équipe |
| --- | --- | --- |
| 0:00–1:00 | Midnight snack | Gel de la v0.1, vidéo de secours |
| 7:00–9:00 | Petit-déjeuner | Intégration finale, QA |
| **12:00** | **Hacking ends** | Plus de code |
| 11:30–12:30 | Lunch | Soumission déjà faite |
| 12:30–15:00 | Judging | Démo 3 min + Q&A 1 min |
| 15:00–17:00 | Cérémonie de clôture | Rester jusqu'au bout (obligatoire pour le remboursement) |

> Le créneau lunch du dimanche chevauche la fin du hack : c'est normal. La soumission
> doit être bouclée **avant 12:00**, pas à 12:15.

## Ce que le jury note

Critères officiels du classement global ([details](https://lauzhack.com/details)) :

1. **Innovant, original, inattendu** — la nouveauté doit être explicite.
2. **Démo convaincante, fluide, facile à suivre** — 3 minutes, pas de slides.
3. **Impact potentiel** — quelle différence pour le monde / pour une personne précise.

Les challenges sponsors sont jugés **selon les critères de chaque sponsor**. Une équipe
peut gagner le classement global **et** un challenge (règle 34), et plusieurs prix par
sponsor sont possibles (règle 33).

### La règle de nouveauté

- Les bibliothèques et APIs existantes sont autorisées.
- Le code que vous avez déjà écrit est autorisé.
- Mais le travail doit être **novel** par rapport à ces ressources, et vous devez
  **dire explicitement ce que vous avez créé pendant l'événement et ce que vous avez réutilisé**.

Conséquence : préparez une phrase du type :

> « Avant, il fallait X pour faire Y. Nous faisons Y en Z, et la partie nouvelle est W.
> Le reste, ce sont des bibliothèques existantes. »

## Soumission

La soumission se fait sur **[app.lauzhack.com](https://app.lauzhack.com)**, pas sur Devpost.
D'après le code de l'application ([EventManager](https://github.com/LauzHack/EventManager)) :

| Champ | Contrainte | Conseil |
| --- | --- | --- |
| Titre | ≤ 40 caractères | Le nom + un sous-titre court |
| Description courte | ≤ 100 caractères | La phrase que le jury doit retenir |
| Description longue | ≤ 1000 caractères | Problème, solution, nouveauté, ce qui est réel vs simulé |
| Lien | URL publique | Un repo GitHub public avec un README qui explique comment lancer |
| Miniature | Image ≤ 300 Ko | Une capture de l'écran principal, pas un logo |
| Challenges | Cases à cocher | 1 à 2 maximum, ceux qui correspondent vraiment |

- Un seul membre crée le projet et **invite les deux autres** : sinon pas de crédit.
- Le projet est éditable jusqu'au passage en phase « JudgingStarted » (~12:30).
  **Soumettez avant 12:00** pour avoir une marge.
- Pas de champ vidéo dans le formulaire : la vidéo de secours est pour vous, pas pour
  la soumission. (À vérifier le jour J.)

## Règles et éligibilité

- Au moins 18 ans, étudiant/apprenti/PhD, ou diplômé depuis moins de 24 mois.
- Équipe de **4 personnes maximum**.
- IP : LauzHack ne revendique aucun droit sur votre travail.
- Code de conduite : respect, pas de harcèlement, pas d'alcool/drogue. Signalement via
  [lauzhack.com/sos](https://lauzhack.com/sos) ou un organisateur.
- **Aucune clause IA/LLM** dans les règles 2026. L'obligation explicite reste :
  dire ce que vous avez créé pendant l'événement et ce que vous avez réutilisé.
- Les règles peuvent être mises à jour jusqu'à 7 jours avant l'événement.

## Logistique

- **Lieu** : bâtiment BC, EPFL. Aéroport Genève → train Lausanne/Renens → métro M1.
- **Wi-Fi** : `eduroam` avec les identifiants universitaires ; réseau libre EPFL en secours.
- **Prises** : adaptateur **type J** (3 broches suisse) si nécessaire.
- **À apporter** : ID valide, laptop + chargeur, adaptateur, affaires de toilette
  (douches), sac de couchage optionnel, gourde.
- **Sommeil** : zone de sommeil avec navettes régulières à partir du samedi soir ;
  couverture de voyage dans le welcome kit ; **pas** de dortoir dimanche-lundi.
- **Nourriture** : petit-déj, lunch, dîner, snack de minuit ; restrictions alimentaires
  honorées « si possible ».
- **Remboursement** : reçus uploadés **avant samedi soir**, paiement en **cash CHF** à la
  fin ; il faut rester jusqu'à la clôture et démontrer le hack. Plafonds : 0 CHF
  (Lausanne), 50 CHF (Suisse), 100 CHF (Europe), 150 CHF (reste du monde).
- **Exclusivité** : pas d'accompagnants non participants.

## Sponsors 2026

| Sponsor | Niveau | Challenge annoncé |
| --- | --- | --- |
| Logitech | Gold | Non publié |
| Loterie Romande | Gold | Non publié |
| IMC | Gold | Non publié |
| JetBrains | Silver | Non publié |
| Nexthink | Silver | Non publié |
| Scholé AI | Silver | Non publié |
| AXA | Silver | Non publié |
| UBS | Silver | Non publié |
| SBB | Silver | Non publié |

**Aucun challenge n'est publié à ce jour.** Les énoncés arrivent probablement aux tech
talks (10:20–11:40) ou sur Discord. Huawei n'est plus sponsor en 2026.

## Ce qui a gagné (2023–2025)

| Édition | 1ᵉʳ | 2ᵉ | 3ᵉ | Favori des organisateurs |
| --- | --- | --- | --- | --- |
| 2025 | *logilinux* — support Linux HID++ pour souris Logitech | *KnowTube* — YouTube → cours interactif | *showcAIse* — coach de présentation IA | *Birdy* — tracker de posture |
| 2024 | *Converge* — agents IA de réunion | *Chronica* | *Podcastify AI* | *MakeGreatBox* |
| 2023 | *SightSync* — assistant pour malvoyants | *VirtuWheel* | *It's not about winning* | — |

Motifs observés :

- Une intégration matérielle **profonde et crédible** peut gagner (logilinux).
- Un produit IA avec un bénéfice **social évident** et une démo **très lisible** gagne aussi.
- L'accessibilité revient trois années sur trois.
- Plusieurs équipes cumulent classement global + challenge sponsor.
- Aucun feedback de jury public n'existe : ne sur-optimisez pas sur des rumeurs.

## À vérifier sur place

- Énoncés, prix et contraintes des 9 challenges.
- Salles exactes dans BC, plan, zone de sommeil.
- Nombre de tours de jugement et composition du jury.
- Fin réelle de la clôture et apéro.
- Champs exacts du formulaire de soumission (le code peut évoluer).

## Checklist express

- [ ] Arriver avant 9:30 avec une pièce d'identité.
- [ ] Noter les challenges pendant les tech talks.
- [ ] Créer le projet sur app.lauzhack.com à 12:00 et inviter les coéquipiers.
- [ ] Rendre le repo public + README avant la soumission.
- [ ] Uploader les reçus de voyage avant samedi soir.
- [ ] Répéter la démo de 3 minutes, sans slides, 5 fois.
- [ ] Enregistrer une vidéo de secours.
- [ ] Soumettre avant 12:00 dimanche.
- [ ] Rester jusqu'à la clôture pour le remboursement.

## Sources

- [LauzHack 2026](https://lauzhack.com/) — programme, FAQ, sponsors.
- [Détails participants](https://lauzhack.com/details) — lieu, check-in, critères, démo.
- [Politiques](https://lauzhack.com/policies) — acceptation, sommeil, remboursement.
- [Règles PDF](https://lauzhack.com/pdf/rules.pdf) — éligibilité, jugement, IP, nouveauté.
- [app.lauzhack.com](https://app.lauzhack.com) — soumission.
- [EventManager](https://github.com/LauzHack/EventManager) — champs de soumission.
- [Projets 2025](https://2025.lauzhack.com/projects) — gagnants et challenges.
- [EPFL News 2019](https://news.epfl.ch/news/augmented-reading-experience-takes-top-prize-at--2/) — anciens critères (technique, nouveauté, fonctionnalité) ; ne plus s'y fier seuls.
