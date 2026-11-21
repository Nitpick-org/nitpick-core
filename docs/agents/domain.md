# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Before exploring, read these

- **`docs/GLOSSARY.md`**: the shared vocabulary (single-context repo).
- **`docs/DECISIONS.md`**: the decision log (plays the role of ADRs). Read the entries that touch the area you're about to work in.

`docs/DECISIONS.md` is owned by H2: agents propose a decision in `docs/QUESTIONS.md`, they don't edit it. If a term or decision is missing, **proceed silently**; `/domain-modeling` fills gaps when they actually get resolved.

## Use the glossary's vocabulary

When your output names a domain concept (in an issue title, a refactor proposal, a hypothesis, a test name), use the term as defined in `docs/GLOSSARY.md`. Don't drift to synonyms the glossary explicitly avoids.

If the concept you need isn't in the glossary yet, that's a signal: either you're inventing language the project doesn't use (reconsider) or there's a real gap (note it for `/domain-modeling`).

## Flag decision conflicts

If your output contradicts an entry in `docs/DECISIONS.md`, surface it explicitly rather than silently overriding:

> _Contradicts decision <date/title>, but worth reopening because…_
