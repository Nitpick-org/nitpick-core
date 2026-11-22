# Docs index

Nitpick was built in 24 hours at LauzHack 2026. The repo keeps both the product docs and
the process evidence, so this page says what to read first and what is only history.

Everything is written in English: code, comments, commits and docs. The French left is
intentional — the app's two locales (the tagline and button strings, the EN/FR pairs in the
UI handoff), verbatim quotes of French log lines, the sponsor name and one audio file name.

## Start here

| Doc | Why |
| --- | --- |
| [../README.md](../README.md) | What Nitpick is, how to run it, what was reused |
| [JETBRAINS-CHALLENGE.md](JETBRAINS-CHALLENGE.md) | The JetBrains KMP brief and how Nitpick maps to it |
| [CONTEXT.md](CONTEXT.md) | Master brief: mission, stack, scope, hour-by-hour plan (French) |
| [DEMO.md](DEMO.md) | The 3-minute demo script and the replay captures in [demo/](demo/) |
| [HANDOFF.md](HANDOFF.md) | Last handoff: what changed, what is open, review checklist |

## Design

| Doc | Why |
| --- | --- |
| [DESIGN-SYSTEM.md](DESIGN-SYSTEM.md) | JetBrains New UI tokens and component rules |
| [design/UI-HANDOFF.md](design/UI-HANDOFF.md) | Screen-by-screen UI handoff |
| [design/FONT-LICENSES.md](design/FONT-LICENSES.md) | Font licences bundled with the app |
| [design/icon/](design/icon/), [design/mockups/](design/mockups/) | Icons, loaders and the HTML mockups |

## Process evidence

| Doc | Why |
| --- | --- |
| [AGENT_LOG.md](AGENT_LOG.md) | Compiled agent journal, one row per agent and ticket |
| [REVIEW_LOG.md](REVIEW_LOG.md) | Human rejections and corrections of agent work |
| [../progress/](../progress/) | One journal per ticket, written by the agent working on it |
| [AGENT-OPS.md](AGENT-OPS.md), [MODEL-ROUTING.md](MODEL-ROUTING.md), [ULTRA.md](ULTRA.md) | How agents were routed, budgeted and supervised |
| [PLAYBOOK.md](PLAYBOOK.md) | Rituals, syncs and sleep shifts |
| [audits/](audits/) | Read-only audits: architecture, security, release, spec, UX, demo path |
| [audits/audit-2026-11-22.md](audits/audit-2026-11-22.md) | The synthesis the team worked from on the last day |
| [PERFORMANCE-AUDIT.md](PERFORMANCE-AUDIT.md) | Hot-path audit and the fixes it produced |

## Reference

| Doc | Why |
| --- | --- |
| [GLOSSARY.md](GLOSSARY.md) | The team's vocabulary |
| [DECISIONS.md](DECISIONS.md) | Durable product and architecture decisions |
| [QUESTIONS.md](QUESTIONS.md) | Agent questions answered by a human, append-only |
| [IDEAS.md](IDEAS.md) | Out-of-scope ideas, kept for later |
| [RESEARCH.md](RESEARCH.md) | Sourced research cache (Koog, JGit, Compose, providers) |
| [LAUZHACK.md](LAUZHACK.md) | Event rules, schedule and submission requirements |
| [transcripts/](transcripts/) | Transcript of the JetBrains sponsor talk |

## Historical, before H0

These describe the pre-event base and the abandoned TypeScript plan. They are kept for an
honest record; do not apply them. [CONTEXT.md](CONTEXT.md) supersedes them.

| Doc | What it was |
| --- | --- |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Module sketch of the first plan (its deep-module principles still hold) |
| [PRD.md](PRD.md) | Product requirements, replaced by `CONTEXT.md` |
| [STACK.md](STACK.md) | The TypeScript stack that was never built |
| [DRY-RUN.md](DRY-RUN.md) | The pre-event rehearsal protocol |
| [ONBOARDING.md](ONBOARDING.md) | Setup for the TypeScript toolchain |
| [PRE-EVENT-CHECKLIST.md](PRE-EVENT-CHECKLIST.md) | The pre-event readiness list |
| [AUDIT-2.md](AUDIT-2.md), [AUDIT-2026-11-16.md](AUDIT-2026-11-16.md) | Audits of the pre-event base |
| [RESEARCH-HACKATHON.md](RESEARCH-HACKATHON.md), [STRATEGY.md](STRATEGY.md) | Pre-event research and pitch strategy |
| [../dry-run/](../dry-run/) | Notes from the rehearsal; its TypeScript starter was removed after the freeze |
