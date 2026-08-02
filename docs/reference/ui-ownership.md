# UI Ownership

This document is the canonical source for Awake's reusable UI boundaries.

## Goal

Keep reusable UI building blocks separate from branded recipes and separate again from
sample-specific adapters.

## Hard Rules

These are placement rules, not style preferences.

1. `awake:engine:ui:ui-core` may expose only geometry, drawing, anchoring, clipping, slot,
   style primitives, theme contracts, and at most a neutral fallback theme.
2. `awake:engine:ui:ui-unstyled` may expose only generic leaf widgets built on `ui-core`. It
   must not own property-form, inspector, or tooling-shell composition.
3. `awake:engine:ui:ui-designsystem` owns branded or strongly opinionated recipes and every
   named authored theme intended for direct app/sample use.
4. `samples:*` and future game modules own runtime-bound adapters, authored overlays,
   debug HUD wiring, and sample-specific compositions.
5. **Superseded (2026-08-01):** `UiSlot` was merged into `UiBounds` -- there is now a single
   `ui-core`-public measured-bounds type (`io.github.ronjunevaldoz.awake.ui.layout.UiBounds`),
   not a public/internal pair. The dual-type split this rule described, and the mechanical
   enforcement it called for, were dropped as not worth the maintenance cost once Batch 2 of
   `docs/tasks/2026-07-24-uislot-narrowing.md` had already narrowed the public surface. See that
   doc's Resolution section for the full rationale.
