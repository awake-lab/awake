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

### Read first
- `docs/architecture.md`
- `docs/reference/ai-collaboration.md`
- `docs/reference/agent-catalog.md`
- `docs/reference/ui-ownership.md`
- `docs/reference/game-structure.md`
- `docs/MVP_PLAN.md`

### UI ownership non-negotiables
- `awake:engine:ui-core` may own theme contracts and only a neutral fallback theme such as `CoreUiTheme`.
- `DefaultUiTheme`, `DarkUiTheme`, and `LightUiTheme` belong in `awake:engine:ui-designsystem`, not `ui-core`.
- `awake:engine:ui-widgets` may own only generic leaf widgets. Property rows, property checkboxes, inspector scaffolds, and tooling composition belong in `awake:engine:ui-dsl`.
- samples and games should pass a named theme from `awake:engine:ui-designsystem` instead of relying on `CoreUiTheme` defaults.
