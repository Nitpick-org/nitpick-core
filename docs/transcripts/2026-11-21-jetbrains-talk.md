# Talk JetBrains — LauzHack 2026

> Retranscription et synthèse de la prise de parole du sponsor JetBrains pendant LauzHack
> 2026. Ce document est une **source** : `docs/CONTEXT.md` reste le document maître du
> projet Nitpick. Ici, on garde ce que le talk ajoute, confirme ou contredit.

## Métadonnées

| Champ | Valeur |
| --- | --- |
| Événement | LauzHack 2026, EPFL BC, Lausanne |
| Date | Samedi 21 novembre 2026 |
| Durée | 16 min 20 |
| Source | Enregistrement audio de l'équipe, non commité (`École Polytechnique Fédérale de Lausanne.m4a`, 48 kHz stéréo, AAC) |
| Intervenant | Représentant JetBrains (nom non fiable dans l'audio, noté « Val Dreckman » par l'ASR) |
| Transcription | Whisper `large-v3-turbo` en local, puis nettoyage manuel des erreurs évidentes |
| Fiabilité | Bonne sur le sens, imparfaite sur les noms, sigles et pourcentages. Les points incertains sont marqués **[à vérifier]**. |

> Le talk est une présentation de sponsor, pas le règlement officiel. En cas de contradiction,
> le règlement LauzHack et `docs/CONTEXT.md` priment.

## TL;DR

- **Challenge** : Kotlin Multiplatform. JetBrains juge surtout **comment** le projet est
  construit, pas seulement ce qu'il fait.
- **Ce qui compte** : implémentation/POC qui marche, UX au-delà du chemin de démo,
  architecture et capacité à expliquer les choix, innovation, et **process de développement
  authentique**.
- **Bonus IA** : utiliser les outils JetBrains (JetBrains AI / Junie) donne des points bonus
  estimés à **~10–15 %** de la note. **[à vérifier]** : l'intervenant s'est corrigé plusieurs
  fois sur le chiffre exact.
- **Prix** : 1ʳᵉ place **250 € par personne**, 2ᵉ **200 €**, 3ᵉ **150 €**, en cartes cadeaux
  Tremendous.
- **Jury** : les personnes du stand JetBrains, présentes pendant l'événement. Aller leur
  parler, poser des questions techniques, demander les ressources (QR code / Discord).
- **Challenge ouvert** : on peut partir de son idée et la rattacher au challenge JetBrains ;
  pas besoin de choisir un autre challenge.

## Ce que le talk ajoute à `docs/CONTEXT.md`

| Sujet | Déjà dans `CONTEXT.md` | Ce que le talk ajoute | Action Nitpick |
| --- | --- | --- | --- |
| Challenge | KMP, desktop + Android, JetBrains | Insiste sur « how you get things done », pas seulement « what » | Soigner l'histoire d'architecture |
| Architecture | commonMain max, contrats | Le jury va **lire le repo** et juger l'architecture ; il demandera comment elle absorbe un changement de requirements | Préparer une explication de 60 s + un exemple de changement géré |
| Implémentation | P0 desktop + Android | « Implementation / proof of concept » = grosse part des points | Les 2 cibles doivent réellement tourner |
| UX | dark/light, localisation, états | « Pas juste le demo path » ; les détails font la différence | Tester empty/error/loading, petit écran, clavier |
| Innovation | Idée originale | Compte encore, surtout si on n'a pas pris un autre challenge | Garder la review locale pré-push comme différence |
| Process agentique | Agents + review humaine | Bonus « authentic development process » si on utilise l'IA et qu'on montre le vrai travail | Montrer `AGENT_LOG.md` / `REVIEW_LOG.md`, un rejet concret |
| Outils IA | Koog, agents | Utiliser **JetBrains AI / Junie** donne un bonus **[à vérifier ~10–15 %]** | Utiliser Junie sur une tâche réelle, logger le résultat |
| Présentation | 3 min, sans slides | « Presentation is still a lot » même si peu de points | Répéter la démo, pas de deck |
| Prix | Non publiés dans le repo | 250/200/150 € par personne, Tremendous | Pas d'impact produit, mais à connaître |
| Jury | Non précisé | Les gens du stand jugent ; aller les voir | Préparer 3 questions techniques pour le stand |

### Ce qui est confirmé

- Kotlin Multiplatform est bien le cœur du challenge.
- Le jury regarde l'architecture, la répartition commonMain / plateforme, et la qualité de
  l'implémentation.
- L'UX et les finitions (dark/light, localisation, etc.) comptent.
- L'usage d'outils IA/agents est regardé, et peut rapporter des points.
- La présentation compte, même si elle pèse moins que l'implémentation.

### Ce qui est nouveau ou à vérifier

- Les **pourcentages exacts** : l'intervenant donne une première approximation
  (25 / 20 / 20 / 15 / 10 / 10), puis se corrige sur le bonus IA (15 %, puis 10 %). À
  considérer comme un **ordre de grandeur**, pas une grille officielle.
