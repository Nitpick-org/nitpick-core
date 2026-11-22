# Junie brief: branch with planted defects in `pocket-ledger` (#44)

To be pasted into Air, in a clone of `pocket-ledger` checked out on `main`. Junie produces the
branch that Nitpick reviews during the demo (points 3 and 4 of the script). The Air session also
doubles as the capture for #39.

## The prompt

> Create the branch `feature/search-pages-import` from `main` and add three commands to the
> `ledger` CLI, **one commit per command**, with realistic conventional messages.
> This is a demo repo for a code review tool: each commit contains
> **deliberately** the defect described below. Write it like a developer in a hurry
> would write it, without a comment flagging it, and without fixing it.
>
> 1. `ledger list --page <n>`: pages of 10 expenses, numbered from 1, with
>    `LIMIT ? OFFSET ?` in `ExpenseStore.page(page: Int)`. **Defect**: the offset equals
>    `page * PAGE_SIZE`, so page 1 skips the first 10 expenses (off-by-one).
>    Commit: `feat: paginate the expense list`.
> 2. `ledger search <text>`: the expenses whose merchant contains the text, in
>    `ExpenseStore.search(text: String)`. **Defect**: the query is built by
>    concatenation, `"... WHERE merchant LIKE '%$text%'"`, with `createStatement()`
>    (SQL injection). Commit: `feat: search expenses by merchant`.
> 3. `ledger import <file.csv>`: imports a bank export `date,merchant,category,amount`
>    (header ignored) with `parseCents` for the amounts. **Defect**:
>    `File(path).bufferedReader()` is never closed, no `use {}` (resource not
>    closed). Commit: `feat: import expenses from a bank CSV export`.
>
> The rest of the code is clean: same conventions as `ExpenseStore` (prepared queries
> everywhere else), README updated with the three commands. The existing tests
> stay green; do not add a test for these three commands.
> No secret, no token, no `.db` file committed.

## What the human checks before pushing

- 3 commits on the branch, one defect per commit, no comment that gives it away.
- `./gradlew test` green (tests of `main` only); `git ls-files` shows neither `.db` nor `.env`.
- Nitpick on the branch against `main` finds **at least the off-by-one**, with an applicable
  suggestion (`(page - 1) * PAGE_SIZE`). Otherwise we adjust the code, not the review prompt.

## Fallback without Air

Same prompt in Claude Code, noted in `docs/AGENT_LOG.md` (`CONTEXT.md` §11).
