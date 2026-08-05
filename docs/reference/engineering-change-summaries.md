# Engineering Change Summaries

Use this format when handing off non-trivial Awake changes: renderer fixes, ECS/runtime
changes, UI behavior shifts, performance work, platform fixes, or anything with meaningful
risk. The goal is to make the change understandable six months later without rereading the
whole diff.

## Standard Handoff Format

`````md
## Outcome

One or two sentences explaining what changed and why it matters.

## Before / After

```kotlin
// before
oldShape()
```

```kotlin
// after
newShape()
```

## Benefits

- Correctness, stability, or UX improvement.
- Performance impact, if any.
- Risk removed or future work unlocked.

## Validation

- Exact tests, compile tasks, or manual runtime checks run.
- Known failures that remain, clearly marked as existing debt when true.

## Next

The most useful follow-up and why it should happen next.
`````

Keep snippets short. They should show the shape of the change, not replace the diff.

## Changelog / Release Note Derivative

Turn the handoff into a shorter reader-facing entry:

```md
### Changed

- Outcome-focused behavior or architecture change.

### Fixed

- User-visible bug, crash, validation error, or correctness issue resolved.

### Validation

- High-signal proof only; avoid dumping every local command if the audience is not internal.

### Known Issues

- Remaining limitation, only when it affects users or the next maintainer.
```

Use code snippets in release notes only when the audience is developers and the snippet makes
the migration or behavior change clearer.

## Audience Rule

| Audience | Detail Level |
|---|---|
| Commit or PR handoff | Full format, including before/after snippets and validation |
| Internal changelog | Compact changed/fixed/validation/known-issues bullets |
| Public release notes | User-facing impact first; code snippets only for developer APIs |

## Quality Bar

- Lead with the outcome, not the process.
- Name tradeoffs honestly when a fix keeps a known limitation.
- Separate current validation from recommended follow-up.
- Never hide a failing gate inside a success summary.
- Do not include unrelated dirty worktree files in the change summary.
