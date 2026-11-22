# Architecture — deep modules and parallel tickets

> Goal: 3 humans and 6 agents can work without stepping on each other's toes, and one
> human can explain the core to the jury in 2 sentences.

## Principles

1. **Deep modules**: a lot of behaviour behind a simple interface.
   Not 30 small interconnected files.
2. **5 to 8 modules maximum.** One module = one nameable responsibility.
3. **`src/modules/<nom>/index.ts` is the only public interface.** Nobody imports
   `src/modules/<nom>/internal/**` from outside.
4. **Tests lock behaviour at the interface**, not the implementation.
5. **A dependency goes one way.** No cycle between modules.
6. **The UI composes the modules**, it does not contain business logic.

## Target tree

```text
src/
  modules/
    <module>/
      index.ts          # public interface: types + exported functions/classes
      <module>.test.ts  # interface tests
      internal/         # private implementation (never imported from outside)
  app/                  # routes / pages / UI composition
  lib/                  # shared infra: env, db, http, llm
  components/           # reusable UI, no business logic
```

## Interface contract

```ts
// src/modules/example/index.ts
export type ExampleInput = { /* ... */ };
export type ExampleResult = { /* ... */ };

export interface ExampleModule {
  run(input: ExampleInput): Promise<ExampleResult>;
}

export function createExampleModule(deps: {
  db: Database;
  llm: LlmClient;
}): ExampleModule;
```

- The interface is **small**: 1 to 3 public functions.
- External dependencies are injected, not imported hard-coded.
- Errors are explicit (`Result` or typed exceptions), not `any`.
- A mock of the interface must be enough to test the UI.

## Module map (to fill in at H0)

| Module | Responsibility (1 sentence) | Interface (functions) | Owner | Demo-critical |
| --- | --- | --- | --- | --- |
| `capture` | Takes a user input | `start()`, `stop()` | H3 | yes |
| `analysis` | Turns the input into a result | `analyze(input)` | H2 | yes |
| `render` | Displays the result | `render(result)` | H3 | yes |
| `persistence` | Saves/replays a demo | `save()`, `load()` | H2 | no |
| `sponsor` | Wraps the sponsor API | `call()` + mock | H2 | yes |
| `demo` | Seed, reset, mock mode | `seed()`, `reset()` | H1 | yes |

Rules:

- 5 to 8 rows. If you have 12, merge them.
- A module can be **mocked** without changing the UI.
- The `demo` module is a real module: it is the one that saves the demo.

## Tickets as vertical slices

A ticket = **one visible behaviour from end to end**, not a technical layer.

```md
## #12 — The user sees the result in < 2 s

**Visible behaviour**: ...
**Acceptance criteria**:
- [ ] ...
- [ ] ...
**Allowed modules / files**: src/modules/analysis/**, src/app/result/**
**Dependencies**: #10
**Risk**: high / medium / low
**Demo-critical**: yes / no
**Owner**: H2
**Agent brief**: ...
```

### Split

- First the **demo path** (the aha).
- Then the **edge cases** that threaten the demo (API down, empty data).
- Finally the polish.

### Do not split

- "Do the API" then "do the UI": those are layers, not slices.
- "Refactor the module" with no visible behaviour: only do it if it unblocks
  a demo ticket.
- "Add a feature" that is not in the PRD: `docs/IDEAS.md`.

## Collision matrix (to fill in at H0)

| Ticket | `capture` | `analysis` | `render` | `sponsor` | `demo` |
| --- | --- | --- | --- | --- | --- |
| #10 skeleton | x |  | x |  | x |
| #11 aha |  | x | x | x |  |
| #12 error case |  | x | x |  | x |

Two tickets that share a cell **do not run in parallel**. They follow each other, or
one of the two is split.

## Anti-patterns

| Anti-pattern | Symptom | Correction |
| --- | --- | --- |
| God module | a 1500-line module | split by responsibility |
| `utils/` catch-all | everyone imports everything | `lib/` by domain, explicit interfaces |
| Internal import | `import ... from '../analysis/internal'` | go through `index.ts` |
| Cycle | A imports B, B imports A | extract a module or invert the dependency |
| UI that computes | business logic in a component | move it into the module |
| Implementation tests | tests that break on every refactor | test the interface |
| Module per screen | 10 UI modules | modules per business capability |

## What the jury must hear

In 30 seconds:

> "We have <N> modules. The core is <module>. It takes <input> and produces <output>.
> The rest (UI, storage, sponsor) is around it. We can replace it with a mock for the demo."

If you cannot say that, the architecture is too complicated.

## Sources

- [AI Hero — How To Make Codebases AI Agents Love](https://www.aihero.dev/how-to-make-codebases-ai-agents-love): deep modules, grey box, tests.
- [A Philosophy of Software Design](https://web.stanford.edu/~ouster/cgi-bin/book.php) — John Ousterhout: deep modules.
