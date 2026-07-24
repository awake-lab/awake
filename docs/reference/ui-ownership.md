# UI Ownership

This document is the canonical source for Awake's reusable UI boundaries.

## Goal

Keep reusable UI building blocks separate from branded recipes and separate again from
sample-specific adapters.

## Hard Rules

These are placement rules, not style preferences.

1. `awake:engine:ui-core` may expose only geometry, drawing, anchoring, clipping, slot,
   style primitives, theme contracts, and at most a neutral fallback theme.
2. `awake:engine:ui-unstyled` may expose only generic leaf widgets built on `ui-core`. It
   must not own property-form, inspector, or tooling-shell composition.
3. `awake:engine:ui-dsl` may expose generic composition templates and DSL surfaces, and it
   owns neutral property forms and reusable tooling composition, but it
   must not introduce helper APIs that collapse a low-level primitive and a specific
   container into one convenience function when a slot/rect primitive can express it.
4. `awake:engine:ui-designsystem` owns branded or strongly opinionated recipes and every
   named authored theme intended for direct app/sample use.
5. `samples:*` and future game modules own runtime-bound adapters, authored overlays,
   debug HUD wiring, and sample-specific compositions.
6. `UiSlot` is a `ui-core`-internal measurement type. No module outside `ui-core` may
   construct, read, or `.copy()` a `UiSlot`. Anything crossing the `ui-core` boundary
   (`surface{}`/`row{}`/`claimSlot()` return values, `UiSemanticNode.contentBounds`, lambda
   params handed to widget code) must use `UiBounds` instead. See
   `docs/tasks/2026-07-24-uislot-narrowing.md` for the migration in progress.

## Module Responsibilities

| Module | Responsibility | Examples |
|---|---|---|
| `awake:engine:ui-core` | Foundational drawing, layout, and surface primitives | low-level layout, drawing, clipping, slots, style plumbing, `UiTheme`, `CoreUiTheme` |
| `awake:engine:ui-unstyled` | Reusable widget primitives built on `ui-core` | button, checkbox, text field, slider, primitive panels |
| `awake:engine:ui-dsl` | Style-agnostic composition templates and UI DSL surfaces | shells, sections, property forms, reusable inspector layouts |
| `awake:engine:ui-designsystem` | Branded or strongly opinionated recipes | shadcn-style skins, `DefaultUiTheme`, `DarkUiTheme`, `LightUiTheme`, branded presets |
| `samples:*` or game modules | Sample/game adapters and authored usage | scene inspector bindings, demo-specific overlays, debug HUD wiring |

## Primitive Vs Composition

Treat these as reusable primitives:

- `Panel`
- `Section`
- `PropertyList`
- `PropertyRow`
- `UiSlot.anchored(...)`

Treat these as higher-level compositions:

- `InspectorPane`
- sample shells
- demo overlays
- app-specific control bars

Rule: a primitive should still make sense outside the current sample or demo.

Text ownership rule:

- leaf widgets may expose simple text params such as `label`, `title`, or `supportingText`
- structural or multi-region compositions should expose slots or restricted-scope regions as
  the primary API, with string overloads kept only as convenience wrappers
- if a reusable API owns both the container structure and all displayed text, it is usually too
  coupled for long-term reuse

## Theme Token Rule

Where a color/token comes from depends on the module:

- `ui-core` owns the *contract* only -- `UiColorTokens` (the interface), `UiTheme`, and
  `CoreUiTheme` (the one neutral fallback instance, generic gray/white values). No named or
  branded token values live here.
- `ui-unstyled` and `ui-dsl` must never call `Color(...)` directly or hardcode a token value.
  Widgets read colors exclusively through the ambient theme (`theme.tokens.background`,
  `theme.tokens.primary`, etc.) or a resolved `Style` built from those tokens. This is already
  true in practice -- `ui-unstyled` has zero raw `Color(...)` call sites.
- `ui-designsystem` owns every named/branded theme and is the only module allowed to hardcode
  `Color(...)`/`Color.fromOklch(...)` literals, and only inside theme-definition files
  (`AwakeShadcnTheme.kt`, `PresetUiThemes.kt`, `OklchColor.kt`) -- not inside individual
  component files, which should still read `theme.tokens.*` like everything else.
- `samples:*` may reference a named theme (`AwakeShadcnTheme`, etc.) but must not hardcode
  `Color(...)` for anything that a token already covers.

## Concrete Placement Examples

| API shape | Correct home | Why |
|---|---|---|
| `UiSlot.anchored(anchor, width, height, margin)` | `ui-core` | pure placement math returning a slot |
| `button`, `checkbox`, `slider` | `ui-unstyled` | generic reusable leaf widgets |
| `CoreUiTheme`, `UiTheme`, `UiColorTokens` | `ui-core` | theme contract and neutral fallback only |
| `PropertyList`, `PropertyRow`, `propertyCheckbox`, generic inspector scaffolds | `ui-dsl` | reusable compositions of primitives/widgets |
| `DefaultUiTheme`, `DarkUiTheme`, `LightUiTheme` | `ui-designsystem` | named authored themes belong above engine core |
| `AwakeShadcnPanelStyle`, branded variants | `ui-designsystem` | visual opinion, not engine primitive |
| hardcoded `Color(...)` token values | `ui-designsystem` theme-definition files only | everywhere else reads `theme.tokens.*` |
| `HelloCubeHud`, `SceneInspectorBindings`, demo overlays | sample/game module | runtime-bound authored usage |

These API shapes are specifically discouraged in reusable UI modules:

