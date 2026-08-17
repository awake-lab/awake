# UI Ownership

This document is the canonical source for Awake's reusable UI boundaries.

## Goal

Keep reusable UI building blocks separate from branded recipes and separate again from
sample-specific adapters.

## Hard Rules

These are placement rules, not style preferences.

1. `awake:engine:ui:ui-core` may expose only runtime mechanics: geometry resolution, drawing,
   anchoring, clipping, slots, input, focus, state, and a neutral fallback implementation. It
   must not expose component recipes, variants, or branded policy.
2. `awake:engine:ui:ui-headless` owns generic leaf-widget behavior and generic visual-state
   contracts built on `ui-core`. It must not own property-form, inspector, tooling-shell, or
   other multi-widget composition; those belong in `ui-dsl` (when that focused composition layer
   is present). It also must not own named variants or a design language.
3. `awake:engine:ui:ui-designsystem` owns branded or strongly opinionated recipes and every
   named authored theme intended for direct app/sample use.
4. `samples:*` and future game modules own runtime-bound adapters, authored overlays,
   debug HUD wiring, and sample-specific compositions.
5. `UiBounds` is an immutable resolved-bounds contract in `awake:ui:graphics` — the de-facto
   contract module (a dedicated `ui-api` Gradle module does not exist; references to "ui-api"
   in this doc mean the contract *role*, physically hosted by `graphics` today). It is shared
   by Core, Headless, Design System, renderers, and tests; it is not a Headless widget or a
   Core-only runtime capability.
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
   reaching for `.copy()`. (The pre-fix `.copy(widthDimension = ...)` call sites were fully
   swept by 2026-08-17 — none remain outside the builder functions themselves.)

## headless/Designsystem Content Pairing

Concrete rule for what belongs in each module, beyond "generic" vs "branded":

- Every reusable leaf widget in `ui-headless` gets a bare, unbranded name (`checkbox`, `button`,
  `slider`, `dropdown`, ...) and carries no named visual recipe. It accepts one neutral `Style`,
  including its state rules, not `Primary`, `Ghost`, `Outline`, Material, or shadcn vocabulary.
- `ui-designsystem` provides exactly one themed wrapper per `ui-headless` widget it recipes,
  named `shadcn<Widget>` (brand prefix + the same widget name, e.g. `checkbox` ->
  `shadcnCheckbox`). The wrapper maps a branded variant to a neutral Headless `Style`; it
  must not add new structural or behavioral logic the underlying widget doesn't already have.
  If a wrapper needs real new logic, that logic belongs in `ui-headless` (generic) -- see the
  note on neutral composition below.
- `ui-designsystem` may also own branded *compositions* that don't map to a single
  `ui-headless` widget (`shadcnDialog`, `shadcnDropdownMenu`, `shadcnAlertDialog`,
  `shadcnTabs`, `shadcnPropertyDropdown`, ...) when the composition itself is
  brand-opinionated. Every `ui-designsystem` export is `shadcn`-prefixed, including these --
  there is no unprefixed variant.

**Neutral composition belongs in a focused `ui-dsl` layer, not in Headless.** The previous
`ui-dsl` module was deleted on 2026-07-24 after reaching zero production code. That deletion did
not make multi-widget composition a Headless responsibility. Reintroduce or create the focused
composition layer only when production composition needs justify it; until then, keep the need
out of Headless rather than letting a temporary exception become an ownership leak.

## Module Responsibilities

| Module | Responsibility | Examples |
|---|---|---|
| `awake:engine:ui:ui-api` | Immutable cross-layer values and theme-value contracts | `UiBounds`, `Dimension`, `Dp`, `Sp`, color/shape/typography contracts |
| `awake:engine:ui:ui-core` | Runtime layout, drawing, input, state, and neutral component-style fallback | low-level layout, clipping, slots, pixel resolution, `CoreUiComponentStyles` |
| `awake:engine:ui:ui-headless` | Reusable leaf-widget behavior and neutral `Style` application | button mechanics, checkbox, text field, slider, primitive panels |
| `awake:engine:ui:ui-dsl` | Neutral multi-widget and tooling composition when needed | property rows/lists, inspector scaffolds, tooling shells |
| `awake:engine:ui:ui-designsystem` | Branded or strongly opinionated recipes | shadcn-style skins, `ShadcnDefaultTheme`, `DarkUiTheme`, `LightUiTheme`, branded presets |
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
  `avatarFallback` (`ui-headless/Avatar.kt`) only took `initials: String` and called `text(...)`
  internally with no slot alternative -- added a `content: BoxScope.(slot) -> Unit` primary
  overload (draws the circle only) with the string form as a convenience wrapper over it.

## Theme Values And Visual Policy

Theme *values* and component visual policy are deliberately separate:

- `ui-api` owns immutable, runtime-free value contracts such as `UiColorTokens`,
  `UiShapeTokens`, and `UiTypography`. They describe values; they do not prescribe a component
  recipe or a named design language.
