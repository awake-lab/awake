# Awake Repo-Local Skills

This folder contains Awake's tracked repo-local skill files.

## What Lives Here

- `agents/*.md`
  - role-specific execution guidance for working in Awake domains such as ECS and engine work
- `commands/*.md`
  - repo-local operational commands such as audits and review helpers

## What Does Not Live Here

- canonical architecture policy
- stable module ownership rules
- long-form project technical guidance

Those belong in:

- `docs/architecture.md`
- `docs/reference/ai-collaboration.md`
- `docs/reference/ui-ownership.md`

## Working Rule

- `docs/*` is the source of truth
- `skills/*` is execution guidance
- `.claude/agents` and `.claude/commands/awake` are symlinks into this folder

Edit the tracked files here, not the symlinked `.claude/` paths.
