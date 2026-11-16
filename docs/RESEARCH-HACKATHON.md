# Recherche — mener au mieux un hackathon de 24 h

> Synthèse des sources officielles et de la littérature opérationnelle, vérifiée le
> 16 novembre 2026. Les inférences sont marquées **[inférence]**.

## Méthode

- Sources primaires : règlement et page détails LauzHack 2026, galerie 2025, documentation
  Claude Code, documentation AI Hero.
- Sources secondaires : MLH, WildHacks, Devpost, AngelHack, étude CHI 2025.
- Chaque affirmation est reliée à une source en bas de page. Les chiffres non sourcés sont
  des inférences explicites.

## 10 constats → implications

### 1. Les critères 2026 ne sont pas les anciens critères EPFL

Officiel : (1) innovant/original/inattendu, (2) démo convaincante, fluide, facile à suivre,
(3) impact potentiel. La profondeur technique n'est pas un critère séparé : c'est une
**preuve** au service de la nouveauté et de l'impact.

→ Optimiser nouveauté + démo + impact. La technique doit être **visible** et **crédible**,
pas seulement impressionnante en coulisses.

### 2. La nouveauté est une règle, pas un bonus

Le règlement autorise les bibliothèques et le code existant, mais exige un travail
« novel » et une nouveauté **explicite pendant le jugement**. La page détails ajoute :
dire ce qu'on a créé pendant l'événement et ce qu'on a réutilisé.

→ Une phrase de nouveauté avant/après. Un slide mental « ce qui existait / ce qu'on a fait
de neuf ». Pas de « ChatGPT pour X ».

### 3. Le format est court, répété, sans slides

3 minutes de démo + 1 minute de Q&A. Slides interdites ; images et vidéos de secours
autorisées. Les jurys voient beaucoup de projets ; la démo doit être **rejouable** et
**lisible debout**.

→ Script de 2:45, 5 répétitions, vidéo de 2 min, une seule histoire.

### 4. La présentation est un seuil, pas le score

Les grilles publiques (WildHacks, MLH) donnent l'essentiel au **fonctionnement** et à
l'**utilité** ; le design et la présentation pèsent moins. Devpost met en garde contre les
belles landing pages au code vide.

→ Cœur technique qui marche, UI simple et lisible. Ne pas passer 4 h sur le CSS.

### 5. Les rubriques récompensent la complexité utile

Exemple WildHacks : complexité technique 30 %, utilité 30 %, originalité 20 %, design 15 %,
présentation 5 %. Ce n'est pas LA grille de LauzHack, mais ça indique ce que les jurys
valorisaient ailleurs.

→ Un cœur difficile qui marche vaut mieux que trois features faciles.

### 6. Un challenge sponsor, pas deux

Les règles garantissent au moins un gagnant par challenge et autorisent plusieurs prix.
Mais un challenge = un jury supplémentaire = des critères supplémentaires.

→ Un challenge principal, seulement si **≥ 70 % de ses exigences sont déjà sur le chemin
de démo**. Un seul produit, une seule démo.

### 7. Le pattern LauzHack : profondeur + stack sponsor

2025 : 1ᵉʳ *logilinux* (driver Linux bas niveau pour du matériel Logitech), 2ᵉ *KnowTube*
(YouTube → cours), 3ᵉ *showcAIse* (coach de présentation). Les challenges couvrent
accessibilité, IA, fintech, legal tech, hardware.

→ Choisir un problème réel, une intégration sponsor centrale, et une partie difficile
**visible en 20 secondes**.

### 8. Le scope 24 h est une discipline, pas une ambition

Les projets qui échouent ajoutent auth, paiements, multi-utilisateur, mobile natif, ou
plusieurs intégrations inconnues. Les gagnants font **un** flux, de bout en bout, avec des
données seedées.

→ 1 intégration externe max, fallback fixture, pas de login, données seedées, déploiement
dès le premier jour. Si l'intégration risquée n'est pas démontrable à 19:00 samedi, on la
coupe.

### 9. Le sommeil est une décision d'architecture

Un all-nighter dégrade la démo, les merges et le Q&A. Les équipes qui gagnent dorment en
shifts et gardent 2 personnes éveillées.

→ 4 h de sommeil par personne, shifts 22:00/02:00/06:00, pas de all-nighter, gel à 09:30.

### 10. Les agents accélèrent l'exécution, pas le jugement

La littérature sur les agents de code est claire : le goulot est la **revue**, pas la
génération. Multi-agent ≈ 15× les tokens d'un chat ; les agent teams sont expérimentales.
Les subagents sont utiles pour absorber le bruit (lecture, recherche, review, QA), pas pour
écrire 5 features dans le même fichier.

→ 1 ticket = 1 worktree = 1 session ; 6 sessions max pour 3 humains ; AFK seulement avec
un oracle déterministe (`check.sh`) ; les décisions produit restent humaines.

## Ce que ça change pour l'équipe

| Décision | Avant | Après |
| --- | --- | --- |
| Critères | « technique, nouveauté, fonctionnalité » (2019) | nouveauté, démo, impact |
| Démo | 3 min générique | 2:45, sans slides, 5 répétitions, vidéo 2 min |
| Nouveauté | implicite | phrase explicite + « créé vs réutilisé » |
| Scope | « on verra » | 1 intégration max, fallback, coupe à 19:00 |
| Sponsor | viser large | 1 challenge si ≥ 70 % sur le chemin |
| Sommeil | tard | 4 h/personne, shifts, pas d'all-nighter |
| Agents | beaucoup | 6 sessions max, revue humaine, AFK borné |

## Ce qui reste incertain

- Les challenges 2026 et leurs critères exacts (non publiés au 16/11).
- La composition du jury et le nombre de passages.
- Le poids exact des critères dans la note finale.
- La politique IA détaillée : aucune clause, mais l'honnêteté « créé vs réutilisé » est
  explicitement demandée.

## Sources

- [LauzHack 2026](https://lauzhack.com/) — programme, prix, équipes.
- [LauzHack — détails](https://lauzhack.com/details) — critères, 3+1 min, sans slides.
- [LauzHack — règles](https://lauzhack.com/pdf/rules.pdf) — nouveauté, prix, conduite.
- [LauzHack — prospectus sponsors](https://lauzhack.com/pdf/sponsor-prospectus.pdf) — design des challenges.
- [Projets 2025](https://2025.lauzhack.com/projects) — gagnants et challenges.
- [MLH — judging plan](https://guide.mlh.com/general-information/judging-and-submissions/judging-plan.md) — format court, passages répétés.
- [WildHacks — rubric](https://dashboard.wildhacks.net/guide/judging-and-awards/project-evaluation) — pondération des critères.
- [Devpost — judging tips](https://info.devpost.com/blog/hackathon-judging-tips) — pièges des jurys.
- [AngelHack — demo tips](https://angelhack.com/blog/10-tips-to-help-you-rock-your-next-hackathon-demo/) — structure de démo.
- [CHI 2025 — hackathon projects study](https://arxiv.org/abs/2503.04290) — filtre utilité/nouveauté.
- [Claude Code — subagents](https://code.claude.com/docs/en/sub-agents) · [coûts](https://code.claude.com/docs/en/costs)
- [Anthropic — multi-agent research system](https://www.anthropic.com/engineering/multi-agent-research-system)
- [AI Hero — 7 phases](https://aihero.dev/my-7-phases-of-ai-development) · [Ralph](https://www.aihero.dev/tips-for-ai-coding-with-ralph-wiggum)