- `ui-core` resolves and applies those values while running the UI, and provides the neutral
  `UiComponentStyles` contract plus `CoreUiComponentStyles` fallback. It also owns the neutral
  theme, typography, and text-style local machinery. Its defaults must remain generic and free
  of brand, product, or named-variant policy.
- `ui-headless` accepts and applies the generic `Style` shape a widget needs. It consumes Core's
  scope-level environment accessors rather than reaching into `UiContext` / `Local*` stacks, and
  it does not publish theme providers. `Style` is its only public visual contract. Headless
  must not name a state `Primary`, `Ghost`,
  `Outline`, Material, or shadcn, invent token names, or hardcode `Color(...)` values.
- `ui-designsystem` owns named themes, token instances, component recipes, and branded variants.
  Its lower-case `UiScope.shadcnTheme(...)` wrapper delegates to Core's neutral providers, while
  `shadcnThemeValues(...)` builds the complete, unscoped `ShadcnThemeValues` value.
  `ShadcnThemeValues` is design-system-owned and includes its Core-compatible values and metrics;
  public recipes expose
  semantic options rather than generic `Style` overrides.
  component-local style files map brand-specific variants and interaction states to `Style`. It
  may depend internally on Core for style and local infrastructure, but a component recipe must
  use Headless for layout, rendering, input, interaction, and semantics; it must not call Core
  widget primitives directly. It is the only module allowed to hardcode `Color(...)`/`Color.fromOklch(...)` literals, and only inside
  theme-definition files (`ShadcnTheme.kt`, `PresetUiThemes.kt`, `OklchColor.kt`) -- not inside
  individual component files.
- `samples:*` select a named design-system theme and must not hardcode `Color(...)` for anything
  a supplied theme or visual recipe already covers.

## Concrete Placement Examples

| API shape | Correct home | Why |
|---|---|---|
| `UiSlot.anchored(anchor, width, height, margin)` | `ui-core` | pure placement math returning a slot |
| `button`, `checkbox`, `slider` | `ui-headless` | generic reusable leaf widgets |
| `UiColorTokens`, `UiShapeTokens`, `UiTypography` | `ui-api` | immutable, runtime-free theme value contracts |
| neutral fallback theme resolution and `CoreUiComponentStyles` | `ui-core` | generic runtime fallback, not branded policy |
| neutral theme/text local providers | `ui-core` | runtime mechanics shared by every skin, with no branded API |
| `Style` / generic interaction-state rules | `ui-headless` | widget-state contract without branded names |
| `UiScope.shadcnTheme(...)` and shadcn `themeValues` access | `ui-designsystem` | branded scope and recipe-facing ambient access |
| `PropertyList`, `PropertyRow`, `propertyCheckbox`, generic inspector scaffolds | `ui-dsl` | neutral multi-widget/tooling composition, not a leaf widget |
| `ShadcnDefaultTheme`, `DarkUiTheme`, `LightUiTheme` | `ui-designsystem` | named authored themes belong above engine core |
| `ShadcnPanelStyle`, `Primary`/`Ghost`/`Outline` variants | `ui-designsystem` | branded visual policy, not engine primitive |
| hardcoded `Color(...)` token values | `ui-designsystem` theme-definition files only | everywhere else reads `theme.tokens.*` |
| `HelloCubeHud`, `SceneInspectorBindings`, demo overlays | sample/game module | runtime-bound authored usage |

These API shapes are specifically discouraged in reusable UI modules:

- `anchoredColumn(...)`
- `anchoredRow(...)`
- `anchoredPanel(...)`
- `propertyRow(...)` in `ui-headless`
- `propertyCheckbox(...)` in `ui-headless`
- `ShadcnDefaultTheme` in `ui-core`
- `DarkUiTheme` in `ui-core`
- `LightUiTheme` in `ui-core`
- `HelloCube*`
- helpers that know `SceneGameRuntime`, ECS `World`, demo modes, or sample debug state
- raw `Color(...)` literals in `ui-headless`, or in `ui-designsystem` component files
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
- express parent-axis fill through `fillMaxWidth()`, `fillMaxHeight()`, or `fillMaxSize()`;
  `Dimension.FillMax` is a Core layout-resolution sentinel and must not appear in public
  Headless or Design System authoring APIs

Keep raw pixel literals such as `12f.px` in low-level layout/render internals only, where the
code is already operating in measured pixel space. Shared UI modules, DSL layers, and samples
should not author visual values in pixels.

## What Must Stay Out Of Reusable UI Modules

If a UI component knows about any of these directly, it likely belongs in a sample or game
adapter layer instead of `ui-core`, `ui-headless`, or `ui`:

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
  composition -- see the note under headless/Designsystem Content Pairing above

## Folder Structure And File Naming (2026-07-24)

Decided via Q&A, applies going forward -- existing violations are not retroactively fixed by
this rule, only new/moved files must comply.

