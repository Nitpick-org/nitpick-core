# Ticket 122 — Phone UI/UX pass

- Status: done, to merge
- Human owner: @pepito2t (H3)
- Track: C android + polish
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Allowed files: `ui/phone/*`, `ui/components/NitBottomSheet.kt`, `ui/repository/CommitGraph.kt`
  (`compactTime` parameter), `ui/repository/CommitTimeFormat*.kt`, `composeResources/drawable/ic_github.xml`,
  `ic_gitlab.xml`

## Done

- Nit bottom sheet: the body scrolls, Dismiss / Apply stay under the thumb (on a long
  suggestion they went off screen, seen during dogfooding).
- Commit lines: compact date on the phone (`HH:mm` today, `d MMM` this year,
  `MMM yyyy` before); the full date ate the subject at 393 dp.
- Top bar: actions as 44 dp icons (Settings gear, Shared cloud) instead of text;
  title + subtitle on Review ("Review" / "branch vs main") instead of a truncated title.
- Icons on the buttons: fetch / pull / push on the branch card, pin on "Nitpick
  branch", check on "Stage & commit", push on "Push", cloud on "Clone repo".
- GitHub / GitLab marks (Simple Icons, CC0, as vector XML) on the forge choice, the
  "Sign in with…" button and next to `@login` in the Repositories bar.
- Graph: the sync status line has a separator and some air, it no longer sticks to the list.
- Repo card: the internal path `/data/user/0/…` is removed, name + chevron.
- Review summary in `body` instead of `section` (4 bold lines were heavy).
- Feel: slide between screens (220 ms, forward/back like a native stack), "Up to date"
  in green instead of "↑0 ↓0", haptic feedback on Apply, a "Clean branch" screen with a
  check that pops when the review finds nothing, and the same loading card as desktop
  (AI badge, "Reviewing…", animated pin, phrases).

## Not done

- "merge" icon: no merge action in the app, nothing to illustrate.
- Repo card with branch, last review and badge (handoff §4.1): it would need storing more
  than a path in `Settings.recentRepositories` (contract), separately.
- Settings and phone Diff not reviewed.

## Evidence

- `check.sh` green (UI only, no test added: no Compose test infra).
- Emulator: Repos, Connect, Graph (compact dates, "Up to date"), Review (title + subtitle), sheet
  full height with pinned actions, transition captured mid-course; commit `93089d1` pushed
  from the phone. Not seen: the "Clean branch" screen (the model always found a nit).

## Log

- 01:20 — ticket, worktree, re-reading of the screens and of handoff §4.
- 01:40 — changes, check green.
- 01:30 — emulator captures, full-height sheet, transitions, desktop loader reused.
