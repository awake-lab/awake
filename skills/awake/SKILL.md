---
name: awake
description: >
  Repo-local skill bundle for the Awake KMP game engine and game studio. Contains role-specific
  execution guidance across the Engine Framework Suite (core, render, UI, runtime, platform, auditor) and
  the Game Studio Creative Suite (producer, game designer, narrative, camera, art/VFX, audio) under agents/,
  operational commands under commands/, and starter templates under templates/. Use this skill to locate
  the right domain-specific agent persona or command for Awake tasks.
---

# Awake Repo-Local Skills

See [README.md](README.md) for the full breakdown of what lives here, what doesn't,
agent naming conventions, and the agent model-tier field.

- `agents/*.md` — role-specific execution guidance for Awake's Engine Framework Suite and Game Studio Creative Suite
- `commands/*.md` — repo-local operational commands (audits, review helpers)
- `templates/*.md` — starter templates for new repo-local agent docs

Canonical architecture policy and module ownership rules live in `docs/*`, not here —
this is execution guidance, not the source of truth. `.claude/agents` and
`.claude/commands/awake` are symlinks into this folder; edit the tracked files here.
