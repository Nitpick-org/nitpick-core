# Stack — choisir vite, déployer tôt, ne pas casser la démo

> La meilleure stack est celle que **vous savez déboguer à 3 h du matin**. Pas la plus
> hype. Ce document aide à trancher en 15 minutes et à poser un contrat commun.

## Décider en 15 minutes (J-4)

Chaque humain note de 1 à 5 :

| Critère | Question |
| --- | --- |
| Maîtrise | Est-ce que je peux corriger un bug sans IA ? |
| Déploiement | Preview par branche en < 5 min ? |
| Écosystème | Auth, DB, stockage, IA, UI : tout existe ? |
| Feedback | Typecheck + tests + e2e en < 60 s ? |
| Risque | Quelle intégration peut nous coûter 4 h ? |

Prendre la stack avec la **somme la plus haute**, pas la plus populaire. Si égalité :
prendre celle qui a le moins de dépendances externes.

## Trois options raisonnables

| Stack | Pour qui | Forces | Risques |
| --- | --- | --- | --- |
| **Next.js 15 + TS + Tailwind + shadcn/ui + Postgres (Supabase/Neon) + Vercel** | défaut si personne n'a de préférence forte | déploiement preview, écosystème IA, UI rapide, un seul repo | App Router/Server Actions à maîtriser ; Vercel peut coûter |
| **SvelteKit + TS + Tailwind + Postgres + Vercel/Cloudflare** | équipe à l'aise avec Svelte | léger, rapide, peu de magie | moins de templates hackathon |
| **Symfony 8 + Turbo/Stimulus + Postgres + Fly.io/Platform.sh** | équipe PHP forte | Turbo = UI rapide sans SPA, solide | bootstrap plus long, moins d'exemples IA |

> Le repo est **agnostique** : `check.sh` ne connaît que les scripts de `package.json`.
> Si vous choisissez Symfony, gardez les mêmes noms de scripts dans `package.json`
> (`typecheck` → PHPStan, `lint` → PHP-CS-Fixer, `test` → PHPUnit, `e2e` → Playwright).
> `check.sh` reste identique : il appelle les scripts, pas la stack.

## Le contrat de scripts

`package.json` doit exposer :

| Script | Rôle | Obligatoire |
| --- | --- | --- |
| `dev` | serveur local | oui |
| `build` | build de production | oui |
| `typecheck` | TypeScript/PHPStan, zéro erreur | oui |
| `lint` | ESLint/PHP-CS-Fixer | oui |
| `test` | tests unitaires/intégration | oui |
| `e2e` | parcours de `docs/DEMO.md` | oui avant H6 |
| `format` | Prettier/CS-Fixer | recommandé |

Exemple de `package.json` (Next.js) :

```json
{
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "typecheck": "tsc --noEmit",
    "lint": "next lint",
    "test": "vitest run",
    "e2e": "playwright test",
    "format": "prettier --write ."
  }
}
```

## Bootstrap Next.js (si c'est le choix)

Le dossier contient déjà `docs/`, `.claude/`, `scripts/`, `README.md`. Ne les écrasez pas.

```bash
# 1. Generate the app in a temporary directory
cd /tmp
pnpm create next-app@latest lauzhack-app \
  --ts --tailwind --eslint --app --src-dir --import-alias "@/*" --use-pnpm

# 2. Copy into the repo without overwriting docs/.claude/scripts/.github
cd /tmp/lauzhack-app
rsync -a --exclude .git --exclude node_modules ./ "$OLDPWD/"

# 3. Test + UI dependencies
cd "$OLDPWD"
pnpm add -D vitest @vitejs/plugin-react jsdom @testing-library/react @testing-library/jest-dom
pnpm add -D @playwright/test
pnpm dlx shadcn@latest init

# 4. Add the typecheck/test/e2e/format scripts (see the contract above)
pnpm install
./scripts/check.sh
```

