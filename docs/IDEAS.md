# Out-of-scope ideas

> Good ideas that are not in the PRD. We re-read them at H12; otherwise they go into the
> "and after" pitch. **Agents may add an idea here, but never implement it
> without human validation.**

## Format

```md
## <title>

- **For**: <person / problem>
- **What it would do**: <one sentence>
- **Why it is interesting**: <novelty / impact>
- **Estimated cost**: <hours>
- **Verdict**: keep for the pitch / forget it
```

## Ideas

- …

## "Sign in with GitHub" on the phone (OAuth device flow)

- **For**: the mobile user, who today types an access token on the Connect screen.
- **What it would do**: a "Sign in with GitHub / GitLab" button opens the browser, the app gets the token through the device flow and stores it in settings; nothing left to copy-paste.
- **Why it is interesting**: same model as Air Teams (repo access goes through your forge account), with no manual input; a smoother demo.
- **Estimated cost**: 2 h (GitHub OAuth App + Ktor client device flow + screen), GitLab 1 h more.
- **Verdict**: after the demo; the typed token is enough for LauzHack.

## OpenAI-compatible endpoint in the settings (LM Studio, vLLM, LiteLLM)

- **For**: whoever runs a local model without Ollama; LM Studio does not emulate the Ollama API (#82).
- **What it would do**: a "Server URL" field under the OpenAI provider; empty = api.openai.com. The code is there (`KoogLlmClient(openAiBaseUrl)`); the `Settings` field and its desktop + phone control are missing.
- **Why it is interesting**: the "100% local review" demo works with any compatible server, not only Ollama.
- **Estimated cost**: 45 min (field, 2 screens, EN/FR strings, serialization test).
- **Verdict**: after the demo; the test hook is enough for #82.


## SSH remotes through JGit's Apache SSH module

- **For**: anyone whose clone uses `git@github.com:…`, which is what `gh repo clone` and most IDEs produce.
- **What it would do**: add `org.eclipse.jgit.ssh.apache` so fetch, pull, push and clone work over SSH with the user's keys and agent; today the app refuses an SSH remote with a sentence (#232).
- **Why it is interesting**: no HTTPS remote or token to set up for a repo you already have.
- **Estimated cost**: 1 h on desktop (host-key prompt, passphrase prompt, known_hosts), unknown on Android (dexing of the module, no ssh-agent).
- **Verdict**: after the demo; the demo repos are HTTPS clones.

## JetBrains AI (Junie) as a review provider

- **For**: a JetBrains user who wants the review to run through the account they already pay for, and the jury reading criterion 5 of the JetBrains brief (#227, reading B).
- **What it would do**: a fifth entry in the Settings provider list, "JetBrains AI (Junie)", greyed with a "coming" tooltip until a public API exists; never a fake call.
- **Why it is interesting**: shows the JetBrains ecosystem inside the product, not only in the process.
- **Estimated cost**: 20 min for the greyed entry (enum value, 2 strings EN/FR, disabled state in `NitpickRadioGroup`); unknown for a real integration, no public Junie API known (`docs/RESEARCH.md` has nothing).
- **Verdict**: parked; `docs/CONTEXT.md` puts a runtime Junie integration in WON'T, and a greyed entry in a 3-minute demo reads as unfinished. Reading A (a real Junie Lite ticket, `docs/demo/junie-lite-brief.md`) is the criterion as written.
