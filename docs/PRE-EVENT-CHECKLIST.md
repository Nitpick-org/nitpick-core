# Pre-H0 checklist — J-5 → J0

> Tick as you go. An unticked item at J-1 is a demo risk, not a detail.
> Owners: H1 product/pitch, H2 architecture/integration, H3 builder/demo.

## J-5 — Monday, November 16

- [ ] Read `docs/LAUZHACK.md`, `docs/STRATEGY.md`, `docs/PLAYBOOK.md`, `docs/AGENT-OPS.md` (whole team).
- [ ] 60-min team call: final roles, availability, sleep, transport.
- [ ] Confirm the LauzHack acceptance and join the Discord.
- [x] Check the **private** GitHub repo (it must become **public** before the submission).
- [x] Invite the 2 others; `main` without protection (direct merge allowed).
- [ ] Send `docs/ONBOARDING.md` to the 2 others; each one clones and runs `doctor.sh`.
- [x] Create the labels (`ticket`, `bug`, `demo`, `P0–P2`, `blocked`, `review`) and the `LauzHack 2026` milestone.
- [ ] Check that all 3 have Git, Node 22+, `gh`, `claude`, an editor.
- [ ] Run `git config core.hooksPath .githooks` in each clone.
- [ ] Fill in `CLAUDE.md`: first names, roles, status sentence.
- [ ] Read `docs/ULTRA.md`; confirm who is on Ultra (H2, H3 by default).
- [ ] Ultra accounts **verified**: Settings > Usage, no "Current session" bar.
- [ ] Note the day/time of the weekly reset for each Ultra account.

**Gate**: repo created, roles written, everyone has read the docs.

## J-4 — Tuesday, November 17

- [ ] Prepare the hosting account (Vercel/Netlify) and the Vite fallback.
- [ ] If the idea is already clear: choose the stack in 15 min (`docs/STACK.md`) and do the bootstrap.
- [ ] Otherwise: stack at H0 (11:40–12:00), then bootstrap and deployment.
- [ ] Check that `.env` is not committed.

**Gate**: a public preview URL responds, `check.sh` is green.

## J-3 — Wednesday, November 18

- [ ] Install Claude Code + AI Hero skills:
      `claude plugin install mattpocock-skills@claude-plugins-official`
      then `/setup-matt-pocock-skills` once per repo.
- [ ] Check that the 8 agents appear (`/agents` or ask Claude).
- [ ] Test `/route-task`: docs → `scribe`/Haiku, standard code → Sonnet, critical → Opus.
- [ ] `./scripts/doctor.sh` with no failure.
- [ ] Test `./scripts/new-worktree.sh test-1`, open `claude` in it, make a mini-commit, then `rm-worktree.sh`.
- [ ] Test `AFK_OK=1 ./scripts/afk-ralph.sh 1` on a fake **low-risk** ticket.
- [ ] Check the hooks: `PreToolUse` blocks a subagent on `PRD.md`; `Stop` blocks if `check.sh` is red.
- [ ] Do the 2 h 30 **dry run** (`docs/DRY-RUN.md`) on a throwaway clone.
- [ ] Fill in `dry-run/RETRO.md`: 3 concrete frictions.
- [ ] Add a Playwright test that follows `docs/DEMO.md` (even empty).
- [ ] Prepare `progress/` and explain the protocol to the agents.

**Gate**: an agent can take a ticket, code, test, commit; `doctor.sh` green.

## J-2 — Thursday, November 19

- [ ] 2-hour dress rehearsal on a **dummy idea**: idea → DEMO → PRD → 3 tickets → 2 agents → demo.
- [ ] Time it: the skeleton must be deployed in < 2 h.
- [ ] Each human prepares **3 ideas** (one sentence, aha, risk #1) for Saturday.
- [ ] Fix the 3 frictions from the dry run (tickets in the real repo).
- [ ] Research the 2026 sponsors and the likely challenges (`researcher`).
- [ ] Prepare the submission form: title, descriptions, thumbnail, link.
- [ ] Prepare a backup video script (2 min) and a screenshots folder.
- [ ] Check the API quotas/plans (DB, hosting, sponsors); app key on the Ultra API credits (`APP_ANTHROPIC_API_KEY`).
- [ ] Week's agent tests: stay **under 25%** of the Ultra weekly limit.
- [ ] Decide who takes notes on the challenges at the tech talks.

**Gate**: the rehearsal runs, the 3 ideas are written, the keys exist.

## J-1 — Friday, November 20

- [ ] `./scripts/doctor.sh` with no failure.
- [ ] Public repo, up-to-date README, `.env` ignored, no secret in the history.
- [ ] API keys created, quotas checked, backup cards.
- [ ] `/usage`: **≥ 75%** of the week available on each Ultra account; `ANTHROPIC_API_KEY` absent from the shell.
- [ ] Decide the paid usage after the limit (Q-004).
- [ ] Gear: laptop, charger, type J adapter, ID, sleeping bag, water bottle, clothes.
- [ ] Phones charged, backup tethering, cables.
- [ ] Final 3-min demo rehearsal (on the dummy idea): 3 run-throughs.
- [ ] Prepare the jury questions (10).
- [ ] Sleep 7–8 h. **No code after 22:00.**
- [ ] Check the time and place (BC, EPFL), the route, the departure time.

**Gate**: everyone is ready, rested, with the keys and the gear.

## J0 — Saturday, November 21

- [ ] **8:00–9:30** check-in, ID, breakfast, table, `eduroam` Wi-Fi, power sockets.
- [ ] **9:30** mini-sync: roles, channels, repo status, preview that responds.
- [ ] **10:00–10:15** opening ceremony: take note of the announcements.
- [ ] **10:20–11:40** tech talks: H1 takes notes on the challenges; H2/H3 prepare the repo.
- [ ] **11:40–12:00** team building: idea, target, novelty sentence, **and stack**
      (Vite fallback if no decision by 12:00).
- [ ] **12:00** H0: create the project on app.lauzhack.com, invite the teammates.
- [ ] **12:00–13:00** kickoff: DEMO.md, PRD, modules, tickets.
- [ ] **13:00** first worktrees.
- [ ] Upload the travel receipts **before Saturday evening**.

**H0 gate**: demo written in 5 points, PRD, 8–12 tickets, skeleton launched.

## During the night

- [ ] 4-hour sleep shifts: 22:00 H3, 02:00 H2, 06:00 H1.
- [ ] Always 2 people awake; no all-nighter.
- [ ] AFK only on low-risk tickets with an oracle (`check.sh`).
- [ ] One AFK per Ultra account, started by H2/H3 before their shift; machine plugged in, `caffeinate`.
- [ ] No product decision without a human.
- [ ] `main` stays green; no merge without `check.sh --demo`.
- [ ] Backup video (2 min) recorded before 23:00.

## Sunday — freeze and submission

- [ ] **07:00–09:30** integration, bugs, QA.
- [ ] **09:30–10:30** freeze: no more features, only blocking fixes.
- [ ] **10:30–11:30** form, README, video, public link.
- [ ] **11:30–12:00** 3-min rehearsal ×5, Q&A.
- [ ] **12:00** hacking ends; check that the submission is properly saved.
- [ ] **12:30–15:00** judging; stay until the closing.
