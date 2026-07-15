### Codex Project Profile

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

### UI ownership rules
- Reusable UI templates must not live in sample modules.
- Put style-agnostic UI composition templates in `awake:engine:ui`.
- Keep foundational building blocks in `awake:engine:ui-core` and `awake:engine:ui-widgets`.
- Put branded or design-system-specific recipes in `awake:engine:ui-designsystem`.
- Keep scene-, ECS-, and sample-specific adapters out of reusable UI modules.
- Treat `Panel`, `Section`, `PropertyList`, and `PropertyRow` as reusable primitives; treat
  `InspectorPane` as a custom composition built from them, not as a foundational primitive.
