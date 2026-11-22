# Ticket 216 — Animated sync icons

- Status: done
- Human owner: @pepito2t (H3)
- Track: B desktop + C android (same component)
- Agent: Claude Code (Fable 5.1), human session
- Model: fable
- Allowed files: `ui/components/SyncIcon.kt`, `ui/phone/PhoneGraphScreen.kt`, `ui/repository/RepositoryScreen.kt`

## Done

- `SyncIcon(resource, motion, active, tint)`: still as long as `active` is false (no frame
  spent); otherwise an infinite transition: Fetch spins (900 ms, linear), Pull moves down and
  back up by 3 dp (450 ms, back and forth), Push the same upwards.
- Phone: the branch card wires `isFetching` / `isPulling` / `isPushing`; the active button
  stays at full opacity during the operation. Desktop: the three bar actions get the
  animated icon as `leading` (they had no icon).

## Not done

- No test (animation); not seen on screen: emulator closed.

## Log

- 03:10 — component, wiring, check green.
