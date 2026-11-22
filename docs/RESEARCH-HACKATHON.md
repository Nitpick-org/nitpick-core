# Research — running a 24 h hackathon at its best

> Summary of the official sources and the operational literature, verified on
> November 16, 2026. Inferences are marked **[inference]**.

## Method

- Primary sources: LauzHack 2026 rules and details page, 2025 gallery, Claude Code
  documentation, AI Hero documentation.
- Secondary sources: MLH, WildHacks, Devpost, AngelHack, CHI 2025 study.
- Each claim is linked to a source at the bottom of the page. Unsourced figures are
  explicit inferences.

## 10 findings → implications

### 1. The 2026 criteria are not the old EPFL criteria

Official: (1) innovative/original/unexpected, (2) convincing demo, smooth, easy to follow,
(3) potential impact. Technical depth is not a separate criterion: it is **evidence**
serving novelty and impact.

→ Optimise novelty + demo + impact. The technical work must be **visible** and **credible**,
not just impressive backstage.

### 2. Novelty is a rule, not a bonus

The rules allow libraries and existing code, but require "novel" work and **explicit
novelty during judging**. The details page adds: say what we created during the event and
what we reused.

→ A before/after novelty sentence. A mental slide "what existed / what we did that is new".
No "ChatGPT for X".

### 3. The format is short, repeated, without slides

3 minutes of demo + 1 minute of Q&A. Slides forbidden; backup images and videos allowed.
Juries see a lot of projects; the demo must be **replayable** and **readable standing up**.

→ A 2:45 script, 5 rehearsals, a 2 min video, a single story.

### 4. Presentation is a threshold, not the score

Public rubrics (WildHacks, MLH) give the essentials to **working** and to
**usefulness**; design and presentation weigh less. Devpost warns against beautiful
landing pages with empty code.

→ A technical core that works, a simple and readable UI. Do not spend 4 h on the CSS.

### 5. The rubrics reward useful complexity

WildHacks example: technical complexity 30 %, usefulness 30 %, originality 20 %, design 15 %,
presentation 5 %. This is not THE LauzHack rubric, but it indicates what juries
valued elsewhere.

→ One hard core that works is worth more than three easy features.

### 6. One sponsor challenge, not two

The rules guarantee at least one winner per challenge and allow several prizes.
But one challenge = one extra jury = extra criteria.

→ One main challenge, only if **≥ 70 % of its requirements are already on the
demo path**. A single product, a single demo.

### 7. The LauzHack pattern: depth + sponsor stack

2025: 1st *logilinux* (low-level Linux driver for Logitech hardware), 2nd *KnowTube*
(YouTube → courses), 3rd *showcAIse* (presentation coach). The challenges cover
accessibility, AI, fintech, legal tech, hardware.

→ Choose a real problem, a central sponsor integration, and a hard part
**visible in 20 seconds**.

### 8. 24 h scope is a discipline, not an ambition

Projects that fail add auth, payments, multi-user, native mobile, or
several unknown integrations. Winners do **one** flow, end to end, with
seeded data.

→ 1 external integration max, fixture fallback, no login, seeded data, deployment
from the first day. If the risky integration is not demonstrable by 19:00 on Saturday, we
cut it.

### 9. Sleep is an architecture decision

An all-nighter degrades the demo, the merges and the Q&A. Winning teams sleep in
shifts and keep 2 people awake.

→ 4 h of sleep per person, shifts at 22:00/02:00/06:00, no all-nighter, freeze at 09:30.

### 10. Agents speed up execution, not judgement

The literature on coding agents is clear: the bottleneck is **review**, not
generation. Multi-agent ≈ 15× the tokens of a chat; agent teams are experimental.
Subagents are useful to absorb the noise (reading, research, review, QA), not to
write 5 features in the same file.

→ 1 ticket = 1 worktree = 1 session; 6 sessions max for 3 humans; AFK only with
a deterministic oracle (`check.sh`); product decisions stay human.

## What this changes for the team

| Decision | Before | After |
| --- | --- | --- |
| Criteria | "technical, novelty, functionality" (2019) | novelty, demo, impact |
| Demo | generic 3 min | 2:45, no slides, 5 rehearsals, 2 min video |
| Novelty | implicit | explicit sentence + "created vs reused" |
| Scope | "we'll see" | 1 integration max, fallback, cut-off at 19:00 |
| Sponsor | aim broad | 1 challenge if ≥ 70 % on the path |
| Sleep | late | 4 h/person, shifts, no all-nighter |
| Agents | many | 6 sessions max, human review, bounded AFK |

## What remains uncertain

- The 2026 challenges and their exact criteria (not published as of 16/11).
- The jury composition and the number of passes.
- The exact weight of the criteria in the final score.
- The detailed AI policy: no clause, but honesty "created vs reused" is
  explicitly requested.

## Sources

- [LauzHack 2026](https://lauzhack.com/) — program, prizes, teams.
- [LauzHack — details](https://lauzhack.com/details) — criteria, 3+1 min, no slides.
- [LauzHack — rules](https://lauzhack.com/pdf/rules.pdf) — novelty, prizes, conduct.
- [LauzHack — sponsor prospectus](https://lauzhack.com/pdf/sponsor-prospectus.pdf) — challenge design.
- [2025 projects](https://2025.lauzhack.com/projects) — winners and challenges.
- [MLH — judging plan](https://guide.mlh.com/general-information/judging-and-submissions/judging-plan.md) — short format, repeated passes.
- [WildHacks — rubric](https://dashboard.wildhacks.net/guide/judging-and-awards/project-evaluation) — weighting of the criteria.
- [Devpost — judging tips](https://info.devpost.com/blog/hackathon-judging-tips) — jury pitfalls.
- [AngelHack — demo tips](https://angelhack.com/blog/10-tips-to-help-you-rock-your-next-hackathon-demo/) — demo structure.
- [CHI 2025 — hackathon projects study](https://arxiv.org/abs/2503.04290) — usefulness/novelty filter.
- [Claude Code — subagents](https://code.claude.com/docs/en/sub-agents) · [costs](https://code.claude.com/docs/en/costs)
- [Anthropic — multi-agent research system](https://www.anthropic.com/engineering/multi-agent-research-system)
- [AI Hero — 7 phases](https://aihero.dev/my-7-phases-of-ai-development) · [Ralph](https://www.aihero.dev/tips-for-ai-coding-with-ralph-wiggum)