- Le nom exact du programme IA : l'ASR écrit « AIR » ; il s'agit très probablement de
  **JetBrains AI** / **AI Assistant**. **[à vérifier]**.
- « Junie Lite » : agent JetBrains avec un free tier et des tokens gratuits sur un petit
  modèle, annoncé comme disponible cette année. **[à vérifier]**.
- Les cartes cadeaux « Tremendous » et les montants sont donnés à l'oral. **[à vérifier]**.
- Le QR code / les slides / le canal Discord du sponsor n'ont pas été capturés dans la
  transcription. À demander au stand.

## Actions concrètes pour Nitpick

1. **Architecture story** : être capable de dire en 60 s :
   - ce qui vit dans `commonMain` (domain, git, graph, diff, review, ui) ;
   - ce qui reste plateforme (JGit via `GitRepository`, fichiers, Android) ;
   - pourquoi ce découpage absorbe un changement de requirements.
2. **Process agentique visible** : montrer un exemple où un agent a produit quelque chose,
   un humain l'a rejeté/corrigé (`REVIEW_LOG.md`), et ce qui a changé. C'est la preuve
   « authentic development process ».
3. **Utiliser JetBrains AI / Junie** sur une tâche réelle, pas en démo artificielle. Logger
   la tâche et le modèle utilisé. **[à vérifier]** pour le bonus exact.
4. **UX au-delà du demo path** : dark/light, FR/EN, empty/error/loading, petit écran, clavier,
   états de chargement de la review. Le talk répète que les détails comptent.
5. **Présentation** : 3 min, sans slides (règle LauzHack), mais avec une narration
   architecture + process. Répéter 5 fois.
6. **Stand JetBrains** : y aller aujourd'hui, poser 3 questions :
   - quel poids exact pour l'architecture et le process IA ?
   - quels critères sponsor sont éliminatoires ?
   - où sont les slides/le QR/le canal Discord ?
7. **Ne pas sur-optimiser le challenge** : le challenge est ouvert ; la démo et l'architecture
   comptent plus que le logo.

## Citations utiles

> « We're going to focus a little bit more on how you get things done for this challenge and
> not so much on what you get done. »

> « We might skim through your repository, judge your architecture a little bit. »

> « As requirements change, sometimes you chose an architecture, or your AI chose an
> architecture — how do you handle that? »

> « Implementation and proof of concept, this is a big part for us. »

> « The UX, of course, and not just the demo path you show during the presentation. »

> « Authentic development process: if you use AI, this is bonus points… around 10–15% of the
> score. » **[à vérifier]**

> « Presentation is still a lot… even if we don't give a lot of points for it. »

## Transcription nettoyée

> Transcription **non mot à mot** : les hésitations, répétitions et erreurs ASR évidentes ont
> été retirées. Les coupures sont marquées `[…]`. Les noms et chiffres incertains sont
> signalés.

### [00:00] Introduction

**Intervenant JetBrains** : Comment ça va ? […] Je représente JetBrains aujourd'hui, et on
vous propose le **Kotlin Multiplatform Challenge**. Nous sommes une entreprise très technique.
On aime les développeurs, on aime la tech, et on va se concentrer un peu plus sur **comment
vous faites les choses** que sur **ce que vous faites**. […]

### [00:45] JetBrains, les produits, l'IA

