# JetBrains Challenge — Cross-Platform Apps with Kotlin Multiplatform

> **Source**: official challenge brief, provided by JetBrains / LauzHack 2026
> (`LauzHack.docx`). This document is the reference for the pitch, the criteria and the
> bonus; `docs/CONTEXT.md` remains the product spec for Nitpick.

## 1. Build with JetBrains Air (recommended)

The brief **recommends** building the project with **JetBrains Air** in a JetBrains IDE.
This is not mandatory, but it earns **bonus points** under the *Agentic Development
Process* criterion.

Air allows you to:

- run several code agents **side by side**;
- follow and review their work in the same place;
- steer them with the IDE's intelligence;
- use the already known agents: **Codex, Gemini, GitHub Copilot, Claude, Junie**, and
  others via **ACP**;
- use **Junie Lite for free** if you have no agent/subscription;
- let agents work with the IDE's deterministic tools (fewer errors,
  fewer tokens).

**Key point**: Air is an **agent orchestration environment inside the IDE**, not an
AI provider and not a runtime dependency of the application. The brief asks you to
**build with Air**, not to call an Air API from the app.

## 2. Challenge description

Kotlin Multiplatform (KMP) lets you share code across platforms while keeping
native experiences. The goal is to build a **cross-platform app** with KMP:

- write the logic once and reuse it on Android, iOS, web or desktop;
- stay platform-specific where it is necessary;
- explore how shared logic makes development more efficient;
- ship a **polished and user-friendly** product.

The app can be:

- social impact (communities, awareness, real problems);
- or simple and fun (to-do, weather, quiz).

What matters: learn, experiment, contribute, and build with KMP.

## 3. Features that earn bonus points

- **Dark/light mode**
- **Localization** (multi-language)
- **Jetpack Compose / Compose Multiplatform** for the UI
- **Kotlin backend integration**
- **Shared AI/agent logic**: if the app has an AI feature, a Kotlin-native framework such as
  **Koog** lets you write the logic once in `commonMain` and run it on
  all targets. It is not mandatory, and you should not force an AI feature if
  the idea does not need one.

## 4. Final result

An app that runs on **at least 2 platforms**.

## 5. Judging criteria

1. **Implementation and Proof of Concept** — the app really runs on at least 2
   platforms, with the core feature working.
2. **User Experience and Interface** — polished and **native-feeling** on each platform,
   not just functional.
3. **Technical Features** — the share of logic genuinely shared (`commonMain`) vs duplicated
   per platform; Compose Multiplatform, backend integration, localization, shared
   AI/agent logic where relevant.
4. **Innovation and Creativity** — originality of the idea, especially for the social-good track.
5. **Agentic Development Process** — **bonus points, not mandatory**: did agents in
   **JetBrains Air** do real work while humans reviewed and
   steered? Extra bonus for **multi-agent**: different agents on the
   shared code and the platform-specific code, or a comparison of their results.
6. **Presentation** — clarity of the demo, with an explicit account of what is
   shared across platforms and what stays platform-specific.

## 6. Useful resources

- KMP templates: https://kmp.jetbrains.com
- KMP docs: https://kotlinlang.org/docs/multiplatform/multiplatform-create-first-app.html
- Compose Multiplatform: https://www.jetbrains.com/compose-multiplatform/
- MVI + Compose Multiplatform video: https://youtu.be/dveR4xWid4Q
- Air in JetBrains IDEs / Air plugin: https://blog.jetbrains.com/ai/2026/10/air-in-ides-eap/
- Junie / Junie Lite: https://junie.jetbrains.com
- Koog: https://docs.koog.ai

## 7. Reading for Nitpick

| Requirement / bonus | Nitpick | Status |
| --- | --- | --- |
| App on **2 platforms** | Desktop JVM + Android | P0 — in progress |
| KMP / shared logic | `commonMain` + `jvmShared` | P0 — in place |
| Compose Multiplatform | UI `commonMain` | in place |
| Dark/light | `ThemeMode` + theme | in place |
| Localization EN/FR | `composeResources` | in progress |
| Shared AI/agent logic (Koog) | `ReviewAgent` + Koog | P0 — #20 |
| Kotlin backend | Ktor `server/` server: review sharing (`CONTEXT.md` §7.1) | P0 after the 21:00 gate |
| Air / Junie process | H1/H2/H3 use Air | to prove |
| Multi-agent | Junie + second agent on one task | to do |
| Clear demo | `docs/DEMO.md` + Air screenshots | to do |

## 8. Decisions that follow from the brief

1. **Air/Junie = development process**, not an API inside the app.
2. **Koog = the product's AI engine**, in `commonMain` ([#20](https://github.com/Nitpick-org/nitpick-core/issues/20)).
3. **Air evidence**: screenshots, `AGENT_LOG.md`, `REVIEW_LOG.md`, comparison of two agents.
4. **No Air/Junie runtime integration** in Nitpick for the hackathon: out of scope,
   documented CLI fallback if needed.
5. **At least 2 platforms**: desktop + Android; the demo must show it.
6. **All 5 bonus features are targeted**, Kotlin backend included: a Ktor server that
   shares reviews, while the review itself stays local.

## 9. What to show the jury

- An app that runs on desktop **and** Android.
- The review loop: diff → inline nits → apply → commit.
- A review shared from the desktop by the Ktor backend, then opened on Android.
- What is shared (`commonMain`, Koog) vs platform-specific.
- Air screenshots with the agents side by side + one human correction.
- A multi-agent comparison (same task, two agents, kept result).
- A 3-minute demo, without slides.
