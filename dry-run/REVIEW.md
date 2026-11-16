# Review de la base — <ton prénom>

> À remplir après le dry run. **20 minutes max.** Sois direct, donne des preuves, ne réécris
> pas les docs : propose. Le but est de corriger la base avant H0, pas de choisir l'idée.
>
> Si tu as 5 minutes : sections **1**, **6** et **8**. Si tu as 20 minutes : tout.

## Comment remplir

1. **Verdict** (2 min) : note, GO/NO-GO, top 3.
2. **Preuves** (12 min) : temps, bugs, agents, docs.
3. **Recommandations** (6 min) : P0 / P1 / P2.

Règles :

- Dur sur le fond, pas sur les personnes.
- Une affirmation = une preuve : commande, message d'erreur, timestamp, capture.
- Distingue **bug**, **friction**, **préférence**.
- Si tu n'as pas testé, écris « non testé ». Ne devine pas.
- Pas d'idée produit ici : la review porte sur la base, les docs, les agents et le rythme.

---

## 0. Contexte

| Champ | Valeur |
| --- | --- |
| Review de | <prénom> |
| Date | |
| Commit testé | `...` |
| OS / Node / pnpm / Claude Code | |
| Compte | Ultra / Pro / autre |
| Temps total passé | |
| Dry run fait dans | clone jetable / autre |
| Branche | `dry-run/base` |
| `./scripts/check.sh` final | ✅ / ❌ |
| `./scripts/check.sh --demo` final | ✅ / ❌ |
| Démo 3 min tenue | ✅ / ❌ |

## 1. Verdict en 30 secondes

| Dimension | Poids | Note /5 | Points |
| --- | --- | --- | --- |
| Onboarding | 4 | | |
| Dry run | 5 | | |
| Docs | 3 | | |
| Agents / modèles | 4 | | |
| Confiance pour H0 | 4 | | |
| **Total** | **20** | | **/20** |

- **GO / NO-GO pour H0** :
- **Les 3 choses à corriger avant H0** :
  1.
  2.
  3.
- **La chose qui te ferait gagner le plus de temps** :
- **La chose la plus solide de la base** :

## 2. Onboarding

| Étape | Temps | Statut | Blocage / commentaire |
| --- | --- | --- | --- |
| Invitation + clone | | ✅ / ⚠️ / ❌ | |
| Hooks + `.env` | | | |
| Connexion Claude / Ultra | | | |
| Skills + `/setup-matt-pocock-skills` | | | |
| `doctor.sh` | | | |
| Premier agent lancé | | | |
| Premier worktree | | | |
| Premier `check.sh` vert | | | |

- Temps jusqu'au premier `check.sh` vert :
- Ce qui était clair :
- Ce qui était ambigu :
- Ce qui manquait :
- **Suggestion concrète** :

### Signaux faibles

- J'ai hésité à :
- J'ai supposé que :
- Je n'ai pas compris :
- J'ai cherché sans trouver :

## 3. Dry run

| Métrique | Valeur |
| --- | --- |
| Temps jusqu'au premier `check.sh` vert | |
| Temps jusqu'au premier commit d'agent | |
| Temps jusqu'au premier merge | |
| Temps jusqu'à la première démo | |
| Tickets terminés | /3 |
| Conflits de merge | |
| `check.sh` vert au premier essai | ✅ / ❌ |
| `check.sh --demo` vert | ✅ / ❌ |
| Démo 3 min | ✅ / ❌ |
| Nombre d'agents utilisés | |

- Ce qui a cassé :
- Ce qui a été plus lent que prévu :
- Ce qui a été plus rapide que prévu :
- Ce que tu as dû faire à la main alors que ça devait être automatique :
- **Le moment où tu as failli abandonner** :

### Top 3 pertes de temps

| # | Moment | Minutes perdues | Cause | Correctif |
| --- | --- | --- | --- | --- |
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |

## 4. Docs et repo

| Dimension | Note /5 | Commentaire |
| --- | --- | --- |
| Clarté | | |
| Exactitude | | |
| Complétude | | |
| Cohérence FR / EN | | |
| Longueur / navigabilité | | |

- Doc la plus utile :
- Doc la plus confuse :
- Lien mort / commande fausse / contradiction :
- Ce qu'on peut supprimer :
- Ce qu'on doit ajouter :
- Un nouveau collaborateur comprendrait-il le repo en < 30 min ? ✅ / ❌

## 5. Agents et modèles

| Agent | Testé | Modèle | Verdict | Problème / réglage |
| --- | --- | --- | --- | --- |
| `researcher` | | | | |
| `devils-advocate` | | | | |
| `architect` | | | | |
| `implementer` | | | | |
| `reviewer` | | | | |
| `qa-demo` | | | | |
| `pitch` | | | | |
| `scribe` | | | | |

- `/route-task` utilisé ? ✅ / ❌ — utile ?
- `scribe` utilisé pour la doc ? ✅ / ❌ — qualité ?
- Escalade déclenchée ? ✅ / ❌ — combien, pourquoi ?
- Modèle le plus utile :
- Modèle le plus surdimensionné :
- `/usage` avant / après (ou % consommé) :
- Où les tokens ont brûlé :
- Briefs d'agents : assez clairs ? ✅ / ❌
- Hooks `Stop` / `PreToolUse` : utiles ? fausses alertes ?
- Ce qui manque dans les agents :
- Ce qu'on peut supprimer :

## 6. Bugs et frictions

| # | Sévérité | Où | Repro | Attendu | Obtenu | Correctif proposé |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | P0 / P1 / P2 | | | | | |
| 2 | | | | | | |
| 3 | | | | | | |

> P0 = bloque la démo ou l'onboarding. P1 = ralentit vraiment. P2 = polish.

## 7. Risques pour le vrai hackathon

| Risque | Probabilité | Impact | Mitigation |
| --- | --- | --- | --- |
| Démo | | | |
| Timing | | | |
| Coordination 3 humains | | | |
| Quota Ultra | | | |
| Stack / déploiement | | | |

## 8. Recommandations

### À faire avant H0 (P0)

1.
2.
3.

### Quick wins (P1)

1.
2.

### À ignorer / plus tard (P2)

1.
2.

## 9. Questions / décisions

| Question | Options | Ta recommandation |
| --- | --- | --- |
| | | |
| | | |

## 10. Preuves

- Commandes lancées :
- Logs / captures :
- Branche / commit :
- Temps par étape :
- Ce que tu n'as pas testé :

---

## Le message à renvoyer

```text
Review de <prénom> — commit <sha>

Note : <x>/20 — GO / NO-GO

Top 3 à corriger :
1.
2.
3.

Le plus gros gain de temps :
- ...

Bugs P0 :
- ...

Ce qui manque dans les agents :
- ...

Ce que je n'ai pas testé :
- ...
```
