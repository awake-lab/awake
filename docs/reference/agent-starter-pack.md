# Agent Starter Pack

This page turns Awake's cross-agent setup into a reusable downstream starter pack.

Use it when you want to add multi-assistant support to another Kotlin Multiplatform repo
without rediscovering the same layout decisions.

## Goal

Keep the repo readable for humans and reliable for assistants:

- `docs/*` stays the source of truth
- entrypoint files stay small
- repo-local skills stay execution-focused
- provider model choices stay swappable

## Recommended Layout

```text
docs/
├── architecture.md
├── reference/
│   ├── ai-collaboration.md
│   ├── agent-catalog.md
│   └── <domain-rules>.md
skills/
└── <repo>/
    ├── agents/
    ├── commands/
    └── templates/
AGENTS.md
CLAUDE.md
GEMINI.md
.claude/AGENTS.md
```

Use that structure like this:

- `docs/architecture.md`
  - stable architecture, module boundaries, and long-lived technical rules
- `docs/reference/ai-collaboration.md`
  - cross-agent rules: what lives in `docs/*`, entrypoints, and `skills/*`
- `docs/reference/agent-catalog.md`
  - repo-local agent roster, ownership map, and model-tier mapping
- `docs/reference/<domain-rules>.md`
  - canonical placement rules for areas such as UI, state, or folder structure
- `skills/<repo>/agents/*.md`
  - repo-local execution guidance for named roles
- `skills/<repo>/commands/*.md`
  - repo-local operational commands and review workflows
- `skills/<repo>/templates/*.md`
  - starter templates for new repo-local agents or command docs

## Minimal Entrypoints

Keep all assistant entrypoint files thin. They should point to canonical docs and carry only
critical startup guardrails.

### `AGENTS.md`

```md
# AGENTS.md — <Project>

Use this file as a startup index, not as the long-form home for project policy.

## Read First

- `docs/architecture.md`
- `docs/reference/ai-collaboration.md`
- `docs/reference/agent-catalog.md`
- `docs/reference/<domain-rules>.md`
- `docs/tasks.md`

## Critical Guardrails

- keep generated code generated
- keep reusable rules in canonical docs, not samples
- keep repo-local workflow guidance in `skills/<repo>/`
```

### `CLAUDE.md`

```md
### Claude Code Project Profile

### Load skills context on initialization
--system-prompt-file=".claude/AGENTS.md"

### Read first
- `docs/architecture.md`
- `docs/reference/ai-collaboration.md`
- `docs/reference/agent-catalog.md`
- `docs/reference/<domain-rules>.md`
```

### `GEMINI.md`

```md
### Gemini Project Profile

### Canonical docs
- `docs/architecture.md`
- `docs/reference/ai-collaboration.md`
- `docs/reference/agent-catalog.md`
- `docs/reference/<domain-rules>.md`

### Repo-local skills
- Canonical repo-local skill docs live in `skills/<repo>/`
```

## Canonical Docs Checklist

### `docs/reference/ai-collaboration.md`

Make this the durable explanation of how the repo handles multiple assistants.

Suggested sections:

1. purpose
2. ownership model
3. decision rule: `docs/*` vs entrypoints vs `skills/*`
4. duplication policy
5. model-selection rule
6. read order

### `docs/reference/agent-catalog.md`

Make this the durable explanation of repo-local agent structure.

Suggested sections:

1. purpose
2. naming standard
3. model tiers
4. provider mapping
5. active agents
6. responsibility boundaries
7. agent file contract
8. current file map

## Model Tier Convention

If your tooling still expects a `model:` field in agent frontmatter, use capability tiers
instead of a provider-specific lock-in:

- `flagship-coding`
- `balanced-coding`
- `fast-utility`

Keep the provider-specific mapping in one canonical doc such as
`docs/reference/agent-catalog.md`.

That lets Codex, Claude, Gemini, and future runners resolve models without rewriting every
repo-local agent file.

## Repo-Local Agent Template

Awake keeps a reusable sample here:

- [skills/awake/templates/awake-domain-engineer.template.md](/Users/ronvaldoz/StudioProjects/awaken/skills/awake/templates/awake-domain-engineer.template.md)

For most repos, one good template is enough as long as the catalog explains ownership
boundaries clearly.

## Adoption Order

Use this rollout order:

1. write the canonical docs
2. slim the entrypoints down to pointers plus critical guardrails
3. create the agent catalog
4. create one or two repo-local agents for the highest-risk domains
5. add templates only after the conventions are stable enough to copy

That order avoids baking bad structure into every new agent file.