6. **No authored param may duplicate what `UiModifier` or `Style` already expresses.** Not
   limited to params literally named `width`/`height`/`gap`/`insets` -- any `Dp`/`Float` param
   that's really a size, position, or spacing value under another name (`diameter`, `radius`
   used as a dimension, `length`, `thickness`, `spacing`, ...) is the same violation. Before
   adding a sizing/spacing param to a widget signature, check whether
   `Modifier.size()`/`.width()`/`.height()`/`.padding()`/`.offset()` or
   `Style.shape()`/`.borderWidth()` already covers it. This has no generic mechanical check
   (param names vary too much for a denylist) -- it's a manual gate at review time. Found and
   fixed 2026-07-24: `avatarFallback`'s `diameter: Dp` param (see `docs/tasks/
   2026-07-17-ui-api-simplification.md`'s modifier-first policy for the full history of this
   class of bug, starting with `surface()`'s `radius`/`borderWidth`).
7. **Prefer `UiModifier` builder chaining (`.width().height().padding()`) over `.copy(...)`.**
   `.copy()` used to be the workaround for a name collision between `UiModifier`'s stored
   `width`/`height` fields and the `.width()`/`.height()` builder functions -- fixed 2026-07-24
   by renaming the fields to `widthDimension`/`heightDimension`, so the workaround is no longer
   needed for those two. New code should chain builder calls, not `.copy(widthDimension = ...)`.
   Other fields (`forceHover`/`forceActive`/`forceFocus`/`testTag`/`styleable`/`graphicsLayer`)
   share the same collision shape with their builder functions and carry the same latent risk
   -- if you hit the ambiguity error on one of those, the fix is the same rename pattern, not
   reaching for `.copy()`. Existing `.copy(widthDimension = ...)` call sites from before this
   fix were not swept to chained calls in this pass (mechanical follow-up, not yet scoped).

## Unstyled/Designsystem Content Pairing

Concrete rule for what belongs in each module, beyond "generic" vs "branded":

- Every reusable leaf widget in `ui-unstyled` gets a bare, unbranded name (`checkbox`, `button`,
  `slider`, `dropdown`, ...) and carries zero style opinion -- callers must supply a `Style`/theme
  for it to look like anything.
- `ui-designsystem` provides exactly one themed wrapper per `ui-unstyled` widget it recipes,
  named `shadcn<Widget>` (brand prefix + the same widget name, e.g. `checkbox` ->
  `shadcnCheckbox`). That wrapper's only job is supplying the brand's `Style`/theme
  defaults on top of the existing `ui-unstyled` widget -- it must not add new structural or
  behavioral logic the underlying widget doesn't already have. If a wrapper needs real new
  logic, that logic belongs in `ui-unstyled` (generic) -- see the note on neutral
  composition below.
- `ui-designsystem` may also own branded *compositions* that don't map to a single
  `ui-unstyled` widget (`shadcnDialog`, `shadcnDropdownMenu`, `shadcnAlertDialog`,
  `shadcnTabs`, `shadcnPropertyDropdown`, ...) when the composition itself is
  brand-opinionated. Every `ui-designsystem` export is `shadcn`-prefixed, including these --
  there is no unprefixed variant.

**Neutral composition currently has no dedicated module.** `awake:engine:ui-dsl` used to be
that home but was deleted 2026-07-24 -- it had accumulated zero production code (every real
composition had already migrated into `ui-unstyled`/`ui-designsystem`/`game-dsl` over time,
leaving only stray test files behind). If a genuinely neutral (unbranded) composition need
comes up again, don't default to recreating a `ui-dsl`-shaped module reflexively -- place it in
`ui-unstyled` if it's still widget-shaped, or scope a new module deliberately if it's large
enough to need one. Treat "we need a neutral composition layer" as a real decision to make at
the time, not a foregone conclusion.

## Module Responsibilities

| Module | Responsibility | Examples |
|---|---|---|
| `awake:engine:ui:ui-core` | Foundational drawing, layout, and surface primitives | low-level layout, drawing, clipping, slots, style plumbing, `UiTheme`, `CoreUiTheme` |
| `awake:engine:ui:ui-unstyled` | Reusable widget primitives built on `ui-core` | button, checkbox, text field, slider, primitive panels |
| `awake:engine:ui:ui-designsystem` | Branded or strongly opinionated recipes | shadcn-style skins, `DefaultUiTheme`, `DarkUiTheme`, `LightUiTheme`, branded presets |
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
- **any leaf widget that renders content by calling another widget internally (e.g. calling
  `text(...)` to draw a label) must expose a slot/content-lambda primary form, not only the
  string convenience.** If the only entry point takes a raw string and renders it internally
  with a hardcoded widget call, the widget is coupled to that specific content widget and
  cannot be reused with an icon, multiple lines, or anything else. Found and fixed 2026-07-24:
  `avatarFallback` (`ui-unstyled/Avatar.kt`) only took `initials: String` and called `text(...)`
  internally with no slot alternative -- added a `content: BoxScope.(slot) -> Unit` primary
  overload (draws the circle only) with the string form as a convenience wrapper over it.

## Theme Token Rule

Where a color/token comes from depends on the module:

- `ui-core` owns the *contract* only -- `UiColorTokens` (the interface), `UiTheme`, and
  `CoreUiTheme` (the one neutral fallback instance, generic gray/white values). No named or
  branded token values live here.
- `ui-unstyled` must never call `Color(...)` directly or hardcode a token value.
  Widgets read colors exclusively through the ambient theme (`theme.tokens.background`,
  `theme.tokens.primary`, etc.) or a resolved `Style` built from those tokens. This is already
  true in practice -- `ui-unstyled` has zero raw `Color(...)` call sites.
- `ui-designsystem` owns every named/branded theme and is the only module allowed to hardcode
  `Color(...)`/`Color.fromOklch(...)` literals, and only inside theme-definition files
  (`ShadcnTheme.kt`, `PresetUiThemes.kt`, `OklchColor.kt`) -- not inside individual
  component files, which should still read `theme.tokens.*` like everything else.
- `samples:*` may reference a named theme (`ShadcnTheme`, etc.) but must not hardcode
  `Color(...)` for anything that a token already covers.

## Concrete Placement Examples

| API shape | Correct home | Why |
|---|---|---|
| `UiSlot.anchored(anchor, width, height, margin)` | `ui-core` | pure placement math returning a slot |
| `button`, `checkbox`, `slider` | `ui-unstyled` | generic reusable leaf widgets |
| `CoreUiTheme`, `UiTheme`, `UiColorTokens` | `ui-core` | theme contract and neutral fallback only |
| `PropertyList`, `PropertyRow`, `propertyCheckbox`, generic inspector scaffolds | `ui-unstyled` (or a scoped-for-purpose new module, see Neutral composition note above) | reusable compositions of primitives/widgets |
| `DefaultUiTheme`, `DarkUiTheme`, `LightUiTheme` | `ui-designsystem` | named authored themes belong above engine core |
| `ShadcnPanelStyle`, branded variants | `ui-designsystem` | visual opinion, not engine primitive |
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
- raw `Color(...)` literals in `ui-unstyled`, or in `ui-designsystem` component files
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
- property-form rows, checkbox rows, and inspector-style control groupings are neutral
  composition -- see the note under Unstyled/Designsystem Content Pairing above

## Folder Structure And File Naming (2026-07-24)

Decided via Q&A, applies going forward -- existing violations are not retroactively fixed by
this rule, only new/moved files must comply.

**`ui-core`**
- No new files directly under `ui/` root. Every new type goes in a themed subfolder
  (`modifier/`, `style/`, `layout/`, `scope/`, `theme/`, `context/`, `font/`, `graphics/`, or a
  new one if none fit). The 16 existing root files (`UiAnchor.kt`, `Canvas.kt`, `Dp.kt`, etc.)
  are legacy debt, not a model to copy -- migrating them is a separate, not-yet-scoped cleanup.
- Public types in `ui-core` are `Ui`-prefixed (`UiModifier`, `UiBounds`, `UiAnchor`, ...) -- the
  prefix signals "engine contract type" the way it already does today.

**`ui-unstyled`**
- No `Ui`-prefix requirement -- the module name already scopes these as generic widgets
  (`Buttons.kt`, `Surface.kt`, `Spinner.kt`).
- Subfolder by interaction category: anything that takes user input goes under `input/`
  (further split by kind where it already exists: `input/text/`, `input/selection/`,
  `input/toggle/`); everything display-only (`Surface`, `Spinner`, `Separator`, `Avatar`,
  `Skeleton`) stays at the top level of `unstyled/`.

**`ui-designsystem`**
- Every component file is prefixed with its brand name (`Shadcn*` today). Components
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
- compositional + generic -> `ui-unstyled` (or a scoped-for-purpose new module -- see the
  Neutral composition note above; there is no default composition-only module anymore)
- branded/opinionated -> `ui-designsystem`
- sample/runtime-bound -> sample or game module

## Test File Rule (2026-07-24)

One test file per component/behavior under test. A test file that covers several unrelated
components "because they're all UI" is a god-class test file -- it hides which component broke
when the suite fails, and it invites unrelated tests to keep accumulating in one place with no
natural stopping point.

- Name the test file after the thing it tests (`CheckboxTest.kt`, not `WidgetsTest.kt` or
  `UiTest.kt`). If you can't name it after one component, that's the signal it's testing too
  much.
- A shared setup helper (e.g. a `testSnapshot()` frame builder) used by multiple test files is
  fine and expected -- put it in its own `*TestSupport.kt` file, not inline in one of the test
  classes that happens to need it first.
- Integration/composition tests that deliberately exercise several primitives together (e.g.
  proving `surface`/`row`/`dropdown` compose correctly) are still one file per *composition
  pattern under test*, named for that pattern -- not bundled into a generic catch-all just
  because no single widget owns the whole scenario.
- Found and fixed 2026-07-24: `UiDslTest.kt` (318 lines, 8 tests spanning surface/dropdown/
  toggle/canvas/scroll, with its own doc comment reading `/** Too generic **/`) was split into
  `CanvasResponsiveLayoutTest.kt`, `ToggleCompositionTest.kt`, `ScrollPanelCompositionTest.kt`,
  `RowSpacerCompositionTest.kt`, `SurfaceDropdownCompositionTest.kt`, and a shared
  `UiDslTestSupport.kt` for the `testSnapshot()` helper, when the file moved out of the deleted
  `ui-dsl` module into `game-dsl`.
- Not (yet) mechanically enforced -- like the modifier-first checklist above, file scope is a
  judgment call a lint can't reliably make. Catch it at review time or during an audit pass.

## Mechanical Enforcement

This policy is build-enforced in Awake's reusable UI modules.

- `:awake:engine:ui:ui-core:check`
- `:awake:engine:ui:ui-unstyled:check`

run a `verifyUiOwnership` task that rejects:

- container-bound anchored helper names such as `anchoredColumn`
- `propertyRow` and `propertyCheckbox` in `ui-core`/`ui-unstyled`
- `DefaultUiTheme`, `DarkUiTheme`, and `LightUiTheme` in `ui-core`
- direct sample/runtime-bound references such as `SceneGameRuntime` or `HelloCube*`

The check is intentionally lightweight and curated. It is not a theorem prover. When the
policy grows, expand the canonical doc first, then update the check.

Awake also build-enforces authored-unit usage in:

- `:awake:engine:ui:ui-unstyled`
- `:awake:engine:ui:ui-designsystem`
- `:awake:engine:game-dsl`
- `:samples:ui-showcase`

Those modules run `verifyUiAuthoredUnits`, which currently rejects numeric `.px` literals in
`*Main` source so shared/sample UI stays authored in `Dp`/`Sp`.

## Identity Params: `id` vs `testTag` vs `cacheKey`

Three different string-identity params exist across the UI API. They look similar but serve
unrelated purposes -- never treat one as a substitute for another.

| Param | Type | Required? | Purpose | Lifetime |
|---|---|---|---|---|
| `id: String` | positional widget param | required, no default | Cross-frame `WidgetState` lookup key -- dropdown open/close, animation progress, any per-widget state that must survive frame-to-frame | Stable for the widget's lifetime; changing it resets its state |
| `testTag: String?` | `UiModifier` field | optional, `null` default | Debug/test identification only -- has zero effect on rendering, state, or measurement | N/A -- cosmetic |
| `id` / `cacheKey` (on `row()`/`column()`) | opt-in perf params | optional, both `null` | Cross-frame trial-measure cache key (see `docs/tasks/2026-08-02-trial-measure-cross-frame-cache.md`) -- `cacheKey` must change whenever the weighted-child structure could change | Opt-in; omitting both means no caching, safest default |

Rule: if a widget owns state (open/closed, progress, selection), it takes a required
`id: String`. If you only need to find/assert on a node in a test, use `testTag` via the
modifier -- don't repurpose `id` for that. If you're opting a `row()`/`column()` into the
trial-measure cache, that's a third, unrelated `id`/`cacheKey` pair -- supplying it does not
give the row/column any `WidgetState`.

Anti-pattern: adding a new `id`-like param to a widget "just in case" without deciding which
of the three jobs it does. Pick one, name it for that job, document why.

Composite ids (a container building its children's ids from its own, e.g.
`id = "$id.track"`, `id = "$id.$index"`) are the existing convention -- see
`ShadcnTabs.kt` -- and stay plain string interpolation. A wrapper function for this was
considered and rejected: the interpolation is already one line and self-explanatory: adding
an indirection for it is a net increase in concepts-to-learn for no real gain.
