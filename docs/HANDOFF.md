# Handoff — état du projet au 21/11/2026 16:45

> Pour la review à 3. Objectif : comprendre **ce qui a changé**, **pourquoi**, **ce qui
> reste ouvert**, et **ce qu'il faut décider** avant de repartir.
>
> Référence produit : [`docs/CONTEXT.md`](CONTEXT.md). Référence challenge :
> [`docs/JETBRAINS-CHALLENGE.md`](JETBRAINS-CHALLENGE.md).

## 0. Snapshot

| Champ | Valeur |
| --- | --- |
| Branche | `main` |
| Dernier commit | `b58832b` |
| État | pas de PR ouverte ; `main` non protégée ; travail par worktree/issue |
| Phase | H0 + ~6 h ; gates 21:00 (review desktop), 02:00 (Android) |
| Démo | 3 min, sans slides ; desktop + Android ; repo de démo à préparer |
| Challenge | JetBrains / KMP — voir `docs/JETBRAINS-CHALLENGE.md` |

## 1. Pourquoi ces changements

Deux choses ont bougé depuis la première spec :

1. **Le design** : pepito a produit un design system JetBrains New UI puis un handoff UI
   (PR #32) ; l'accent est passé du bleu au **teal**, le shell desktop est en place.
2. **Le challenge officiel** : le brief demande de **construire avec JetBrains Air** pour
   le bonus *Agentic Development Process*. Air est un orchestre d'agents dans l'IDE, pas
   une API/provider. La spec devait donc distinguer **Air/Junie = process** et
   **Koog = IA produit**.

## 2. Changements spec

Commit `175121a`, `5d63ce5`, `fdaab6c` :

| Section | Changement |
| --- | --- |
| §1 Identité | DA JetBrains IDE New UI, accent teal, JetBrains Mono |
| §2 Jury | Air/Junie = process de développement, **pas** provider runtime ; bonus multi-agent |
| §3 Équipe | chaque humain installe Air + un agent (Junie Lite/Codex/Gemini/Copilot/Claude) ; logs/captures |
| §5 Stack | **IA produit = Koog** ; **développement = Air + agents** |
| §7 Review | le moteur de review reste Koog ; Air/Junie ne sont pas appelés par l'app |
| §8 WON'T | pas d'intégration runtime Air/Junie (provider/CLI dans l'app) |
| §9 Process | chaque piste pilotée dans Air, fallback Claude Code/Codex CLI |
| §9.1 | ownership des issues H1/H2/H3 |
| §10 | Air installé + premier agent loggé dans la première heure |
| §11 | règles Air/Junie + fallback |
| §13 | la démo montre des captures Air + le `REVIEW_LOG` |
| §14 | risques Air/Junie indisponible, trust/permissions |
| §15 | liens Air, Junie, Koog |

**Décision clé** : on ne branche **pas** Air/Junie dans Nitpick. Le produit garde Koog et
les providers LLM ; Air/Junie servent à construire et à prouver le process.

## 2.1 Audit du 21/11 — findings et suite

L’audit a confirmé que la boucle review réelle a été lancée depuis le desktop (2 nits), puis
a trouvé quatre problèmes corrigés ou à corriger :

| Finding | Statut |
| --- | --- |
| Le diff comparait le tip de `main`, pas le **fork point** ; des fichiers de main apparaissaient supprimés | corrigé par #53 (merge-base + working tree, non-suivis exclus) |
| La review prenait les 20 premiers fichiers **alphabétiques** ; deux sidecars Godot `.import` ont été relus à la place du code | corrigé par #54/#55 (tri par lignes changées, assets ignorés) |
| Koog refusait `claude-opus-5-5` (« does not support tools ») | corrigé par #52 (capabilities du modèle) |
| L’UI affiche `:0` pour un nit de fichier entier | **reste** : à traiter avec #33 (états/affichage review) |
| La boucle complète n’a pas encore été rejouée à la main avec une clé | **reste** : #22 |

## 3. Changements design / UI

| Commit | Quoi |
| --- | --- |
| `1c7e2d1` | design system JetBrains New UI (tokens, composants, catalogue, docs) |
| `1ede34c` | accent **teal** (`#0F766E` / `#5EEAD4`), bleu en lane 5 |
| `a60b12b` | shell desktop : toolbar, CTA « Nitpick branch », branche, inspector à onglets, provider dans la status bar |
| `b10184e` | merge du handoff UI #32 |

À lire :
- [`docs/DESIGN-SYSTEM.md`](DESIGN-SYSTEM.md) — tokens, composants, règles ;
- [`docs/design/UI-HANDOFF.md`](design/UI-HANDOFF.md) — écrans, états, textes ;
- `docs/design/mockups/` — maquettes HTML de référence (pas du code à porter).

## 4. État du code

Déjà là :

- projet KMP `shared` + `desktopApp` + `androidApp` ;
- contrats du §6 (`domain`, `git`, `review`, `settings`) ;
- graphe de commits avec lanes (`graph`) ;
- parseur de diff unifié (`diff`), diff fork-point (#53) ;
- ouverture d'un repo local + branches + commits + détails (desktop) ;
- thème clair/sombre + settings persistés ;
- design system et shell desktop ;
- agent Koog réel (#51), capabilities corrigées (#52), review réelle lancée (2 nits) ;
- boucle UI Diff/Review/Apply/Commit (#50) et contexte review trié (#54/#55).

Pas encore fait :

| Issue | Owner | Quoi |
| --- | --- | --- |
| #22 | H2 | **reste** : passe manuelle de la boucle desktop avec une clé, capture, fermeture |
| #21 | H3 | Android clone + Repos/Graph (en cours) |
| #23 | H3 | boucle review Android |
| #33 | H1 | états de review + affichage `whole file` (le `:0` de l’audit) |
| #34/#35 | H3 | settings desktop/Android selon handoff |
| #39/#44 | H1 | démo, soumission, repo de démo, preuves Air |
| #40 | H1 | comptage des lignes shared |
| #46/#48 | H1 | backend de partage, après 21:00 |
| #36–#38/#42/#43/#45 | — | P2 bloquées, après chemin critique |

## 5. Ownership après inversion

| Rôle | GitHub | Issues | Gate |
| --- | --- | --- | --- |
| **H1 — Produit & Pitch** | `@dovatgabriel` | #39 démo/soumission/Air, #33 états | 17:30 première preuve Air ; 09:30 freeze ; 11:30 soumission |
| **H2 — Architecture & Intégration** | `@zooldyyk` | #19, #20, #22 | **21:00** review desktop |
| **H3 — Builder & Démo** | `@pepito2t` | #21, #23, #34, #35 | **02:00** review Android |

P2 #36–#38 : non assignées, à ne pas commencer avant le chemin critique.

## 6. Preuves Air attendues

Pour le bonus, il faut **des artefacts**, pas juste une déclaration :

- 1 session Air réelle par owner (Junie/Codex/Gemini/Copilot/Claude) ;
- 1 comparaison de **deux agents** sur la même tâche (ex. Junie vs Claude/DeepSeek) ;
- captures : agents côte à côte, diff/résultat, correction humaine ;
- `docs/AGENT_LOG.md` : `Junie via Air`, `Claude via Air`, piste, tâche, décisions ;
- `docs/REVIEW_LOG.md` : ce qui a été rejeté/corrigé et pourquoi ;
- une slide de démo « Agentic Development Process ».

## 7. Review à 3 — checklist 20 min

1. **Challenge** : lire `docs/JETBRAINS-CHALLENGE.md` (Air = process, Koog = produit).
2. **Spec** : lire les sections modifiées de `docs/CONTEXT.md` (§2, §3, §5, §7, §8, §9.1,
   §10, §11, §13, §14).
3. **Design** : lire `docs/DESIGN-SYSTEM.md` + `docs/design/UI-HANDOFF.md` ; vérifier que
   le shell/teal est cohérent.
4. **Issues** : vérifier le mapping H1/H2/H3 et l'ordre #19 → #20 → #22 / #21 → #23.
5. **Code** : `git pull`, `./gradlew :desktopApp:run`, ouvrir un repo, vérifier le shell.
6. **Air** : confirmer que les 3 ont accès à Air/Junie Lite et choisir le second agent
   (Claude, Codex, Gemini, DeepSeek si ACP/BYOK).
7. **Décisions** : confirmer que Junie CLI dans le produit est **hors scope** (stretch
   après 21:00 seulement si la boucle est verte).

## 8. Questions à trancher en review

- [ ] Le mapping H1 = dovat, H2 = zooldyyk, H3 = pepito est-il correct ?
- [ ] Les 3 laptops ont-ils Air + un agent connecté ? Qui utilise Junie Lite ?
- [ ] Quel second agent pour la comparaison ? Claude, Codex, Gemini, DeepSeek ?
- [ ] Confirme-t-on **pas d'intégration runtime Air/Junie** dans l'app ?
- [ ] Le gate 21:00 est-il tenable avec #19/#20/#22 ? Sinon, que coupe-t-on ?
- [ ] Le handoff UI (teal, shell) est-il validé comme DA finale ?

## 9. Commandes utiles

```bash
git pull origin main
git log --oneline -12
gh issue list --repo Nitpick-org/nitpick-core --state open
./gradlew :desktopApp:run
GRADLE_USER_HOME=/tmp/nitpick-gradle ANDROID_USER_HOME=/tmp/nitpick-android \
  ./gradlew :shared:jvmTest :desktopApp:compileKotlin
```

Le check complet `./scripts/check.sh` demande le SDK Android ; sur une machine sans SDK,
valider au moins `:shared:jvmTest` + `:desktopApp:compileKotlin` et le noter.

## 10. Prochaine action

- **H1** : installe Air, lance la première review Junie + second agent, capture, `#39`.
- **H2** : worktree #19, puis #20, puis #22 ; log Air et review humaine.
- **H3** : worktree #21, puis #23 ; log Air et capture Android.
- **Tous** : sync 17:30 avec une capture Air et l'état de l'issue en cours.
