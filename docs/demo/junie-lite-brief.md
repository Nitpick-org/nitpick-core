# Junie Lite brief: one real ticket in Air (#227)

The JetBrains brief scores the use of JetBrains AI / Junie (`docs/RESEARCH.md`, criterion 5).
Nothing in the repo shows it yet, and #169 forbids claiming it without a capture. This brief
is what a human runs in Air with Junie Lite (free with a JetBrains account, Q-021) on a clone of
this repo, and what to keep as proof. Budget: 30–45 min including captures.

## Pick the ticket

Two small candidates, both deterministic (`check.sh` is the oracle). Pick one, note it in the
Air session title.

| Candidate | What Junie writes | Visible result | Why it fits |
| --- | --- | --- | --- |
| **A. Provider hints on desktop** (recommended) | `SettingsDialog.kt`: pass the same `subtitle` hints the Android screen already passes to `NitpickRadioGroup` (`provider_cloud_hint` / `provider_ollama_hint`, see `SettingsScreen.kt`). | The desktop Settings dialog shows "cloud, key needed" / "local, no key" under each provider mark. | 1 file, strings already exist in EN and FR, a UX polish the jury sees. |
| **B. Default model test** | `shared/src/commonTest/.../settings/LlmModelsTest.kt`: every `LlmProvider` has at least one known model and the selector falls back to the first one. | A test that ran, in the jvmTest report. | 1 new file, no UI, cannot break the demo. |

## The prompt (paste into Junie)

> Read `AGENTS.md` and `docs/CONTEXT.md` §6. Branch `ticket/<n>-junie-<a|b>` from `main`.
> Task: <paste the row above>. Rules: logic stays in `commonMain`; every UI string goes through
> `composeResources` in English and French; no new dependency; keep the existing component
> API unless you explain why. Run `./gradlew :shared:jvmTest :desktopApp:assemble` and show the
> test count. One conventional commit in English whose body explains the why to a Kotlin
> beginner. Append your journal to `progress/<n>-junie-<a|b>.md` (agent, model, done, decisions,
> uncertain).

## What the human does

1. Open an issue first (`gh issue create`, label `ticket`), so the branch has a number.
2. Run the prompt in Air with **Junie Lite**. Do not fix its output by hand before the capture.
3. Captures to `docs/demo/`: `junie-01-prompt.png` (Air, Junie, the prompt), `junie-02-diff.png`
   (the diff Junie proposes), `junie-03-check.png` (the green test count or the desktop dialog).
4. Review it like any agent PR: reject or correct at least one point, and write that line in
   `docs/REVIEW_LOG.md` (human-only file) naming "Junie Lite via Air". Add the matching row to
   `docs/AGENT_LOG.md`.
5. Merge through a PR like every ticket.

## Then paste these (only once the captures exist)

- `README.md`, section "How we build it", after the "Agents do real work" bullet:
  > - **Several agents, one process**: most tickets were driven with Claude Code and its
  >   subagents; ticket #<n> was driven with Junie Lite in JetBrains Air (captures in
  >   `docs/demo/junie-*.png`), reviewed and merged the same way.
- `docs/DEMO.md`, Q&A, answer to "What did you reuse?":
  > Kotlin Multiplatform, Compose, JGit, Koog and Ktor are reused; the review prompt, the
  > anchoring, the apply loop and the share server are ours. The code was written with Claude
  > Code and, for #<n>, Junie Lite in Air; every agent diff was reviewed by a human.

## Fallback

No Air on the laptop, or Junie Lite refuses the account: say so in `docs/DEMO.md` and keep the
"Claude Code + subagents" line from #169. A sentence that is true beats a bonus we cannot show.
