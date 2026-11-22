# Strategy — how to win a 24 h hackathon

> This document does not choose the idea: ideas come from humans. It gives a framework
> to choose fast, cut right, and turn an idea into a demo that wins.

## The formula

At LauzHack, the jury scores three things ([details](https://lauzhack.com/details)):

1. **Novelty** — innovative, original, unexpected.
2. **Demo** — convincing, smooth, easy to follow, in 3 minutes without slides.
3. **Impact** — the difference it can make.

Technical depth is not an explicit criterion, but it is a **credibility multiplier**: it
makes the novelty believable and the impact concrete. The 2025 winner
(*logilinux*, Linux support for a Logitech protocol) is an example of technical depth
serving a real problem and a readable demo.

**Working formula:**

> A real problem + one single thing nobody expects + a demo you can see in
> 20 seconds + credible technical proof + one sentence of novelty.

## Choosing the idea (H0, 45 minutes max)

### Process

1. Each human arrives with **3 ideas**, no more. For each one:
   - one sentence: "For <person>, <problem>; we do <solution>."
   - the **aha moment**: what the jury sees on screen in 20 seconds.
   - the **technical risk #1**: what can take 6 hours and fail.
2. 2 minutes per idea, no debate. We take notes live.
3. Blind scoring with the grid below (everyone scores, we average).
4. Keep the **2 best**, grill them 20 minutes each with the `devils-advocate` agent
   and the `researcher` subagent.
5. Choose. Write `docs/DEMO.md` **before** the PRD. If the demo cannot be written in 5 points,
   the idea is not ready.

### Score grid (100 points)

| Criterion | Points | 1 (weak) | 3 (fair) | 5 (strong) |
| --- | --- | --- | --- | --- |
| **Novelty** | 30 | re-skin of an existing tool | original combination | the jury can name what is new |
| **Demo / feature** | 30 | the happy path breaks | 3 clean runs | 3 runs + 1 edge case, live |
| **Impact** | 20 | no identifiable user | useful to a group | one named person, a clear difference |
| **Technical depth** | 10 | glue/CRUD | one non-trivial part | the hard core works live |
| **Sponsor challenge fit** | 10 | simple logo | API used | API/hardware at the centre, checklist completed |

Score each criterion from 1 to 5, then convert: `points = note / 5 × poids`.
The total is out of 100.

**Elimination thresholds:**

- Demo ≤ 2/5 → we cut, even if the idea is beautiful.
- Novelty ≤ 2/5 → we cut: the jury has seen 10 projects like that.
- Impact ≤ 2/5 → discuss: an impressive technical hack can compensate.
- Feasibility: if the riskiest integration is not demonstrable **by 19:00 Saturday**,
  we cut it. No "we will see tonight".

> Presentation is a **threshold**, not the score: a smooth demo does not save an empty
> product, but a confusing demo kills a good product. Fix the substance first, the flow next.

### Questions to ask before saying yes

- Can we **show** the result without login, without setup, without waiting?
- What is the **single** thing the jury must remember? Can we say it in one sentence?
- Which part can be **mocked** without lying in the demo?
- Do we have access to the API, the hardware, the data, the sponsor account **today**?
- How long for a **working skeleton**? If > 4 h, the idea is too risky.
- Does a direct competitor already exist? If so, what is **our** difference?

## What kills an idea in 24 h

| Trap | Why it is deadly | Alternative |
| --- | --- | --- |
| Auth / accounts / onboarding | 2–4 h lost, fragile demo | guest mode, local session, seeded data |
| Real payment | KYC, keys, 3-D Secure | clear mock, screenshot |
| Multi-user / complex real time | races, permissions, bugs | a single user, local state |
| 3 unknown integrations | a single one can take the whole day | **1 external integration max**, with a fixture fallback |
| Native mobile app / store | build, signing, review | mobile web, PWA |
| Fine-tuning / training | data, GPU, time | pre-trained API + prompt |
| Hardware not available | impossible to test | simulator + video |
| "We will add AI later" | AI is often the heart of the aha | integrate the core early |
| Too many features | none of them is finished | one perfect vertical slice |

## The 3-minute demo

### Structure

| Time | Content | Who |
| --- | --- | --- |
| 0:00–0:25 | One specific person, their pain, in one sentence | H1 |
| 0:25–0:40 | "Today, they do X; it costs Y" | H1 |
| 0:40–2:15 | **Live demo**, one single continuous flow, we narrate what we do | H3 |
| 2:15–2:35 | What is new, and what is reused (honesty) | H2 |
| 2:35–2:50 | Impact, who else, at what scale | H1 |
| 2:50–3:00 | The sentence the jury must remember | H1 |

### Rules

- **No slides.** It is forbidden by the rules. Prepare images and a backup video
  in case the demo breaks.
- **One single story.** No tour of every feature. The jury must be able to tell
  someone else about your project afterwards.
- **Show, don't tell.** "Look" beats "our architecture…".
- **Zero dead time.** If a load takes 5 seconds, preload or fill the gap.
- **Realistic data.** No "test 123", no Lorem ipsum.
- **The aha before the technical.** The technical part comes after, in one sentence.
- **Honesty.** Say what is mocked. The jury will see it anyway.

### 30-second version (elevator, Q&A, networking)

> "For <person> who <pain>, we built <product> that <benefit>.
> The novelty is <W>. We demonstrated it in <time> with <proof>."

### Q&A (1 minute)

Prepare 10 answers of 2 sentences:

1. What did you really build during the event?
2. What did you reuse?
3. Why has nobody done it yet?
4. How does it work technically, in one sentence?
5. How much does it cost per user?
6. What breaks at scale?
7. What is the business model?
8. What is the next step if you had 1 week?
9. Why you, and not a large company?
10. What is the ethical / legal limit?

## Sponsor challenges

- The briefs arrive at the tech talks (10:20–11:40) or on Discord. **One person takes notes.**
- Target **1 main challenge**, possibly 1 secondary one if the same core serves both.
  Rule of thumb: only target a challenge if **≥ 70 % of its requirements are already on the
  demo path**. Otherwise, you build two products and finish neither.
- Each challenge = one extra jury = one extra chance at a prize. But a product
  twisted to fit a challenge shows.
- Sponsors judge **their criteria**, not necessarily the global criteria: read the brief,
  identify the metric they want to see move, and show it.
- You can win the overall prize **and** a challenge (rule 34).
- Keep a mental slide (not a slide!) "what this changes for <sponsor>" for the Q&A.

## The 24 h plan

| Block | Time | Goal | Gate |
| --- | --- | --- | --- |
| Kickoff | 12:00–13:00 | Idea chosen, DEMO.md, PRD, skeleton | Demo written in 5 points |
| Skeleton | 13:00–16:00 | End-to-end demo path, even ugly, deployed | Working preview |
| Core | 16:00–19:00 | The feature that carries the aha | The aha is visible |
| Second feature | 19:00–23:00 | Depth or a second use case | Complete demo |
| Freeze v0.1 | 23:00–00:00 | Tag, backup video, submission as draft | v0.1 recorded |
| Night | 00:00–07:00 | Sleep in shifts, agents bounded to low-risk work | 2 humans awake max |
| Integration | 07:00–09:30 | Fix, polish, rehearse | e2e green |
| Final freeze | 09:30–10:30 | No more features, only bugs | Code frozen |
| Submission | 10:30–11:30 | Form, README, video | Submitted before 12:00 |
| Buffer | 11:30–12:00 | Rehearsal, Q&A, breathing | Everyone ready |

> The real risk is not "not enough features", it is **a demo that breaks** or
> **an incomplete submission**. The freeze is a feature.

## Internal decision rubric (use at H6, H12, H18)

Score from 1 to 5:

| Question | 1 | 5 |
| --- | --- | --- |
| Does the demo tell a single story? | we explain | we see |
| Is the aha visible in < 30 s? | no | yes |
| Is the novelty explicit? | "it is a chatbot" | "nobody had done W" |
| Is the impact credible? | gadget | real pain |
| Does the demo survive an API outage? | no | yes (mock/video) |
| Is the submission complete? | no | yes |

If the average < 3 at H12: **cut** a feature and redo the demo, do not add.

## Sources

- [LauzHack — details and criteria](https://lauzhack.com/details)
- [LauzHack — rules](https://lauzhack.com/pdf/rules.pdf)
- [Projects 2025](https://2025.lauzhack.com/projects), [2024](https://2024.lauzhack.com/projects), [2023](https://2023.lauzhack.com/projects)
- [AI Hero — 7 phases](https://aihero.dev/my-7-phases-of-ai-development): idea → research → prototype → PRD → tickets → execution → QA.
- [AI Hero — codebases AI agents love](https://www.aihero.dev/how-to-make-codebases-ai-agents-love): deep modules, simple interface, tests.
- [MLH — judging plan](https://guide.mlh.com/general-information/judging-and-submissions/judging-plan.md): short format, repeated runs.
- [WildHacks — rubric](https://dashboard.wildhacks.net/guide/judging-and-awards/project-evaluation): complexity 30, usefulness 30, originality 20, design 15, presentation 5.
- [Devpost — judges](https://info.devpost.com/blog/hackathon-judging-tips): juries spot beautiful landing pages with empty code.
- [AngelHack — demo tips](https://angelhack.com/blog/10-tips-to-help-you-rock-your-next-hackathon-demo/): 30 s problem, 90 s demo, 30 s technical, 30 s next steps.
