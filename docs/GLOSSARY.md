# Glossary — the team's language

> An ambiguous term costs agent-hours. As soon as a word can mean two things,
> we write it here. Agents use this vocabulary in the code, the tickets and the tests.

## Rule

- One term = one definition = one consistent variable/file name.
- If a term changes, update this page and `docs/DECISIONS.md`.
- Do not invent synonyms in the code.

## Terms

| Term | Definition | Not to be confused with |
| --- | --- | --- |
| **demo** | The 3-minute walkthrough described in `docs/DEMO.md` | the complete product |
| **aha** | The visible moment when the jury understands the value | the technical feature |
| **ticket** | A vertical slice, a visible behaviour | a technical task |
| **module** | A `src/modules/<nom>/` folder with a public interface | a file |
| **mock** | A simulated implementation, clearly acknowledged as such | fake data no one mentions |
| **demo-critical** | If it breaks, the demo is dead | important but optional |
| **novelty** | What did not exist before, in one sentence | the complexity |
| **sponsor challenge** | A sponsor's challenge, with its criteria | the overall ranking |

<!-- Add the project terms at H0: product name, actors, domain objects. -->
