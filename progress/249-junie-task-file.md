# Ticket 249-junie-task-file

- Status: done
- Human owner: @H2
- Track: A core (review)
- Agent: Claude Code (Fable 5.1, interactive session)
- Allowed files: `shared/src/jvmMain/kotlin/dev/nitpick/app/review/JunieCliLlmClient.kt`,
  `shared/src/jvmTest/kotlin/dev/nitpick/app/review/JunieCliLlmClientTest.kt`

## Done

- The review prompt is written to `review-task-<id>.md` in Junie's empty temp project; the
  `--task` argument only tells Junie to read it and answer with the JSON it asks for. The file is
  deleted after the run, like the answer file.
- The 100 000-character guard from #243 is gone with the reason for it.

## Decisions

- Probe before coding: same prompt, same model (`junie-lite`), prompt read from the file: 14 s,
  same nits, no errors. The 231k-token review of `medlinx` that the guard refused goes through.
- Side effect kept on purpose: the diff no longer sits in the process arguments, so `ps` does not
  show it any more (limit noted in `progress/240-provider-icons.md`, now closed).

## Uncertain

- One extra model step (the file read) per review: 5 s more on the probe.

## Journal

- 22/11 — seen by H2 on `medlinx` as "Could not reach the provider" with no Junie session
  started; fixed, 7 tests ran.
