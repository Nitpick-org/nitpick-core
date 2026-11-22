# Stack — choose fast, deploy early, do not break the demo

> The best stack is the one **you know how to debug at 3 a.m.** Not the most
> hyped. This document helps you decide in 15 minutes and lay down a common contract.

## Decide in 15 minutes (D-4 or H0)

Each human scores from 1 to 5:

| Criterion | Question |
| --- | --- |
| Mastery | Can I fix a bug without AI? |
| Deployment | Preview per branch in < 5 min? |
| Ecosystem | Auth, DB, storage, AI, UI: does everything exist? |
| Feedback | Typecheck + tests + e2e in < 60 s? |
| Risk | Which integration could cost us 4 h? |

Take the stack with the **highest total**, not the most popular. In case of a tie:
take the one with the fewest external dependencies.

## Three reasonable options

| Stack | For whom | Strengths | Risks |
| --- | --- | --- | --- |
| **Next.js 15 + TS + Tailwind + shadcn/ui + Postgres (Supabase/Neon) + Vercel** | default if nobody has a strong preference | preview deployment, AI ecosystem, fast UI, a single repo | App Router/Server Actions to master; Vercel can get expensive |
| **SvelteKit + TS + Tailwind + Postgres + Vercel/Cloudflare** | a team comfortable with Svelte | lightweight, fast, little magic | fewer hackathon templates |
| **Symfony 8 + Turbo/Stimulus + Postgres + Fly.io/Platform.sh** | a strong PHP team | Turbo = fast UI without a SPA, solid | longer bootstrap, fewer AI examples |

> The repo is **agnostic**: `check.sh` only knows about the scripts in `package.json`.
> If you choose Symfony, keep the same script names in `package.json`
> (`typecheck` → PHPStan, `lint` → PHP-CS-Fixer, `test` → PHPUnit, `e2e` → Playwright).
> `check.sh` stays identical: it calls the scripts, not the stack.

## Decision at H0 (11:40–12:00)

If the stack is not chosen beforehand, it is decided **after the idea**, during team
building, in 15 minutes. Do not let this choice slip past 12:00.

| Type of idea | Default stack | Why |
| --- | --- | --- |
| Web + UI + API/LLM | **Next.js 15 + TS** | UI + API routes in a single repo, Vercel preview |
| Simple web, visual demo | **Vite + React + TS** (dry run starter) | already tested: `check.sh`, e2e, build in a few minutes |
| CLI / backend / data | **Node/TS** or **Python** | as few layers as possible |
| Hardware / sensor | **Python** | fast libraries and scripts |
| Mobile | **Expo / React Native** | preview on a phone |
| Strong PHP team | **Symfony 8 + Turbo** | mastery beats hype |

Rules:

- Take the stack that **2 out of 3** can debug without AI.
- One risky external integration maximum.
- **Known fallback**: the Vite/React starter from the dry run (`dry-run/starter/`) already
  has `check.sh`, `check.sh --demo`, build and e2e. If you hesitate at 12:00, go with it
  and replace Pulse with the idea.
- If no decision by **12:00**, Vite fallback. We do not push back the 16:00 skeleton.

> Waiting for the idea to choose the stack is healthy. Waiting for the idea to **prepare**
> the bootstrap is not: the decision must cost 15 minutes, not 2 hours.

## The script contract

`package.json` must expose:

| Script | Role | Mandatory |
| --- | --- | --- |
| `dev` | local server | yes |
| `build` | production build | yes |
| `typecheck` | TypeScript/PHPStan, zero errors | yes |
| `lint` | ESLint/PHP-CS-Fixer | yes |
| `test` | unit/integration tests | yes |
| `e2e` | the `docs/DEMO.md` walkthrough | yes before H6 |
| `format` | Prettier/CS-Fixer | recommended |

Example of `package.json` (Next.js):

