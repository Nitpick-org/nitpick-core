# Challenge JetBrains — Cross-Platform Apps with Kotlin Multiplatform

> **Source** : brief officiel du challenge, fourni par JetBrains / LauzHack 2026
> (`LauzHack.docx`). Ce document est la référence pour le pitch, les critères et les
> bonus ; `docs/CONTEXT.md` reste la spec produit de Nitpick.

## 1. Build with JetBrains Air (recommended)

Le brief **recommande** de construire le projet avec **JetBrains Air** dans un IDE
JetBrains. Ce n'est pas obligatoire, mais cela donne des **points bonus** au titre du
critère *Agentic Development Process*.

Air permet de :

- faire tourner plusieurs agents de code **côte à côte** ;
- suivre et relire leur travail au même endroit ;
- les piloter avec l'intelligence de l'IDE ;
- utiliser les agents déjà connus : **Codex, Gemini, GitHub Copilot, Claude, Junie**, et
  d'autres via **ACP** ;
- utiliser **Junie Lite gratuitement** si l'on n'a pas d'agent/abonnement ;
- laisser les agents travailler avec les outils déterministes de l'IDE (moins d'erreurs,
  moins de tokens).

**Point clé** : Air est un **environnement d'orchestration d'agents dans l'IDE**, pas un
provider IA et pas une dépendance runtime de l'application. Le brief demande de
**construire avec Air**, pas d'appeler une API Air depuis l'app.

## 2. Description du challenge

Kotlin Multiplatform (KMP) permet de partager du code entre plateformes tout en gardant
des expériences natives. Le but est de construire une **app cross-platform** avec KMP :

- écrire la logique une fois et la réutiliser sur Android, iOS, web ou desktop ;
- rester platform-specific là où c'est nécessaire ;
- explorer comment le shared logic rend le développement plus efficace ;
- livrer un produit **poli et user-friendly**.

L'app peut être :

- à impact social (communautés, sensibilisation, problèmes réels) ;
- ou simple et fun (to-do, météo, quiz).

L'important : apprendre, expérimenter, contribuer, et construire avec KMP.

## 3. Features qui donnent des points bonus

- **Dark/light mode**
- **Localisation** (multi-langues)
- **Jetpack Compose / Compose Multiplatform** pour l'UI
- **Kotlin backend integration**
- **Shared AI/agent logic** : si l'app a une feature IA, un framework Kotlin-native comme
  **Koog** permet d'écrire la logique une fois dans `commonMain` et de la faire tourner sur
  toutes les cibles. Ce n'est pas obligatoire et il ne faut pas forcer une feature IA si
  l'idée n'en a pas besoin.

## 4. Résultat final

Une app qui tourne sur **au moins 2 plateformes**.

## 5. Critères de jugement

1. **Implementation and Proof of Concept** — l'app tourne vraiment sur au moins 2
   plateformes, avec la fonctionnalité centrale qui marche.
2. **User Experience and Interface** — poli et **native-feeling** sur chaque plateforme,
   pas seulement fonctionnel.
3. **Technical Features** — part de logique réellement partagée (`commonMain`) vs dupliquée
   par plateforme ; Compose Multiplatform, backend integration, localisation, shared
   AI/agent logic quand c'est pertinent.
4. **Innovation and Creativity** — originalité de l'idée, surtout pour le social-good track.
5. **Agentic Development Process** — **points bonus, non obligatoires** : des agents dans
   **JetBrains Air** ont-ils fait du vrai travail pendant que les humains relisaient et
   pilotaient ? Bonus supplémentaire pour le **multi-agent** : agents différents sur le
   shared code et le platform-specific code, ou comparaison de leurs résultats.
6. **Presentation** — clarté de la démo, avec un compte rendu explicite de ce qui est
   partagé entre plateformes et de ce qui reste spécifique.

## 6. Ressources utiles

- KMP templates : https://kmp.jetbrains.com
- KMP docs : https://kotlinlang.org/docs/multiplatform/multiplatform-create-first-app.html
- Compose Multiplatform : https://www.jetbrains.com/compose-multiplatform/
- Vidéo MVI + Compose Multiplatform : https://youtu.be/dveR4xWid4Q
- Air in JetBrains IDEs / plugin Air : https://blog.jetbrains.com/ai/2026/10/air-in-ides-eap/
- Junie / Junie Lite : https://junie.jetbrains.com
- Koog : https://docs.koog.ai

## 7. Lecture pour Nitpick

| Exigence / bonus | Nitpick | Statut |
| --- | --- | --- |
| App sur **2 plateformes** | Desktop JVM + Android | P0 — en cours |
| KMP / shared logic | `commonMain` + `jvmShared` | P0 — en place |
| Compose Multiplatform | UI `commonMain` | en place |
| Dark/light | `ThemeMode` + thème | en place |
| Localisation EN/FR | `composeResources` | en cours |
| Shared AI/agent logic (Koog) | `ReviewAgent` + Koog | P0 — #20 |
| Kotlin backend | serveur Ktor `server/` : partage des reviews (`CONTEXT.md` §7.1) | P0 après la porte de 21:00 |
| Air / Junie process | H1/H2/H3 utilisent Air | à prouver |
| Multi-agent | Junie + second agent sur une tâche | à faire |
| Démo claire | `docs/DEMO.md` + captures Air | à faire |

## 8. Décisions qui découlent du brief

1. **Air/Junie = process de développement**, pas une API dans l'app.
2. **Koog = moteur IA du produit**, dans `commonMain` ([#20](https://github.com/Nitpick-org/nitpick-core/issues/20)).
3. **Preuves Air** : captures, `AGENT_LOG.md`, `REVIEW_LOG.md`, comparaison de deux agents.
4. **Pas d'intégration runtime Air/Junie** dans Nitpick pour le hackathon : hors scope,
   fallback CLI documenté si besoin.
5. **Au moins 2 plateformes** : desktop + Android ; la démo doit le montrer.
6. **Les 5 features bonus sont visées**, backend Kotlin compris : un serveur Ktor qui
   partage les reviews, alors que la review elle-même reste locale.

## 9. Ce qu'il faut montrer au jury

- Une app qui tourne sur desktop **et** Android.
- La boucle review : diff → nits inline → apply → commit.
- Une review partagée depuis le desktop par le backend Ktor, puis ouverte sur Android.
- Ce qui est partagé (`commonMain`, Koog) vs spécifique.
- Les captures Air avec les agents côte à côte + une correction humaine.
- Une comparaison multi-agent (même tâche, deux agents, résultat conservé).
- Une démo de 3 minutes, sans slides.