Si `pnpm create next-app` refuse un dossier non vide, gardez l'app dans un sous-dossier
`app/` et adaptez les chemins. **Ne perdez pas 2 h sur le scaffold** : au pire, un
`create-vite` + React fait très bien l'affaire pour une démo.

## Ce qu'il faut avoir avant H0

- [ ] Un repo GitHub **public** (le lien de soumission doit être accessible sans login).
- [ ] `main` protégée : PR obligatoire, CI verte.
- [ ] Une app « hello world » **déployée** avec preview par branche.
- [ ] `./scripts/check.sh` vert en < 60 s.
- [ ] `./scripts/check.sh --demo` avec un test Playwright qui suit `docs/DEMO.md`.
- [ ] `.env.example` complet, `.env` local rempli, aucun secret commité.
- [ ] Un endpoint `/api/health` qui répond 200.
- [ ] Un mode `DEMO_MODE=mock` qui permet de faire la démo sans réseau.
- [ ] Des données de démo seedées (`scripts/seed.ts` ou équivalent).
- [ ] Une vidéo de secours de 2 minutes, enregistrée avant H20.

## Variables d'environnement

`.env.example` :

```bash
# App
APP_URL=http://localhost:3000
DEMO_URL=
DEMO_MODE=mock          # mock | live
DEMO_SEED=1

# Data
DATABASE_URL=

# IA (une seule suffit)
ANTHROPIC_API_KEY=
OPENAI_API_KEY=

# Auth (only if you cannot avoid it)
AUTH_SECRET=

# Sponsors / APIs externes
SPONSOR_API_KEY=
```

Règles :

- `.env` n'est **jamais** commité, jamais lu par un agent, jamais mis dans un prompt.
- Validez les variables au démarrage avec `zod` (ou équivalent) et un message clair.
- En démo, si une clé manque : `DEMO_MODE=mock`, pas un crash.

## Déploiement

- **Déployer à H0+1**, pas à H22. Une preview qui marche vaut 10 features.
- Choisir une plateforme avec **preview par branche** (Vercel, Netlify, Cloudflare Pages,
  Fly.io). Le jury peut demander à voir le lien.
- Protéger les previews par mot de passe si elles contiennent des données sensibles.
- Garder un **domaine stable** pour la démo ; éviter les URLs qui changent à chaque deploy.
- Vérifier le deploy **sur le Wi-Fi du lieu**, pas seulement à la maison.

## Résilience de démo

| Risque | Protection |
| --- | --- |
| API sponsor down | mock + cache + fallback local |
| Quota épuisé | clé de secours, réponses précalculées |
| Réseau lent | précharger, afficher un état de chargement, éviter les appels bloquants |
| Base vide | seed script, données de démo réalistes |
| Merge cassé | tag `demo-v1`, branche `demo-freeze`, revert rapide |
| Démo live qui casse | vidéo de secours + captures + mode mock |
| Écran du jury | police lisible, zoom, contraste, pas de menu caché |
| Petit écran | tester 1280×720 et mobile |

## Observabilité minimale

- Un log par étape du parcours de démo (`demo:step=...`).
- Une page `/api/health` qui vérifie DB + clés.
- Un error boundary qui affiche un message utile, pas une stack trace.
- Un bouton « reset demo » qui remet les données seedées.

## Sécurité

- Aucun secret dans le repo, les prompts, les logs, les captures.
- Valider les entrées utilisateur (même en hackathon).
- Ne pas exposer de données personnelles réelles ; utiliser des données synthétiques.
- Si vous utilisez un LLM : limiter les tokens, timeout, pas de clé côté client.
- Lire `docs/AGENT-OPS.md` pour les permissions agents.

## Sources

- [AI Hero — 7 phases](https://aihero.dev/my-7-phases-of-ai-development)
- [AI Hero — codebases AI agents love](https://www.aihero.dev/how-to-make-codebases-ai-agents-love)
- [Next.js docs](https://nextjs.org/docs)
- [Vercel — preview deployments](https://vercel.com/docs/deployments/preview-deployments)
- [Playwright](https://playwright.dev/docs/intro)
