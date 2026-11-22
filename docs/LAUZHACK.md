# LauzHack 2026 — useful facts, rules and logistics

> Verified on 16 November 2026 against the official sources. Uncertain points are
> marked **"to verify"**. Do not confuse this with previous editions.

## At a glance

| | |
| --- | --- |
| Event | LauzHack 2026, 10th edition |
| Dates | Saturday 21 and Sunday 22 November 2026 |
| Location | **BC** building, EPFL, Lausanne |
| Hack duration | **24 h**: Saturday 12:00 → Sunday 12:00 |
| Team | Up to **4 people** (3 is perfectly fine) |
| Theme | Free, or one of the sponsor challenges |
| Prizes | Top 3 overall + organisers' favourite + at least one winner per sponsor challenge |
| Demo | **3 minutes**, then **1 minute of Q&A**, **no slides** |
| Submission | [app.lauzhack.com](https://app.lauzhack.com), before **12:00 on Sunday** |

## Official schedule

### Saturday 21 November

| Time | Event | What the team does |
| --- | --- | --- |
| 8:00–9:30 | Check-in + breakfast | Arrive **before 9:30**, valid ID, pick a good table, test Wi-Fi and power sockets |
| 10:00–10:15 | Opening ceremony | Listen to the announcements, note the challenges |
| 10:20–11:40 | Sponsor tech talks | **One person takes notes**; the others prepare the repo and the questions |
| 11:40–12:00 | Team building | Finalise the idea, the target, the novelty sentence |
| **12:00** | **Hacking starts** | H0: PRD, DEMO, skeleton |
| 12:00–13:00 | Lunch | Eat early, no long meeting |
| 18:30–19:30 | Dinner | 10-minute sync, then back to work |

### Sunday 22 November

| Time | Event | What the team does |
| --- | --- | --- |
| 0:00–1:00 | Midnight snack | Freeze v0.1, backup video |
| 7:00–9:00 | Breakfast | Final integration, QA |
| **12:00** | **Hacking ends** | No more code |
| 11:30–12:30 | Lunch | Submission already done |
| 12:30–15:00 | Judging | 3-minute demo + 1-minute Q&A |
| 15:00–17:00 | Closing ceremony | Stay until the end (required for the refund) |

> The Sunday lunch slot overlaps the end of the hack: that is normal. The submission
> must be wrapped up **before 12:00**, not at 12:15.

## What the jury scores

Official criteria for the overall ranking ([details](https://lauzhack.com/details)):

1. **Innovative, original, unexpected** — the novelty must be explicit.
2. **Convincing, smooth, easy-to-follow demo** — 3 minutes, no slides.
3. **Potential impact** — what difference for the world / for one specific person.

Sponsor challenges are judged **according to each sponsor's own criteria**. One team
can win the overall ranking **and** a challenge (rule 34), and several prizes per
sponsor are possible (rule 33).

### The novelty rule

- Existing libraries and APIs are allowed.
- Code you have already written is allowed.
- But the work must be **novel** relative to those resources, and you must
  **state explicitly what you created during the event and what you reused**.

Consequence: prepare a sentence along the lines of:

> "Before, you needed X to do Y. We do Y in Z, and the new part is W.
> The rest is existing libraries."

## Submission

Submissions go through **[app.lauzhack.com](https://app.lauzhack.com)**, not Devpost.
According to the application's code ([EventManager](https://github.com/LauzHack/EventManager)):

| Field | Constraint | Advice |
| --- | --- | --- |
| Title | ≤ 40 characters | The name + a short subtitle |
| Short description | ≤ 100 characters | The sentence the jury should remember |
| Long description | ≤ 1000 characters | Problem, solution, novelty, what is real vs simulated |
| Link | Public URL | A public GitHub repo with a README explaining how to run it |
| Thumbnail | Image ≤ 300 KB | A screenshot of the main screen, not a logo |
| Challenges | Checkboxes | 1 to 2 maximum, the ones that really match |

- A single member creates the project and **invites the other two**: otherwise no credit.
- The project is editable until it moves to the "JudgingStarted" phase (~12:30).
  **Submit before 12:00** to have some margin.
- No video field in the form: the backup video is for you, not for the
  submission. (To verify on the day.)

## Rules and eligibility

- At least 18 years old, student/apprentice/PhD, or graduated less than 24 months ago.
- Team of **4 people maximum**.
- IP: LauzHack claims no rights over your work.
- Code of conduct: respect, no harassment, no alcohol/drugs. Report via
  [lauzhack.com/sos](https://lauzhack.com/sos) or an organiser.
- **No AI/LLM clause** in the 2026 rules. The explicit obligation remains:
  state what you created during the event and what you reused.
- The rules can be updated up to 7 days before the event.

## Logistics

- **Location**: BC building, EPFL. Geneva airport → Lausanne/Renens train → M1 metro.
- **Wi-Fi**: `eduroam` with your university credentials; free EPFL network as a backup.
- **Power sockets**: **type J** adapter (Swiss 3-pin) if needed.
- **To bring**: valid ID, laptop + charger, adapter, toiletries
  (showers), optional sleeping bag, water bottle.
- **Sleep**: sleep zone with regular shuttles from Saturday evening;
  travel blanket in the welcome kit; **no** dorm Sunday-Monday.
- **Food**: breakfast, lunch, dinner, midnight snack; dietary restrictions
  honoured "if possible".
- **Refund**: receipts uploaded **before Saturday evening**, paid in **CHF cash** at the
  end; you must stay until the closing and demonstrate the hack. Caps: 0 CHF
  (Lausanne), 50 CHF (Switzerland), 100 CHF (Europe), 150 CHF (rest of the world).
- **Exclusivity**: no non-participant guests.

## Sponsors 2026

| Sponsor | Tier | Announced challenge |
| --- | --- | --- |
| Logitech | Gold | Not published |
| Loterie Romande | Gold | Not published |
| IMC | Gold | Not published |
| JetBrains | Silver | Not published |
| Nexthink | Silver | Not published |
| Scholé AI | Silver | Not published |
| AXA | Silver | Not published |
| UBS | Silver | Not published |
| SBB | Silver | Not published |

**No challenge is published to date.** The briefs probably arrive at the tech
talks (10:20–11:40) or on Discord. Huawei is no longer a sponsor in 2026.

## What won (2023–2025)

| Edition | 1st | 2nd | 3rd | Organisers' favourite |
| --- | --- | --- | --- | --- |
| 2025 | *logilinux* — Linux HID++ support for Logitech mice | *KnowTube* — YouTube → interactive course | *showcAIse* — AI presentation coach | *Birdy* — posture tracker |
| 2024 | *Converge* — AI meeting agents | *Chronica* | *Podcastify AI* | *MakeGreatBox* |
| 2023 | *SightSync* — assistant for the visually impaired | *VirtuWheel* | *It's not about winning* | — |

Patterns observed:

- A **deep and credible** hardware integration can win (logilinux).
- An AI product with an **obvious social** benefit and a **very readable** demo wins too.
- Accessibility comes back three years out of three.
- Several teams combine overall ranking + sponsor challenge.
- No public jury feedback exists: do not over-optimise on rumours.

## To verify on site

- Briefs, prizes and constraints of the 9 challenges.
- Exact rooms in BC, floor plan, sleep zone.
- Number of judging rounds and composition of the jury.
- Real end of the closing ceremony and the aperitif.
- Exact fields of the submission form (the code can change).

## Express checklist

- [ ] Arrive before 9:30 with an ID.
- [ ] Note the challenges during the tech talks.
- [ ] Create the project on app.lauzhack.com at 12:00 and invite the teammates.
- [ ] Make the repo public + README before the submission.
- [ ] Upload the travel receipts before Saturday evening.
- [ ] Rehearse the 3-minute demo, without slides, 5 times.
- [ ] Record a backup video.
- [ ] Submit before 12:00 on Sunday.
- [ ] Stay until the closing for the refund.

## Sources

- [LauzHack 2026](https://lauzhack.com/) — schedule, FAQ, sponsors.
- [Participant details](https://lauzhack.com/details) — location, check-in, criteria, demo.
- [Policies](https://lauzhack.com/policies) — acceptance, sleep, refund.
- [Rules PDF](https://lauzhack.com/pdf/rules.pdf) — eligibility, judging, IP, novelty.
- [app.lauzhack.com](https://app.lauzhack.com) — submission.
- [EventManager](https://github.com/LauzHack/EventManager) — submission fields.
- [2025 projects](https://2025.lauzhack.com/projects) — winners and challenges.
- [EPFL News 2019](https://news.epfl.ch/news/augmented-reading-experience-takes-top-prize-at--2/) — old criteria (technical, novelty, functionality); no longer to be trusted alone.
