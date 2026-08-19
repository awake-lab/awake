# Awake Repo-Local Skills

This folder contains Awake's tracked repo-local skill and agent files.

## What Lives Here

- `agents/*.md`
  - Dual-suite agent definitions: **Engine Framework Suite** (core, render, UI, runtime, platform, auditor) and **Game Studio Creative Suite** (producer, game designer, narrative, camera, art/VFX, audio)
- `commands/*.md`
  - Repo-local operational commands such as audits, review helpers, and semantic UI crop/diff workflows
- `templates/*.md`
  - Reusable starter templates for new repo-local agent docs

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
- professional role suffixes only (`engineer`, `auditor`, `director`, `designer`, `producer`)
- no informal names such as `*-dev`

## Agent Model Field

Repo-local agent frontmatter maintains active provider model IDs (e.g. `claude-opus-5`, `claude-sonnet-5`) required by runner tooling (Claude Code dispatch), corresponding to Awake's capability tiers (`flagship-coding`, `balanced-coding`, `fast-utility`).
See `docs/reference/agent-catalog.md` for provider mappings.

## Working Rule

- `docs/*` is the source of truth
- `skills/*` is execution guidance
- `.claude/agents` and `.claude/commands/awake` are symlinks into this folder

Edit the tracked files here, not the symlinked `.claude/` paths.
