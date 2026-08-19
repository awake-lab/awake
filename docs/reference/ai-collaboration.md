# AI Collaboration

This document is the canonical source for how Awake organizes project guidance for agents.

## Purpose

Awake supports multiple assistants. To keep Claude, Codex, Gemini, and repo-local skills in
sync, the repository uses a three-layer model:

1. `docs/*` holds canonical project truth
2. agent entrypoints stay thin and point to canonical docs
3. `skills/*` provides repo-local execution guidance

## Ownership Model

| Surface | Role | What Belongs Here |
|---|---|---|
| `docs/architecture.md` and `docs/reference/*` | Canonical truth | Stable architecture, ownership rules, state categories, module boundaries, long-lived technical guidance |
| `AGENTS.md`, `CLAUDE.md`, `GEMINI.md`, `.claude/AGENTS.md` | Entry points | Bootstrap config, read-this-first links, short critical guardrails |
| `skills/awake/agents/*.md` | Repo-local role overlays | How an agent should approach ECS, engine, UI, or other Awake-specific work |
| `skills/awake/commands/*.md` | Repo-local commands | Operational workflows such as reviews, audits, and validation helpers |

## Decision Rule

- If a rule answers "how is Awake designed?", put it in `docs/*`.
- If a rule answers "how should an agent work on Awake?", put it in `skills/*`.
- If a rule is needed only so an assistant boots correctly, keep it short in an entrypoint
  file and point back to the canonical doc.

## Entry Points

Awake keeps multiple entrypoint files so different assistants can discover the same project:

- [AGENTS.md](/Users/ronvaldoz/StudioProjects/awaken/AGENTS.md)
- [CLAUDE.md](/Users/ronvaldoz/StudioProjects/awaken/CLAUDE.md)
- [GEMINI.md](/Users/ronvaldoz/StudioProjects/awaken/GEMINI.md)
- [.claude/AGENTS.md](/Users/ronvaldoz/StudioProjects/awaken/.claude/AGENTS.md)

Those files should stay small. They should:

- identify the canonical docs to read first
- identify the canonical skill location
- keep only a few critical guardrails that are worth duplicating at startup

They should not become the long-form home for architecture policy.

## Repo-Local Skills

Awake's tracked skill files live under:

- `skills/awake/agents/*.md`
- `skills/awake/commands/*.md`
- `skills/awake/templates/*.md`

These are the canonical repo-local skill sources. The matching `.claude/agents` and
`.claude/commands/awake` paths are symlinks into `skills/awake/`.

Rules:

- edit the tracked files under `skills/awake/`, not the symlinked `.claude/` paths
- keep workflow instructions in skills, not canonical architecture policy
- when a skill needs a project rule, link to the relevant `docs/*` page instead of copying
  the whole policy into the skill

See also:

- [docs/reference/agent-starter-pack.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/agent-starter-pack.md)
- [docs/reference/agent-routing.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/agent-routing.md)
- [docs/reference/engineering-change-summaries.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/engineering-change-summaries.md)
- [docs/reference/ui-testing-dictionary.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-testing-dictionary.md) — plain-English UI testing vocabulary

## Duplication Policy

Allowed duplication:

- a one-line reminder in an entrypoint file
- a one-line reminder in a skill doc that points to the canonical doc

Avoid:

- re-stating the same architecture rule in `AGENTS.md`, `.claude/AGENTS.md`, and multiple
  repo-local skills
- letting `skills/*` turn into parallel architecture docs

## Quality Gate Policy

Detekt is not optional for code pushes. The repository keeps a tracked
[.githooks/pre-push](/Users/ronvaldoz/StudioProjects/awaken/.githooks/pre-push) hook that runs
`./gradlew detekt` before pushing Kotlin, Gradle, Detekt config, build-logic, or workflow
changes.

Local setup:

```bash
git config core.hooksPath .githooks
```

Rules:

- run Detekt before pushing code changes, even when a narrower test suite passed
- do not treat existing Detekt debt as permission to add more debt
- use `AWAKE_SKIP_DETEKT_HOOK=1` only for an explicit, reviewed emergency bypass
- when Detekt is already red, either fix the touched-module findings or deliberately
  re-baseline existing debt in a separate debt-tracking change before relying on the hook
- keep broad legacy cleanup separate from feature/fix commits

## Model Selection Rule

Repo-local agent files keep a `model:` frontmatter field containing the active provider model ID (e.g. `claude-opus-5`, `claude-sonnet-5`) so that runner tooling (Claude Code agent dispatch) can resolve an executable model.

Rules:

- use the provider mapping in
  [docs/reference/agent-catalog.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/agent-catalog.md) to choose the appropriate model ID for the agent's capability tier (`flagship-coding`, `balanced-coding`, or `fast-utility`)
- update the mapping table in the catalog when a provider ships a new model generation

## Read Order

For most Awake work:

1. [docs/architecture.md](/Users/ronvaldoz/StudioProjects/awaken/docs/architecture.md)
2. [docs/reference/ai-collaboration.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ai-collaboration.md)
3. [docs/reference/agent-catalog.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/agent-catalog.md)
4. [docs/reference/ui-ownership.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-ownership.md)
5. [docs/reference/ui-validation.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-validation.md)
6. [docs/reference/game-structure.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/game-structure.md)
7. [docs/MVP_PLAN.md](/Users/ronvaldoz/StudioProjects/awaken/docs/MVP_PLAN.md)
8. [docs/tasks.md](/Users/ronvaldoz/StudioProjects/awaken/docs/tasks.md)
9. the relevant `skills/awake/agents/*.md` file for the task
