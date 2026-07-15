### Gemini Project Profile

### Canonical docs
- `docs/architecture.md`
- `docs/reference/ai-collaboration.md`
- `docs/reference/ui-ownership.md`
- `docs/reference/game-structure.md`
- `docs/MVP_PLAN.md`
- `docs/tasks.md`

### Repo-local skills
- Canonical repo-local skill docs live in `skills/awake/`
- `.claude/agents` and `.claude/commands/awake` are symlinks into `skills/awake/`

### Critical guardrails
- Do not hand-edit generated JNI Accessor/Mutator files; regenerate them.
- Keep reusable UI rules in `docs/reference/ui-ownership.md`, and game state/folder rules in `docs/reference/game-structure.md`, not in sample modules.
- Treat `docs/*` as the source of truth and `skills/*` as execution guidance.
