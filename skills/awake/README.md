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
- `docs/reference/agent-catalog.md`
- `docs/reference/ui-ownership.md`
- `docs/reference/game-structure.md`

## Agent Naming

Repo-local agent files must follow the naming standard documented in
`docs/reference/agent-catalog.md`:

- `awake-<domain>-<role>.md`
- professional role suffixes only, such as `engineer` or `auditor`
- no informal names such as `*-dev`

## Agent Model Field

Repo-local agent frontmatter keeps a `model:` field for tool compatibility, but Awake uses
that field for capability tiers:

- `flagship-coding`
- `balanced-coding`
- `fast-utility`

Resolve those tiers to provider models through
`docs/reference/agent-catalog.md` instead of pinning every repo-local agent to a single
vendor model family.

## Working Rule

- `docs/*` is the source of truth
- `skills/*` is execution guidance
- `.claude/agents` and `.claude/commands/awake` are symlinks into this folder

Edit the tracked files here, not the symlinked `.claude/` paths.
