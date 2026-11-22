# JetBrains Talk — LauzHack 2026

> Transcript and summary of the JetBrains sponsor talk during LauzHack 2026. This document
> is a **source**: `docs/CONTEXT.md` remains the master document of the Nitpick project.
> Here, we keep what the talk adds, confirms or contradicts.

## Metadata

| Field | Value |
| --- | --- |
| Event | LauzHack 2026, EPFL BC, Lausanne |
| Date | Saturday 21 November 2026 |
| Duration | 16 min 20 |
| Source | Team audio recording, uncommitted (`École Polytechnique Fédérale de Lausanne.m4a`, 48 kHz stereo, AAC) |
| Speaker | JetBrains representative (name unreliable in the audio, noted "Val Dreckman" by the ASR) |
| Transcription | Whisper `large-v3-turbo` locally, then manual cleanup of obvious errors |
| Reliability | Good on meaning, imperfect on names, acronyms and percentages. Uncertain points are marked **[to verify]**. |

> The talk is a sponsor presentation, not the official rules. In case of contradiction,
> the LauzHack rules and `docs/CONTEXT.md` take precedence.

## TL;DR

- **Challenge**: Kotlin Multiplatform. JetBrains mostly judges **how** the project is built,
  not just what it does.
- **What counts**: a working implementation/POC, UX beyond the demo path, architecture and
  the ability to explain your choices, innovation, and an **authentic development process**.
- **AI bonus**: using JetBrains tools (JetBrains AI / Junie) gives bonus points estimated at
  **~10–15 %** of the score. **[to verify]**: the speaker corrected himself several times on
  the exact figure.
- **Prizes**: 1st place **250 € per person**, 2nd **200 €**, 3rd **150 €**, as Tremendous
  gift cards.
- **Judges**: the people at the JetBrains booth, present during the event. Go talk to them,
  ask technical questions, ask for the resources (QR code / Discord).
- **Open challenge**: you can start from your own idea and tie it to the JetBrains challenge;
  you don't need to pick another challenge.

## What the talk adds to `docs/CONTEXT.md`

| Topic | Already in `CONTEXT.md` | What the talk adds | Nitpick action |
| --- | --- | --- | --- |
| Challenge | KMP, desktop + Android, JetBrains | Insists on "how you get things done", not just "what" | Polish the architecture story |
| Architecture | max commonMain, contracts | The judges will **read the repo** and judge the architecture; they will ask how it absorbs a change in requirements | Prepare a 60-second explanation + an example of a handled change |
| Implementation | P0 desktop + Android | "Implementation / proof of concept" = a big share of the points | Both targets must actually run |
| UX | dark/light, localization, states | "Not just the demo path"; the details make the difference | Test empty/error/loading, small screen, keyboard |
| Innovation | Original idea | Still counts, especially if you didn't take another challenge | Keep the local pre-push review as the differentiator |
| Agentic process | Agents + human review | "Authentic development process" bonus if you use AI and show the real work | Show `AGENT_LOG.md` / `REVIEW_LOG.md`, a concrete rejection |
| AI tools | Koog, agents | Using **JetBrains AI / Junie** gives a bonus **[to verify ~10–15 %]** | Use Junie on a real task, log the result |
| Presentation | 3 min, no slides | "Presentation is still a lot" even if few points | Rehearse the demo, no deck |
| Prizes | Not published in the repo | 250/200/150 € per person, Tremendous | No product impact, but worth knowing |
| Judges | Not specified | The booth people judge; go see them | Prepare 3 technical questions for the booth |

### What is confirmed

- Kotlin Multiplatform is indeed the core of the challenge.
- The judges look at the architecture, the commonMain / platform split, and the quality of
  the implementation.
- UX and polish (dark/light, localization, etc.) count.
- The use of AI/agent tools is looked at, and can earn points.
- The presentation counts, even if it weighs less than the implementation.

### What is new or to verify

- The **exact percentages**: the speaker gives a first approximation
  (25 / 20 / 20 / 15 / 10 / 10), then corrects himself on the AI bonus (15 %, then 10 %).
  Treat this as an **order of magnitude**, not an official grid.
