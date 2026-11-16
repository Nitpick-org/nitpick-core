# Checklist avant H0 — J-5 → J0

> Cochez au fur et à mesure. Un point non coché à J-1 est un risque de démo, pas un détail.
> Propriétaires : H1 produit/pitch, H2 archi/intégration, H3 builder/démo.

## J-5 — lundi 16 novembre

- [ ] Lire `docs/LAUZHACK.md`, `docs/STRATEGY.md`, `docs/PLAYBOOK.md`, `docs/AGENT-OPS.md` (toute l'équipe).
- [ ] Appel équipe de 60 min : rôles définitifs, disponibilités, sommeil, transport.
- [ ] Confirmer l'acceptation LauzHack et rejoindre le Discord.
- [ ] Vérifier le repo GitHub **privé** (il devra être **public** avant la soumission).
- [ ] Inviter les 2 autres, protéger `main` (PR + CI verte).
- [ ] Envoyer `docs/ONBOARDING.md` aux 2 autres ; chacun clone et lance `doctor.sh`.
- [ ] Vérifier que les 3 ont Git, Node 22+, `gh`, `claude`, un éditeur.
- [ ] Lancer `git config core.hooksPath .githooks` dans chaque clone.
- [ ] Remplir `CLAUDE.md` : prénoms, rôles, phrase d'état.
- [ ] Lire `docs/ULTRA.md` ; confirmer qui est sur Ultra (H2, H3 par défaut).
- [ ] Comptes Ultra **vérifiés** : Settings > Usage, pas de barre « Current session ».
- [ ] Noter le jour/heure du reset hebdomadaire de chaque compte Ultra.

**Porte** : repo créé, rôles écrits, tout le monde a lu les docs.

## J-4 — mardi 17 novembre

- [ ] Choisir la stack en 15 min (`docs/STACK.md`) — la plus maîtrisée, pas la plus hype.
- [ ] Bootstrap : app hello world, `package.json` avec `dev`, `build`, `typecheck`, `lint`, `test`, `e2e`, `format`.
- [ ] `./scripts/check.sh` vert en < 60 s.
- [ ] Déployer le hello world (preview par branche) et vérifier l'URL.
- [ ] Ajouter `/api/health` (ou équivalent) qui répond 200.
- [ ] Mettre `.env.example` à jour ; créer `.env` local.
- [ ] Vérifier que `.env` n'est pas commité.
- [ ] CI verte sur la PR de bootstrap.

**Porte** : une URL de preview publique répond, `check.sh` est vert.

## J-3 — mercredi 18 novembre

- [ ] Installer Claude Code + skills AI Hero :
      `claude plugin install mattpocock-skills@claude-plugins-official`
      puis `/setup-matt-pocock-skills` une fois par repo.
- [ ] Vérifier que les 7 agents apparaissent (`/agents` ou demande à Claude).
- [ ] `./scripts/doctor.sh` sans échec.
- [ ] Tester `./scripts/new-worktree.sh test-1`, ouvrir `claude` dedans, faire un mini-commit, puis `rm-worktree.sh`.
- [ ] Tester `AFK_OK=1 ./scripts/afk-ralph.sh 1` sur un faux ticket **low-risk**.
- [ ] Vérifier les hooks : `PreToolUse` bloque un sous-agent sur `PRD.md` ; `Stop` bloque si `check.sh` est rouge.
- [ ] Faire le **dry run** de 2 h 30 (`docs/DRY-RUN.md`) sur un clone jetable.
- [ ] Remplir `dry-run/RETRO.md` : 3 frictions concrètes.
- [ ] Ajouter un test Playwright qui suit `docs/DEMO.md` (même vide).
- [ ] Préparer `progress/` et expliquer le protocole aux agents.

**Porte** : un agent peut prendre un ticket, coder, tester, commiter ; `doctor.sh` vert.

## J-2 — jeudi 19 novembre

- [ ] Répétition générale de 2 h sur une **idée bidon** : idée → DEMO → PRD → 3 tickets → 2 agents → démo.
- [ ] Chronométrer : le squelette doit être déployé en < 2 h.
- [ ] Chaque humain prépare **3 idées** (une phrase, aha, risque n°1) pour samedi.
- [ ] Corriger les 3 frictions du dry run (tickets dans le vrai repo).
- [ ] Rechercher les sponsors 2026 et les challenges probables (`researcher`).
- [ ] Préparer le formulaire de soumission : titre, descriptions, miniature, lien.
- [ ] Préparer un script de vidéo de secours (2 min) et un dossier de captures.
- [ ] Vérifier les quotas/plans API (DB, hébergement, sponsors) ; clé de l'app sur les crédits API Ultra (`APP_ANTHROPIC_API_KEY`).
- [ ] Tests d'agents de la semaine : rester **sous 25 %** de la limite hebdo Ultra.
- [ ] Décider qui note les challenges aux tech talks.

**Porte** : la répétition tourne, les 3 idées sont écrites, les clés existent.

## J-1 — vendredi 20 novembre

- [ ] `./scripts/doctor.sh` sans échec.
- [ ] Repo public, README à jour, `.env` ignoré, aucun secret dans l'historique.
- [ ] Clés API créées, quotas vérifiés, cartes de secours.
- [ ] `/usage` : **≥ 75 %** de la semaine disponible sur chaque compte Ultra ; `ANTHROPIC_API_KEY` absente du shell.
- [ ] Décider l'usage payant après limite (Q-004).
- [ ] Matériel : laptop, chargeur, adaptateur type J, ID, sac de couchage, gourde, habits.
- [ ] Téléphones chargés, partage de connexion de secours, câbles.
- [ ] Répétition finale de la démo de 3 min (sur l'idée bidon) : 3 passages.
- [ ] Préparer les questions du jury (10).
- [ ] Dormir 7–8 h. **Pas de code après 22:00.**
- [ ] Vérifier l'heure et le lieu (BC, EPFL), le trajet, l'heure de départ.

**Porte** : tout le monde est prêt, reposé, avec les clés et le matériel.

## J0 — samedi 21 novembre

- [ ] **8:00–9:30** check-in, ID, petit-déj, table, Wi-Fi `eduroam`, prises.
- [ ] **9:30** mini-sync : rôles, canaux, état du repo, preview qui répond.
- [ ] **10:00–10:15** cérémonie d'ouverture : noter les annonces.
- [ ] **10:20–11:40** tech talks : H1 note les challenges ; H2/H3 préparent le repo.
- [ ] **11:40–12:00** team building : idée, cible, phrase de nouveauté.
- [ ] **12:00** H0 : créer le projet sur app.lauzhack.com, inviter les coéquipiers.
- [ ] **12:00–13:00** kickoff : DEMO.md, PRD, modules, tickets.
- [ ] **13:00** premiers worktrees.
- [ ] Uploader les reçus de voyage **avant samedi soir**.

**Porte H0** : démo écrite en 5 points, PRD, 8–12 tickets, squelette lancé.

## Pendant la nuit

- [ ] Shifts de sommeil de 4 h : 22:00 H3, 02:00 H2, 06:00 H1.
- [ ] Toujours 2 personnes éveillées ; pas de all-nighter.
- [ ] AFK uniquement sur des tickets low-risk avec oracle (`check.sh`).
- [ ] Un AFK par compte Ultra, lancé par H2/H3 avant leur shift ; machine branchée, `caffeinate`.
- [ ] Aucune décision produit sans humain.
- [ ] `main` reste verte ; pas de merge sans `check.sh --demo`.
- [ ] Vidéo de secours (2 min) enregistrée avant 23:00.

## Dimanche — gel et soumission

- [ ] **07:00–09:30** intégration, bugs, QA.
- [ ] **09:30–10:30** gel : plus de feature, seulement des correctifs bloquants.
- [ ] **10:30–11:30** formulaire, README, vidéo, lien public.
- [ ] **11:30–12:00** répétition 3 min ×5, Q&A.
- [ ] **12:00** hacking ends ; vérifier que la soumission est bien enregistrée.
- [ ] **12:30–15:00** judging ; rester jusqu'à la clôture.
