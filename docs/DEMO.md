# Demo script — 3 minutes, no slides

> Write this **before** the code. If this script does not hold up, the idea is not ready.
> The rules forbid slides; backup images and videos are allowed.

## The line the jury must remember

> "For <person> who <pain>, <product> does <benefit> in <time>.
> The new thing is <W>."

## 3-minute script

> The official slot is 3 minutes. Aim for **2:45**: the margin absorbs the unexpected.
> Also record a **2-minute backup video**.

| Time | Content | Who | What we see |
| --- | --- | --- | --- |
| 0:00–0:20 | The problem, one specific person | H1 | The product on screen, no deck |
| 0:20–0:35 | The before, what it costs | H1 | A screenshot or a sentence, no deck |
| 0:35–2:05 | **Live demo**, a single flow | H3 | The real screen, no edit |
| 2:05–2:25 | The new thing + what is reused | H2 | One sentence, no diagram |
| 2:25–2:40 | Impact, who else, scale | H1 | A credible number or a quote |
| 2:40–2:45 | The line to remember | H1 | Final screen |

### Live demo rules

- **One story.** No tour of every feature.
- **The aha first.** The technique after.
- **Zero dead time.** Preload, seed, hide.
- **Realistic data.** No "test 123".
- **Honesty.** Say what is mocked.
- **No slides.** A backup image, yes; a deck, no.

## 30-second version

> "For <person> who <pain>, we built <product> that <benefit>.
> The new thing is <W>. We demonstrated it in <time> with <proof>."

## Shared vs platform-specific — the number to show

```bash
./scripts/count-lines.sh
```

Reading of 22/11, 10:05 on `main`: **8 965 / 9 092 production lines = 98,6 % shared**
(`commonMain` + `jvmSharedMain`); 127 platform lines; 3 315 test lines excluded.
Re-run `./scripts/count-lines.sh` just before the demo and paste the up-to-date number.

## What we created vs reused

- **Created during the event**: …
- **Reused**: …
- **Mocked**: …

> The rules require saying it clearly. The jury appreciates honesty; it forgives
> a mock, not a lie.

## Q&A — 1 minute

Prepare 10 answers of 2 sentences:

1. What did you really build during the event?
2. What did you reuse?
   > Kotlin Multiplatform, Compose, JGit, Koog and Ktor are reused; the review prompt, the
   > anchoring, the apply loop and the share server are ours. The code was written with Claude
   > Code and, for #255, Junie Lite through the Junie CLI; every agent diff was reviewed by a human.
3. Why has nobody done it before?
4. How does it work, in one sentence?
5. How much does it cost per user?
6. What breaks at scale?
7. What business model?
8. What next step with 1 week?
9. Why you?
10. What ethical/legal limit?

## Plan B

| Failure | Reaction | Ready? |
| --- | --- | --- |
| External API down | open an already shared review on the server, otherwise the backup video | [ ] |
| Slow network | use the backup video | [ ] |
| Empty database | `reset demo` / seed | [ ] |
| Screen that does not display | full-screen PNG screenshot | [ ] |
| Demo that crashes | 2-min video, we keep talking | [ ] |

- **Backup video**: to record before 11:00 (no link yet).
- **Screenshots**: `docs/demo/` (dogfooding on our agent branches)
- **Desktop → Android share replayed** (22/11 02:05, local Ktor server + `adb reverse`): desktop review
  → Share review → opened on the phone from Shared reviews without a model call → Apply:
  `docs/demo/share-proof.md`, screenshots `share-desktop-shared.png`, `share-android-opened.png`,
  `share-android-applied.png`.
- **Android walkthrough replayed** (22/11 00:45, API 36 emulator, GitHub account connected via
  device flow, repo `pepito2t/OpenTab`, branch `perf/thumbnail-lookup` with a lock never
  released and an eviction off-by-one): `docs/demo/android-01-repos-signed-in.png` →
  `android-02-clone-form` → `android-03-branch-checkout` → `android-04-review-two-bugs` (the
  2 bugs found) → `android-05-nit-suggestion` (bottom sheet, suggestion) →
  `android-06-suggestions-applied` → `android-07-push-after-commit` → `android-08-pushed-graph`
  (↑0 ↓0). Commit `cb55041` "Apply review suggestions" visible on GitHub. Second pass
  (01:00): `android-09-commit-message` (subject proposed from the applied nits) and
  `android-10-pushed-banner` (animated confirmation after the push), commit `f91a419`.
- **Demo data**: the captured walkthrough uses `pepito2t/OpenTab`
  (branch `perf/thumbnail-lookup`, two planted bugs). `pocket-ledger` +
  `docs/demo/junie-brief.md` remain the plan B if this repo is recreated; it is not in
  this repository. Prepare the local clones on the laptop and the emulator before the scene.
- **Fallback URL**: none; the demo is local (desktop + Android + Ktor server on the laptop).
- **Backup shared review**: the server keeps its reviews in `~/.nitpick/server/reviews.json`
  across restarts, so share one from the demo clone before the scene and it stays available
  (the key fix in #235 keeps a review shared before that merge visible).
- **Demo repo state (22/11, 10:10)**: `perf/thumbnail-lookup` on GitHub no longer carries the two
  planted bugs (the night's replays fixed them in `cb55041`, `f91a419`, `93089d1`). Before the
  scene, reset it to `3a0510d` (`git push --force origin 3a0510d:perf/thumbnail-lookup`) or push
  a fresh branch from that commit, then re-clone on the laptop and the emulator.
- **Desktop replay on `main` (22/11, 10:15, after the morning's audit merges #229, #231, #233)**:
  Open → graph → Nitpick branch: the review started, its result was not observed (the window was
  taken over by hand); a shared review opened from the local server: OK. Apply → commit → push
  not replayed on this pass (covered by the 02:05 share proof and the 00:45 Android replay).

## Rehearsal

| Run | Time | Duration | Who | Problem | Fixed |
| --- | --- | --- | --- | --- | --- |
| 1 | | | | | [ ] |
| 2 | | | | | [ ] |
| 3 | | | | | [ ] |
| 4 | | | | | [ ] |
| 5 | | | | | [ ] |

Goal: **5 runs**, including 1 with a simulated failure. Time it. If > 2:45,
cut.

## Checklist before going on stage

- [ ] Laptop charged, cable, adapter.
- [ ] Ktor server running, `adb reverse tcp:8080 tcp:8080` in place, a backup shared review.
- [ ] Backup shared review ready (LLM plan B).
- [ ] Backup video accessible offline.
- [ ] Phone on silent.
- [x] Android replayed by hand: Repos → Graph → Nitpick branch → nit in bottom sheet → Apply → Stage & commit → the **Push** button appears after the commit → push visible on GitHub (22/11 00:45, screenshots `docs/demo/android-*.png`).
- [x] Share replayed: desktop review → Share review → Shared reviews on the phone → opening without a model call → Apply (22/11 02:05, `docs/demo/share-proof.md`).
- [ ] All 3 know who speaks when.
- [ ] The novelty line is written on a sticky note.