**`ui-core`**
- No new files directly under `ui/` root. Every new type goes in a themed subfolder
  (`modifier/`, `style/`, `layout/`, `scope/`, `theme/`, `context/`, `font/`, `graphics/`, or a
  new one if none fit). The remaining root files (`Canvas.kt`, `ScrollContainers.kt`, etc. —
  14 as of 2026-08-17; `UiAnchor.kt` and `Dp.kt` have since moved out) are legacy debt, not a
  model to copy -- the migration plan is audit row E2/pkg-3 in
  `docs/audits/2026-08-17-ui-refactor-vs-recreate-audit.md`.
- Public runtime types in `ui-core` are `Ui`-prefixed (`UiModifier`, `UiAnchor`, ...) -- the
  prefix signals an engine runtime type. Cross-layer immutable contracts such as `UiBounds`
  belong in `ui-api`, not Core.

**`ui-headless`**
- No `Ui`-prefix requirement -- the module name already scopes these as generic widgets
  (`Buttons.kt`, `Surface.kt`, `Spinner.kt`).
- Subfolder by interaction category: anything that takes user input goes under `input/`
  (further split by kind where it already exists: `input/text/`, `input/selection/`,
  `input/toggle/`); everything display-only (`Surface`, `Spinner`, `Separator`, `Avatar`,
  `Skeleton`) stays at the top level of `headless/`.

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

- foundational + generic -> `ui-core` or `ui-headless`
- compositional + generic -> `ui-headless` (or a scoped-for-purpose new module -- see the
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

This policy is build-enforced in Awake's reusable UI modules (paths corrected 2026-08-17 —
the checks had been silently disabled for headless/designsystem by a module-rename path
mismatch; see `docs/audits/2026-08-17-ui-refactor-vs-recreate-audit.md` row A1):

- `:awake:ui:ui-core:verifyUiOwnership` (in `check`)
- `:awake:ui:headless:verifyUiOwnership` (in `check`)
- `:awake:ui:designsystem:verifyUiOwnership` (in `check`)
- `:awake:ui:designsystem:auditUiDesignsystemComponentNaming` (in `check`)
- `:awake:ui:designsystem:auditUiDesignsystemRecipeDuplicates` (in `check`)
- `:awake:ui:designsystem:auditUiDesignsystemComponentCoverage` (in `check`)

`verifyUiOwnership` rejects:

- container-bound anchored helper names such as `anchoredColumn`
- `propertyRow` and `propertyCheckbox` in `ui-core`/`ui-headless`
- `ShadcnDefaultTheme`, `DarkUiTheme`, and `LightUiTheme` in `ui-core`
- direct sample/runtime-bound references such as `SceneGameRuntime` or `HelloCube*`
- in designsystem: the `primitive.context` escape hatch, `ui.UiScope` imports, and imports
  of Core runtime packages (`layouts`, `popup`, `scope`, `animate`, `child`, `modifier`,
  `unstyled`, and `context` except the licensed `UiLocal`/`uiLocalOf` contract)
- a module applying the convention without being classified in it (build error, so a module
  rename can never silently disarm the check again)

Known pre-existing escapes are carried as an explicit per-file exemption ledger inside the
convention (`exemptUiSourcePatternFiles` — currently `ShadcnButtonGroupRecipes.kt` and
`ShadcnThemeLocals.kt`, tracked as audit rows B9/D2). The ledger may only shrink.

The designsystem audits additionally reject:

- component files that do not use the `Shadcn*` family prefix or matching subpackage
- the same `shadcn*` recipe (same receiver) declared in more than one file — keyed by file,
  not package, since components deliberately share one flat package; same-file overloads
  (string convenience beside the slot form) remain valid
- public component files that do not delegate through `ui-headless` (contract-only files are
  explicitly exempt)

The temporary `:awake:ui:designsystem-compat` module and its consumer audit were deleted
once migration completed (2026-08-13).

The check is intentionally lightweight and curated. It is not a theorem prover. When the
policy grows, expand the canonical doc first, then update the check.

Theme values follow the same boundary: design-system modules publish `UiThemeValues`, while
Core-owned runtime installers such as `GameUiDsl` may pass those values through
`UiThemeValues.asRuntimeTheme()`. This keeps the runtime facade in Core without making
design-system code depend on Core's component recipes.

Awake also build-enforces authored-unit usage in:

- `:awake:ui:ui-headless`
- `:awake:ui:ui-designsystem`
- `:awake:engine:game-authoring`
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

State hooks have two identity levels: `id` chooses the persistent `WidgetState` bucket for one
widget instance, and the hook `key` chooses a value within that bucket. Leave `key = "value"`
when a widget owns one value; name the fields when it owns several, for example
`rememberBooleanState(id, key = "expanded")` and `rememberStateValue(id, key = "filter")`.
The two are deliberately not interchangeable: changing `id` resets all of that widget's state,
while changing a hook key resets only that field.
