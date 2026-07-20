---
name: awake
description: >
  Repo-local skill bundle for the Awake KMP game engine. Contains role-specific
  execution guidance for working in Awake domains (ECS, scene runtime, UI systems,
  render backends, platform integration, developer experience, architecture audits)
  under agents/, repo-local operational commands (audits, review helpers) under
  commands/, and starter templates for new repo-local agent docs under templates/.
  Use this skill when working on Awake engine internals to find the right
  domain-specific agent persona or command instead of improvising one.
---

# Awake Repo-Local Skills

See [README.md](README.md) for the full breakdown of what lives here, what doesn't,
agent naming conventions, and the agent model-tier field.

- `agents/*.md` — role-specific execution guidance for Awake domains (ECS, scene
  runtime, UI systems, render backends, platform integration, developer experience,
  architecture audits)
- `commands/*.md` — repo-local operational commands (audits, review helpers)
- `templates/*.md` — starter templates for new repo-local agent docs

Canonical architecture policy and module ownership rules live in `docs/*`, not here —
this is execution guidance, not the source of truth. `.claude/agents` and
`.claude/commands/awake` are symlinks into this folder; edit the tracked files here.