- The exact name of the AI program: the ASR writes "AIR"; it is very likely
  **JetBrains AI** / **AI Assistant**. **[to verify]**.
- "Junie Lite": a JetBrains agent with a free tier and free tokens on a smaller model,
  announced as available this year. **[to verify]**.
- The "Tremendous" gift cards and the amounts are given verbally. **[to verify]**.
- The QR code / the slides / the sponsor Discord channel were not captured in the
  transcript. Ask at the booth.

## Concrete actions for Nitpick

1. **Architecture story**: be able to say in 60 s:
   - what lives in `commonMain` (domain, git, graph, diff, review, ui);
   - what stays platform-specific (JGit via `GitRepository`, files, Android);
   - why this split absorbs a change in requirements.
2. **Visible agentic process**: show an example where an agent produced something,
   a human rejected/corrected it (`REVIEW_LOG.md`), and what changed. That is the proof
   of an "authentic development process".
3. **Use JetBrains AI / Junie** on a real task, not in an artificial demo. Log
   the task and the model used. **[to verify]** for the exact bonus.
4. **UX beyond the demo path**: dark/light, FR/EN, empty/error/loading, small screen, keyboard,
   review loading states. The talk repeats that details count.
5. **Presentation**: 3 min, no slides (LauzHack rule), but with an architecture + process
   narrative. Rehearse 5 times.
6. **JetBrains booth**: go there today, ask 3 questions:
   - what exact weight for the architecture and the AI process?
   - which sponsor criteria are eliminatory?
   - where are the slides/the QR/the Discord channel?
7. **Don't over-optimize for the challenge**: the challenge is open; the demo and the
   architecture count more than the logo.

## Useful quotes

> "We're going to focus a little bit more on how you get things done for this challenge and
> not so much on what you get done."

> "We might skim through your repository, judge your architecture a little bit."

> "As requirements change, sometimes you chose an architecture, or your AI chose an
> architecture — how do you handle that?"

> "Implementation and proof of concept, this is a big part for us."

> "The UX, of course, and not just the demo path you show during the presentation."

> "Authentic development process: if you use AI, this is bonus points… around 10–15% of the
> score." **[to verify]**

> "Presentation is still a lot… even if we don't give a lot of points for it."

## Cleaned-up transcript

> **Not word-for-word** transcript: hesitations, repetitions and obvious ASR errors have
> been removed. Cuts are marked `[…]`. Uncertain names and figures are
> flagged.

### [00:00] Introduction

**JetBrains speaker**: How are you doing? […] I represent JetBrains today, and we're
offering you the **Kotlin Multiplatform Challenge**. We're a very technical company. We like
developers, we like tech, and we're going to focus a bit more on **how you do things** than
on **what you do**. […]

### [00:45] JetBrains, products, AI

We're a productivity company for developers. You may know us for IntelliJ, PyCharm, CLion,
or for our language, Kotlin. […] Today, productivity is also a lot about AI. We have our own
tools: **JetBrains AI** (transcribed as "AIR" in the audio), an open platform for using AI in
the IDEs, and **Junie**, our coding agent. […]

Junie has a **free tier** for anyone with a JetBrains account: free tokens on a smaller model,
experimental, available this year. **[to verify]** But the platform is open: you can use
Codex, Gemini, Claude Code, whatever you want. […]

### [03:00] Kotlin Multiplatform

Kotlin is used by millions of people. It's a multiplatform language: you find it wherever Java
is used, in almost every Android app, natively on iOS, and partially on the web. […] You can
ask your AI for the technical details. […]

### [04:15] The challenge is open

The challenge is a bit of a golden hammer: it's up to you to find the nail. It can also be
another task, another challenge from another company. Look at the challenges. If you have an
idea and you're looking for a challenge to fit it into, this one can also work. You don't have
to pick another challenge. […]

We're going to look a bit more at **how it's built**. We might skim through your repo, judge
the architecture. We know it's a hackathon, we respect your time and your mental health. But
we'll do a quick technical check, with more appreciation for **how you chose your architecture**,
whether it makes sense for your project, etc. […]