- `anchoredColumn(...)`
- `anchoredRow(...)`
- `anchoredPanel(...)`
- `propertyRow(...)` in `ui-unstyled`
- `propertyCheckbox(...)` in `ui-unstyled`
- `DefaultUiTheme` in `ui-core`
- `DarkUiTheme` in `ui-core`
- `LightUiTheme` in `ui-core`
- `HelloCube*`
- helpers that know `SceneGameRuntime`, ECS `World`, demo modes, or sample debug state
- raw `Color(...)` literals in `ui-unstyled`/`ui-dsl`, or in `ui-designsystem` component files
  outside its theme-definition files

Rule of thumb: if the API name bakes in both a placement concern and a concrete container,
it is usually too high-level for `ui-core`, and usually too convenience-shaped for long-term
reuse.

## Authored Units And Spacing

For authored UI in shared and sample-facing layers:

- use `Dp` for layout, spacing, padding, radius, and widget sizing
- use `Sp` for text sizing
- use `Arrangement.*` for row/column spacing instead of authored `gap = ...` call sites
- use `UiModifier.padding(...)` instead of authored public `insets = ...` call sites

Keep raw pixel literals such as `12f.px` in low-level layout/render internals only, where the
code is already operating in measured pixel space. Shared UI modules, DSL layers, and samples
should not author visual values in pixels.

## What Must Stay Out Of Reusable UI Modules

If a UI component knows about any of these directly, it likely belongs in a sample or game
adapter layer instead of `ui-core`, `ui-unstyled`, or `ui`:

- `SceneGameRuntime`
- ECS `World` or direct system access
- entity names or entity selection state owned by a sample
- demo modes
- sample-only debug toggles

The reusable module should accept generic state, callbacks, and content slots instead.

Theme rule:

- `ui-core` stays neutral. If the API needs a no-dependency fallback theme, keep it generic
  and do not treat it as the authored app theme.
- named shipped themes for apps, demos, docs, and samples belong in `ui-designsystem`
- property-form rows, checkbox rows, and inspector-style control groupings belong in `ui-dsl`,
  even when they are visually conservative

## Folder Structure And File Naming (2026-07-24)

Decided via Q&A, applies going forward -- existing violations are not retroactively fixed by
this rule, only new/moved files must comply.

**`ui-core`**
- No new files directly under `ui/` root. Every new type goes in a themed subfolder
  (`modifier/`, `style/`, `layout/`, `scope/`, `theme/`, `context/`, `font/`, `graphics/`, or a
  new one if none fit). The 16 existing root files (`UiAnchor.kt`, `Canvas.kt`, `Dp.kt`, etc.)
  are legacy debt, not a model to copy -- migrating them is a separate, not-yet-scoped cleanup.
- Public types in `ui-core` are `Ui`-prefixed (`UiModifier`, `UiSlot`, `UiAnchor`, ...) -- the
  prefix signals "engine contract type" the way it already does today.

**`ui-unstyled`**
- No `Ui`-prefix requirement -- the module name already scopes these as generic widgets
  (`Buttons.kt`, `Surface.kt`, `Spinner.kt`).
- Subfolder by interaction category: anything that takes user input goes under `input/`
  (further split by kind where it already exists: `input/text/`, `input/selection/`,
  `input/toggle/`); everything display-only (`Surface`, `Spinner`, `Separator`, `Avatar`,
  `Skeleton`) stays at the top level of `unstyled/`.

**`ui-designsystem`**
- Every component file is prefixed with its brand name (`AwakeShadcn*` today). Components
  currently live flat under `components/` since only one brand exists -- not yet moved into a
  `components/shadcn/` subfolder, since there is nothing to disambiguate from yet. The rule for
  when a second named theme/brand is added: give it its own prefix + subfolder
  (`components/<brand2>/`), and retroactively move the first brand's files into
  `components/shadcn/` at that point, rather than a new Gradle module -- only split into a
  separate module if a brand needs its own dependency graph, not just its own visual language.

## Naming Guidance

- use general names for real reusable pieces
- avoid making app-specific shells sound foundational
- if a piece is mostly a concrete composition of primitives, name it like a composition
  rather than a primitive

Examples:

- good primitive: `Panel`
- good composition: `InspectorPane`
- suspicious primitive name: `InspectorPanel` if it is tightly tied to one sample workflow

## Placement Checks

Before adding a new UI type, ask:

1. Can this be reused without ECS or sample state?
2. Is it a foundational primitive or a composition?
3. Does it define brand/look opinion, or only structure?

Use the answers like this:

- foundational + generic -> `ui-core` or `ui-unstyled`
- compositional + generic -> `ui-dsl`
- branded/opinionated -> `ui-designsystem`
- sample/runtime-bound -> sample or game module

## Mechanical Enforcement

This policy is build-enforced in Awake's reusable UI modules.

- `:awake:engine:ui-core:check`
- `:awake:engine:ui-unstyled:check`
- `:awake:engine:ui-dsl:check`

run a `verifyUiOwnership` task that rejects:

- container-bound anchored helper names such as `anchoredColumn`
- `propertyRow` and `propertyCheckbox` in `ui-core`/`ui-unstyled`
- `DefaultUiTheme`, `DarkUiTheme`, and `LightUiTheme` in `ui-core`
- direct sample/runtime-bound references such as `SceneGameRuntime` or `HelloCube*`

The check is intentionally lightweight and curated. It is not a theorem prover. When the
policy grows, expand the canonical doc first, then update the check.

Awake also build-enforces authored-unit usage in:

- `:awake:engine:ui-unstyled`
- `:awake:engine:ui-dsl`
- `:awake:engine:ui-designsystem`
- `:awake:engine:game-dsl`
- `:samples:ui-showcase`

Those modules run `verifyUiAuthoredUnits`, which currently rejects numeric `.px` literals in
`*Main` source so shared/sample UI stays authored in `Dp`/`Sp`.