On est une entreprise de productivité pour les développeurs. Vous nous connaissez peut-être
pour IntelliJ, PyCharm, CLion, ou pour notre langage, Kotlin. […] Aujourd'hui, la
productivité, c'est aussi beaucoup d'IA. On a nos propres outils : **JetBrains AI** (transcrit
« AIR » dans l'audio), une plateforme ouverte pour utiliser l'IA dans les IDE, et **Junie**,
notre agent de code. […]

Junie a un **free tier** pour toute personne avec un compte JetBrains : des tokens gratuits
sur un modèle plus petit, en expérimentation, disponible cette année. **[à vérifier]** Mais
la plateforme est ouverte : vous pouvez utiliser Codex, Gemini, Claude Code, ce que vous
voulez. […]

### [03:00] Kotlin Multiplatform

Kotlin est utilisé par des millions de personnes. C'est un langage multiplateforme : on le
trouve là où Java est utilisé, dans presque toutes les apps Android, en natif sur iOS, et
partiellement sur le web. […] Vous pouvez demander les détails techniques à votre IA. […]

### [04:15] Le challenge est ouvert

Le challenge, c'est un peu un marteau doré : à vous de trouver le clou. Ça peut aussi être
une autre tâche, un autre challenge d'une autre entreprise. Regardez les challenges. Si vous
avez une idée et que vous cherchez un challenge dans lequel la caser, celui-ci peut aussi
faire l'affaire. Vous n'êtes pas obligés de prendre un autre challenge. […]

On va regarder un peu plus **comment c'est construit**. On va peut-être parcourir votre
repo, juger l'architecture. On sait que c'est un hackathon, on respecte votre temps et votre
santé mentale. Mais on va faire une petite vérification technique, avec plus d'appréciation
pour **comment vous avez choisi votre architecture**, si elle a du sens pour votre projet,
etc. […]

### [05:45] Outils de développeurs et finitions

On aime construire des outils qu'on utilise nous-mêmes. On vous encourage à faire pareil :
quelque chose que vous, les gens d'EPFL, ou des gens du monde entier, pourraient
réellement utiliser. […]

On vous recommande les **finitions** : dark/light mode, c'est un prompt aujourd'hui, mais ça
fait la différence entre une démo tech aléatoire et quelque chose que les gens ont envie
d'utiliser. La localisation aussi. On fait attention à ça. […]

### [07:00] Architecture et changement de requirements

Et la partie technique : comment vous intégrez les choses, si l'application de votre
architecture a du sens. Et **quand les requirements changent** — parfois vous avez choisi une
architecture, ou votre IA l'a choisie, et les requirements changent — comment vous gérez ça ?
C'est un challenge intéressant, et un point un peu sous-estimé dans les hackathons. On va y
faire un peu plus attention. […]

### [08:00] Critères de jugement

Voici nos critères. On pense qu'il est fair de les mettre sur la table. En bas : la
**présentation**. Désolé, même si on est techniques, la présentation compte beaucoup. Même si
on ne donne pas beaucoup de points, la façon de présenter nous aide à mieux scorer. […]
Je vous encourage vraiment à soigner la présentation. […]

**Implémentation / proof of concept** : c'est une grosse part pour nous. **UX** : pas
seulement le chemin de démo que vous montrez, mais un peu plus que ça. Vous ne pouvez pas
faire un PowerPoint et dire que c'était votre app. **Features, innovation, créativité** :
ça compte, surtout pour ceux qui n'ont pas pris un autre challenge. **Process de
développement authentique** : si vous utilisez l'IA, c'est des points bonus, autour de
**10–15 %** de la note. **[à vérifier]** On est curieux de voir comment vous faites du
« vibe coding » ou moins de vibe coding. […]

### [09:45] Ressources, QR code, Discord

On a des ressources supplémentaires. Est-ce qu'on peut partager les slides ? Les
organisateurs ? […] Il n'y a pas de canal spécifique pour les sponsors ? […] Voici le QR
code. Vous le trouverez probablement aussi sur Discord. Sinon, venez nous voir, on trouvera.
Il y a une liste de ressources utiles. Pour des raisons de temps, je ne rentre pas dans les
détails. […]

### [11:30] Questions — pourcentages

**Question** : quel est le poids exact des critères ? […]

**Intervenant** : On peut être précis. […] Les critères 1 à 5, le 5 étant le bonus. J'ai dit
15 %, c'est en fait **10 %**. **[à vérifier]** Si je me souviens bien, c'était 25 %, 20 %,
20 %, puis 15, 10, 10, 10… C'est un ordre de grandeur. Si vous êtes perdus, venez nous voir ;
si on pense que c'est fair, on le publiera peut-être dans notre canal. […]

### [13:15] Questions — prix

**Question** : et les prix ?

**Intervenant** : Oh mon Dieu, je ne les ai pas mentionnés. Merci. Première place :
**250 € par personne**, cartes cadeaux **Tremendous**, utilisables dans plein de boutiques
en ligne partout dans le monde. Deuxième place : **200 € par personne**. Troisième :
**150 € par personne**. **[à vérifier]** […]

### [14:45] Questions — jury et disponibilité

**Question** : qui juge ?

**Intervenant** : Ce sera les personnes du stand, celles que vous voyez aujourd'hui. Je ne
pourrai pas m'impliquer sur tout, je dois rester un peu neutre, mais oui, ce sont eux.
Je vous encourage à venir parler de votre implémentation technique, on est ouverts. […]

**Question** : est-ce qu'on peut vous déranger à 3 h du matin ?

**Intervenant** : Vous pouvez aussi poser vos questions dans le canal. […]

### [16:00] Conclusion

Merci. Si vous avez d'autres questions, venez nous voir au stand. On sera ravis de vous aider
et d'entendre vos idées. *[Applaudissements.]*

## À vérifier au stand / sur Discord

- [ ] Nom exact de l'intervenant (l'ASR hésite).
- [ ] Nom exact du programme IA (« AIR » → JetBrains AI / AI Assistant ?).
- [ ] Nom exact du free tier de Junie (« Junie Lite » ?).
- [ ] Poids exacts des critères et du bonus IA (10 % ou 15 % ?).
- [ ] Montants et devise des prix (250/200/150 € par personne ?).
- [ ] Lien des slides / QR code / canal Discord sponsor.
- [ ] Le challenge peut-il être combiné officiellement avec un autre challenge ?

## Sources

- Enregistrement audio de l'équipe, 21/11/2026, 16 min 20 (non commité).
- [docs/CONTEXT.md](../CONTEXT.md) §2 (critères) et §11 (process agentique).
- [docs/DEMO.md](../DEMO.md) (démo 3 min, sans slides).
- [docs/LAUZHACK.md](../LAUZHACK.md) (règles de l'événement).
- [JetBrains AI](https://www.jetbrains.com/ai/) · [Junie](https://www.jetbrains.com/junie/) · [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) — à recouper avec le stand.
