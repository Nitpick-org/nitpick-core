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