### [05:45] Developer tools and polish

We like building tools we use ourselves. We encourage you to do the same: something that
you, the people at EPFL, or people from around the world could actually use. […]

We recommend **polish**: dark/light mode, it's a prompt today, but it makes the difference
between a random tech demo and something people want to use. Localization too. We pay
attention to that. […]

### [07:00] Architecture and changing requirements

And the technical part: how you integrate things, whether applying your architecture makes
sense. And **when the requirements change** — sometimes you chose an architecture, or your AI
chose it, and the requirements change — how do you handle that? It's an interesting challenge,
and a somewhat underrated point in hackathons. We're going to pay a bit more attention to it. […]

### [08:00] Judging criteria

Here are our criteria. We think it's fair to put them on the table. At the bottom: the
**presentation**. Sorry, even if we're technical, the presentation counts a lot. Even if we
don't give many points for it, the way you present helps us score you better. […] I really
encourage you to polish the presentation. […]

**Implementation / proof of concept**: this is a big part for us. **UX**: not just the demo
path you show, but a bit more than that. You can't make a PowerPoint and say that was your
app. **Features, innovation, creativity**: that counts, especially for those who didn't take
another challenge. **Authentic development process**: if you use AI, that's bonus points,
around **10–15 %** of the score. **[to verify]** We're curious to see how you do "vibe coding"
or less vibe coding. […]

### [09:45] Resources, QR code, Discord

We have additional resources. Can we share the slides? The organizers? […] There's no specific
channel for sponsors? […] Here's the QR code. You'll probably also find it on Discord.
Otherwise, come see us, we'll figure it out. There's a list of useful resources. For time
reasons, I won't go into the details. […]

### [11:30] Questions — percentages

**Question**: what is the exact weight of the criteria? […]

**Speaker**: We can be precise. […] Criteria 1 to 5, with 5 being the bonus. I said 15 %, it's
actually **10 %**. **[to verify]** If I remember correctly, it was 25 %, 20 %, 20 %, then 15,
10, 10, 10… It's an order of magnitude. If you're lost, come see us; if we think it's fair, we
might publish it in our channel. […]

### [13:15] Questions — prizes

**Question**: and the prizes?

**Speaker**: Oh my God, I didn't mention them. Thank you. First place: **250 € per person**,
**Tremendous** gift cards, usable in lots of shops online all over the world. Second place:
**200 € per person**. Third: **150 € per person**. **[to verify]** […]

### [14:45] Questions — judges and availability

**Question**: who judges?

**Speaker**: It will be the people at the booth, the ones you see today. I won't be able to
get involved in everything, I have to stay somewhat neutral, but yes, it's them. I encourage
you to come talk about your technical implementation, we're open. […]

**Question**: can we bother you at 3 a.m.?

**Speaker**: You can also ask your questions in the channel. […]

### [16:00] Conclusion

Thank you. If you have more questions, come see us at the booth. We'll be happy to help you
and hear your ideas. *[Applause.]*

## To verify at the booth / on Discord

- [ ] Exact name of the speaker (the ASR hesitates).
- [ ] Exact name of the AI program ("AIR" → JetBrains AI / AI Assistant?).
- [ ] Exact name of Junie's free tier ("Junie Lite"?).
- [ ] Exact weights of the criteria and the AI bonus (10 % or 15 %?).
- [ ] Prize amounts and currency (250/200/150 € per person?).
- [ ] Link to the slides / QR code / sponsor Discord channel.
- [ ] Can the challenge be officially combined with another challenge?

## Sources

- Team audio recording, 21/11/2026, 16 min 20 (uncommitted).
- [docs/CONTEXT.md](../CONTEXT.md) §2 (criteria) and §11 (agentic process).
- [docs/DEMO.md](../DEMO.md) (3 min demo, no slides).
- [docs/LAUZHACK.md](../LAUZHACK.md) (event rules).
- [JetBrains AI](https://www.jetbrains.com/ai/) · [Junie](https://www.jetbrains.com/junie/) · [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) — to cross-check with the booth.