```json
{
  "packageManager": "pnpm@11.28.5",
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

> `packageManager` is **mandatory** if you use pnpm: the CI relies on it to install
> the right version. Without it, `pnpm/action-setup` fails. If you use npm, put
> `"packageManager": "npm@11"` and no `pnpm-lock.yaml`.

## Next.js bootstrap (if that is the choice)

The folder already contains `docs/`, `.claude/`, `scripts/`, `README.md`. Do not overwrite them.

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

If `pnpm create next-app` refuses a non-empty folder, keep the app in an `app/`
subfolder and adapt the paths. **Do not lose 2 h on the scaffold**: at worst, a
`create-vite` + React does the job very well for a demo.

## What you must have before H0

- [ ] A **public** GitHub repo (the submission link must be reachable without login).
- [ ] `main` reachable: direct merge allowed; PRs and CI recommended, not blocking.
- [ ] A "hello world" app **deployed** with a preview per branch.
- [ ] `./scripts/check.sh` green in < 60 s.
- [ ] `./scripts/check.sh --demo` with a Playwright test that follows `docs/DEMO.md`.
- [ ] A complete `.env.example`, a filled local `.env`, no committed secret.
- [ ] A `/api/health` endpoint that answers 200.
- [ ] A `DEMO_MODE=mock` mode that lets you run the demo without a network.
- [ ] Seeded demo data (`scripts/seed.ts` or equivalent).
- [ ] A 2-minute backup video, recorded before H20.

## Environment variables

`.env.example`:

```bash
# App
APP_URL=http://localhost:3000
DEMO_URL=
DEMO_MODE=mock          # mock | live
DEMO_SEED=1

# Data
DATABASE_URL=

# AI for the app (one key is enough)
# Never export ANTHROPIC_API_KEY in your shell: Claude Code would bill the API instead of Ultra.
# Use APP_ANTHROPIC_API_KEY so the app key can never become Claude Code's key.
APP_ANTHROPIC_API_KEY=
OPENAI_API_KEY=

# Auth (only if you cannot avoid it)
AUTH_SECRET=

# Sponsors / external APIs
SPONSOR_API_KEY=
```

Rules:

- `.env` is **never** committed, never read by an agent, never put in a prompt.
- Validate the variables at startup with `zod` (or equivalent) and a clear message.
- During the demo, if a key is missing: `DEMO_MODE=mock`, not a crash.

## Deployment

- **Deploy at H0+1**, not at H22. A working preview is worth 10 features.
- Choose a platform with **preview per branch** (Vercel, Netlify, Cloudflare Pages,
  Fly.io). The jury may ask to see the link.
- Protect the previews with a password if they contain sensitive data.
- Keep a **stable domain** for the demo; avoid URLs that change on every deploy.
- Test the deploy **on the venue's Wi-Fi**, not only at home.

## Demo resilience

| Risk | Protection |
| --- | --- |
| Sponsor API down | mock + cache + local fallback |
| Quota exhausted | backup key, precomputed responses |
| Slow network | preload, show a loading state, avoid blocking calls |
| Empty database | seed script, realistic demo data |
| Broken merge | `demo-v1` tag, `demo-freeze` branch, quick revert |
| Live demo breaking | backup video + screenshots + mock mode |
| Jury screen | readable font, zoom, contrast, no hidden menu |
| Small screen | test 1280×720 and mobile |

## Minimal observability

- One log per step of the demo walkthrough (`demo:step=...`).
- A `/api/health` page that checks DB + keys.
- An error boundary that shows a useful message, not a stack trace.
- A "reset demo" button that restores the seeded data.

## Security

- No secret in the repo, the prompts, the logs, the screenshots.
- Validate user input (even at a hackathon).
- Do not expose real personal data; use synthetic data.
- If you use an LLM: limit tokens, set a timeout, no key on the client side.
- Read `docs/AGENT-OPS.md` for agent permissions.

## Sources

- [AI Hero — 7 phases](https://aihero.dev/my-7-phases-of-ai-development)
- [AI Hero — codebases AI agents love](https://www.aihero.dev/how-to-make-codebases-ai-agents-love)
- [Next.js docs](https://nextjs.org/docs)
- [Vercel — preview deployments](https://vercel.com/docs/deployments/preview-deployments)
- [Playwright](https://playwright.dev/docs/intro)
