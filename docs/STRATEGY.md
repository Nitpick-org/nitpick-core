# Stratégie — comment gagner un hackathon de 24 h

> Ce document ne choisit pas l'idée : les idées viennent des humains. Il donne un cadre
> pour choisir vite, couper juste, et transformer une idée en démo qui gagne.

## La formule

À LauzHack, le jury note trois choses ([détails](https://lauzhack.com/details)) :

1. **Nouveauté** — innovant, original, inattendu.
2. **Démo** — convaincante, fluide, facile à suivre, en 3 minutes sans slides.
3. **Impact** — la différence que ça peut faire.

La profondeur technique n'est pas un critère explicite, mais c'est un **multiplicateur de
crédibilité** : elle rend la nouveauté believable et l'impact concret. Le gagnant 2025
(*logilinux*, support Linux d'un protocole Logitech) est un exemple de profondeur technique
au service d'un problème réel et d'une démo lisible.

**Formule de travail :**

> Un problème réel + une seule chose que personne n'attend + une démo qu'on peut voir en
> 20 secondes + une preuve technique crédible + une phrase de nouveauté.

## Choisir l'idée (H0, 45 minutes max)

### Processus

1. Chaque humain arrive avec **3 idées**, pas plus. Pour chacune :
   - une phrase : « Pour <personne>, <problème> ; nous faisons <solution>. »
   - le **moment aha** : ce que le jury voit à l'écran en 20 secondes.
   - le **risque technique n°1** : ce qui peut prendre 6 heures et échouer.
2. 2 minutes par idée, sans débat. On note à chaud.
3. Scoring aveugle avec la grille ci-dessous (chacun note, on moyenne).
4. Garder les **2 meilleures**, les griller 20 minutes chacune avec l'agent
   `avocat-du-diable` et le sous-agent `chercheur`.
5. Choisir. Écrire `docs/DEMO.md` **avant** le PRD. Si la démo ne s'écrit pas en 5 points,
   l'idée n'est pas prête.

### Grille de score (1 à 5)

| Critère | Poids | 1 (faible) | 3 (correct) | 5 (fort) |
| --- | --- | --- | --- | --- |
| **Démo-abilité** | 30 % | il faut expliquer avant de montrer | on voit le résultat en 1 min | le aha est visible en 20 s, sans slides |
| **Nouveauté** | 25 % | c'est un CRUD avec un LLM | combinaison originale | « je n'ai jamais vu ça » + explicable en 1 phrase |
| **Impact** | 20 % | personne ne paierait | utile pour un groupe | douleur réelle, personne précise, bénéfice évident |
| **Faisabilité 24 h** | 15 % | 3 intégrations inconnues | 1 intégration risquée | tout est faisable avec les APIs/le matériel disponibles |
| **Fit challenge sponsor** | 10 % | aucun | correspond à un challenge | le challenge est presque écrit pour l'idée |

**Seuils d'élimination :**

- Démo-abilité ≤ 2 → on coupe, même si l'idée est belle.
- Faisabilité ≤ 2 → on coupe, sauf si le risque est isolable et mockable.
- Nouveauté ≤ 2 → on coupe : le jury a vu 10 projets comme ça.
- Impact ≤ 2 → à discuter : un hack technique impressionnant peut compenser.

### Questions à poser avant de dire oui

- Est-ce qu'on peut **montrer** le résultat sans login, sans setup, sans attendre ?
- Quelle est la **seule** chose que le jury doit retenir ? Peut-on la dire en une phrase ?
- Quelle partie peut être **mockée** sans mentir sur la démo ?
- A-t-on accès à l'API, au matériel, aux données, au compte sponsor **aujourd'hui** ?
- Combien de temps pour un **squelette qui marche** ? Si > 4 h, l'idée est trop risquée.
- Est-ce qu'un concurrent direct existe déjà ? Si oui, quelle est **notre** différence ?

## Ce qui tue une idée en 24 h

| Piège | Pourquoi c'est mortel | Alternative |
| --- | --- | --- |
| Auth / comptes / onboarding | 2–4 h perdues, démo fragile | mode invité, session locale, données seedées |
| Paiement réel | KYC, clés, 3-D Secure | mock clair, capture d'écran |
| Multi-utilisateur / temps réel complexe | courses, permissions, bugs | un seul utilisateur, état local |
| 3 intégrations inconnues | une seule peut prendre la journée | 1 intégration réelle + mocks |
| App mobile native / store | build, signature, review | web mobile, PWA |
| Fine-tuning / entraînement | données, GPU, temps | API pré-entraînée + prompt |
| Matériel non disponible | impossible à tester | simulateur + vidéo |
| « On ajoutera l'IA après » | l'IA est souvent le cœur du aha | intégrer le cœur tôt |
| Trop de features | aucune n'est finie | une tranche verticale parfaite |

## La démo de 3 minutes

### Structure

| Temps | Contenu | Qui |
| --- | --- | --- |
| 0:00–0:25 | Une personne précise, sa douleur, en une phrase | H1 |
| 0:25–0:40 | « Aujourd'hui, elle fait X ; ça coûte Y » | H1 |
| 0:40–2:15 | **Démo live**, un seul flux continu, on narre ce qu'on fait | H3 |
| 2:15–2:35 | Ce qui est nouveau, et ce qui est réutilisé (honnêteté) | H2 |
| 2:35–2:50 | Impact, qui d'autre, à quelle échelle | H1 |
| 2:50–3:00 | La phrase que le jury doit retenir | H1 |

### Règles

- **Pas de slides.** C'est interdit par le règlement. Préparez des images et une vidéo
  de secours si la démo casse.
- **Une seule histoire.** Pas de tour de toutes les features. Le jury doit pouvoir raconter
  votre projet à quelqu'un d'autre après.
- **Montrer, pas raconter.** « Regardez » vaut mieux que « notre architecture… ».
- **Zéro temps mort.** Si un chargement prend 5 secondes, préchargez ou meublez.
- **Données réalistes.** Pas de « test 123 », pas de Lorem ipsum.
- **Le aha avant la technique.** La technique arrive après, en une phrase.
- **Honnêteté.** Dites ce qui est mocké. Le jury le verra de toute façon.

### Version 30 secondes (ascenseur, Q&A, réseautage)

> « Pour <personne> qui <douleur>, nous avons construit <produit> qui <bénéfice>.
> La nouveauté, c'est <W>. On l'a démontré en <temps> avec <preuve>. »

### Q&A (1 minute)

Préparez 10 réponses de 2 phrases :

1. Qu'est-ce que vous avez vraiment construit pendant l'événement ?
2. Qu'est-ce que vous avez réutilisé ?
3. Pourquoi personne ne l'a déjà fait ?
4. Comment ça marche techniquement, en une phrase ?
5. Combien ça coûte par utilisateur ?
6. Qu'est-ce qui casse à l'échelle ?
7. Quel est le modèle économique ?
8. Quelle est la prochaine étape si vous aviez 1 semaine ?
9. Pourquoi vous, et pas une grande entreprise ?
10. Quelle est la limite éthique / légale ?

## Challenges sponsors

- Les énoncés arrivent aux tech talks (10:20–11:40) ou sur Discord. **Une personne note.**
- Ciblez **1 challenge principal**, éventuellement 1 secondaire si le même cœur les sert.
- Chaque challenge = un jury supplémentaire = une chance de prix en plus. Mais un produit
  tordu pour rentrer dans un challenge se voit.
- Les sponsors jugent **leurs critères**, pas forcément les critères globaux : lisez l'énoncé,
  identifiez la métrique qu'ils veulent voir bouger, et montrez-la.
- Vous pouvez gagner le global **et** un challenge (règle 34).
- Gardez une slide mentale (pas une slide !) « ce que ça change pour <sponsor> » pour le Q&A.

## Le plan des 24 h

| Bloc | Heure | Objectif | Porte |
| --- | --- | --- | --- |
| Kickoff | 12:00–13:00 | Idée choisie, DEMO.md, PRD, squelette | Démo écrite en 5 points |
| Squelette | 13:00–16:00 | Parcours de démo bout en bout, même moche, déployé | Preview qui marche |
| Cœur | 16:00–19:00 | La fonction qui porte le aha | Le aha est visible |
| Deuxième feature | 19:00–23:00 | Profondeur ou deuxième cas d'usage | Démo complète |
| Gel v0.1 | 23:00–00:00 | Tag, vidéo de secours, soumission en brouillon | v0.1 enregistrée |
| Nuit | 00:00–07:00 | Sommeil en shifts, agents bornés sur du low-risk | 2 humains éveillés max |
| Intégration | 07:00–09:30 | Corriger, polir, répéter | e2e vert |
| Gel final | 09:30–10:30 | Plus de feature, seulement des bugs | Code gelé |
| Soumission | 10:30–11:30 | Formulaire, README, vidéo | Soumis avant 12:00 |
| Buffer | 11:30–12:00 | Répétition, Q&A, respiration | Tout le monde prêt |

> Le vrai risque n'est pas « pas assez de features », c'est **une démo qui casse** ou
> **une soumission incomplète**. Le gel est une feature.

## Rubrique de décision interne (à utiliser à H6, H12, H18)

Notez de 1 à 5 :

| Question | 1 | 5 |
| --- | --- | --- |
| La démo raconte-t-elle une seule histoire ? | on explique | on voit |
| Le aha est-il visible en < 30 s ? | non | oui |
| La nouveauté est-elle explicite ? | « c'est un chatbot » | « personne n'avait fait W » |
| L'impact est-il crédible ? | gadget | douleur réelle |
| La démo survit-elle à une panne API ? | non | oui (mock/vidéo) |
| La soumission est-elle complète ? | non | oui |

Si la moyenne < 3 à H12 : **coupez** une feature et refaites la démo, ne rajoutez pas.

## Sources

- [LauzHack — détails et critères](https://lauzhack.com/details)
- [LauzHack — règles](https://lauzhack.com/pdf/rules.pdf)
- [Projets 2025](https://2025.lauzhack.com/projects), [2024](https://2024.lauzhack.com/projects), [2023](https://2023.lauzhack.com/projects)
- [AI Hero — 7 phases](https://aihero.dev/my-7-phases-of-ai-development) : idée → recherche → prototype → PRD → tickets → exécution → QA.
- [AI Hero — codebases AI agents love](https://www.aihero.dev/how-to-make-codebases-ai-agents-love) : modules profonds, interface simple, tests.
