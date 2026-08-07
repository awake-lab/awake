### Claude Code Project Profile

### Load skills context on initialization
--system-prompt-file=".claude/AGENTS.md"

### Default flags
--compact
--verbose=false

### Ignore generated and vendor directories
--ignore="**/build/**"
--ignore="**/.gradle/**"
--ignore="**/vendor/**"
--ignore="**/third_party/**"

### Read first
- `docs/architecture.md`
- `docs/reference/ai-collaboration.md`
- `docs/reference/agent-catalog.md`
- `docs/reference/ui-ownership.md`
- `docs/reference/ui-validation.md`
- `docs/reference/game-structure.md`
- `docs/MVP_PLAN.md`

### Read before writing engine code
Mandatory for the domain you are touching — each encodes a bug this repo actually shipped:
- `skills/awake-core-math/SKILL.md` — before any `Vec3`/`Mat4`/camera math, or any code inside
  a `System.update`. Covers the mutating-vs-allocating naming contract (`normalize()` mutates,
  `normalized()` allocates), per-frame allocation rules, and the shared camera-basis rule.
- `skills/awake-ecs-authoring/SKILL.md` — before adding a component, writing a `System`, or
  building entities with the `scene { }` DSL. Covers `Poolable.reset()` completeness, why
  reflective component construction breaks on iOS/wasmJs, structural-change churn, entity
  ownership on teardown, and `@DslMarker` on nested builders.
- `skills/awake-ecs-scene-runtime/SKILL.md` — consuming the scene runtime from a sample/demo.
