# UI roadmap — consolidated 2026-08-14

Supersedes `2026-08-14-ui-implementation-plan.md` (phases 0–2 are done). Companion:
`2026-08-14-ui-layout-weight-parking.md` for what is already ruled out with evidence.

## Where things stand

Whole UI + studio surface: **2 failing tests**, both committed reproductions of known-open bugs.
Everything else green. The shadcn parity gate passes on all twelve pairs.

### Landed this session

| area | commits |
|---|---|
| text centres on the cap box (descenders) | `2d8abbaa` |
| scroll viewport measures with a real height (infinite scroll, phantom scrollbar) | `5cf62917` |
| weight distribution: column child, surface, nested measure scope | `eb1bda4b`, `b10a3a11`, `111793c8` |
| sidebar content weighted so header/footer pin | `f59c4f32` |
| radio group owns its column (options were stacking at 0,0) | `3f310f44` |
| OTP backing field stops painting its value | `d46d2b26` |
| surface foreground beats an inherited text colour (dark-on-dark) | `c984011e` |
| studio: bar semantics + two stale test contracts | `03771720` |
| loud failures: `row`/`box` scroll, dropped weight | `167d75a7`, `bcf11632` |
| gates: sizing matrix, contrast matrix | `e0f66992`, `c984011e` |
| skills: one entry point per widget, test tiers | `8e8b339e`, `7d2f6f47` |

### Open — 2 failing tests

1. **`ShadcnSidebarExampleLayoutTest`** — footer at y=144 in the preview while every
   component-level test passes. A weighted child placed DIRECTLY in a surface nested inside a
   scroll viewport still plans nothing. `111793c8` was necessary, not sufficient. `shadcnSidebar`
   keeps a wrapper column with the reproduction written in its comment.
2. **`StudioModuleCameraTest.clickingTopInTheIconRailCameraMenu…`** — camera popup never opens.
   The 2026-08-11 diagnosis (100,000px sentinel via `shadcnToggleGroupContainer`) is **stale**:
   that component no longer exists and the header now passes explicit sizes. Needs re-instrumenting
   from measured bounds, not the old write-up.

### Pending from the original report

- reusable demo-page container; code tab that renders from the same source as the preview;
  missing component previews
- "text not visible in icons" — the badge half is fixed by `c984011e`; the icon half is unverified
  and needs one component named

### Parked

- `studio-camera-adoption` branch (`840a5c7c`), 4 tests red, `CameraSystem` needs a target entity
- `World → SceneDocument` (the one missing function for saving)
- studio cursor (`SceneGameRuntime` discards it)

---

## Why `pushTheme` / `pushFont` / `pushTextStyle` look coupled

They are **not** coupled to each other. `UiContextStacks` holds seven independent parallel stacks —
theme, textStyle, textStyleToken, font, shapeSpec, alpha, transform — each with its own
`push`/`pop`/`current`. The coupling is elsewhere, and it is what produced today's dark-on-dark bug:

**1. Every stack is hand-written, and most of it is copy-paste.** Counting the bodies in
`UiContextStacks`:

| | duplicated? |
|---|---|
| `popTheme` / `popFont` / `popShapeSpec` / `popTransform` / `popTextStyle` | identical — `if (stack.size > 1) stack.removeAt(lastIndex)` |
| `pushTheme` / `pushFont` / `pushShapeSpec` / `pushTransform` | identical — `stack.add(value)` |
| **`pushTextStyle`** | **NOT** — merges with the parent (`last() then style`) and pushes a second parallel stack |
| **`pushAlpha`** | **NOT** — compounds with the parent (`currentAlpha * alpha`) |

So roughly 9 of 14 members are pure duplication. But the two that are not are the ones that matter:
a scoped value is not always "replace the parent" — text style **merges** and alpha **multiplies**.

That is the real design constraint for a generic provider. A naive `provide(key, value)` that only
did `add` would silently break both: text styles would stop inheriting and alpha would stop
compounding through nested layers. The generic form needs a **combine function per key**, not just a
value — `LocalTextStyle` combining by merge, `LocalAlpha` by product, `LocalTheme` by replacement.
Getting that wrong is the same silent-wrong-answer failure mode as the rest of this session.

**2. Push/pop is manual, so imbalance is possible.** `Surface.kt` calls `pushTextStyle(...)` and
must remember the matching pop. A missed pop leaks into siblings for the rest of the frame.
Compose's `CompositionLocalProvider(x provides y) { }` cannot leak because scope is the API.

**3. `textStyle` and `textStyleToken` genuinely ARE coupled** — two parallel stacks that must move
together, maintained by convention rather than by type. `pushTextStyle(style, tokenId)` pushes both;
nothing stops them diverging and silently mis-attributing token provenance.

**4. Provenance is not recorded — this is the important one.** `Style.resolve()` seeds its builder
from `currentTextStyle`, so a resolved value cannot say whether a colour was *declared here* or
*merely inherited*. `surface` tested `resolved.textStyle.color == null` to mean "nothing declared
it", which was only ever true at the top of the tree. `c984011e` reconstructs provenance by
comparing against the inherited value — correct, but a workaround for a type that should carry it.

---

## Plan

Ordered so each phase leaves a state the next can trust.

### Phase A — close the two open bugs

**A1. Weighted child directly inside a surface nested in a scroll viewport.** Build the instrument
first: print what `planWeightedColumnSlots`' trial returns (`measured.weights`, `slots.size`) for
that nesting. Do not start from a hypothesis — four were disproved chasing this. Closes the sidebar
wrapper and `ShadcnSidebarExampleLayoutTest`.

**A2. Camera popup.** Re-instrument from measured bounds. Add the missing shell invariant —
*nothing lays out beyond its frame* — to `StudioShellLayoutTest`, which catches the whole sentinel
class rather than this one symptom.

### Phase B — the reported UI work

**B1. One reusable demo-page container.** `showcasePage(title, subtitle, description) { preview /
code }`. Fixes inconsistent container sizes and the over-weighted Notes section.

**B2. Code tab renders from the same source as the preview**, so "code doesn't match preview"
cannot recur. This is what makes B1 worth doing over cosmetic tidying.

**B3. Missing previews**, once the container exists. Every preview added this session
(`shadcn-sidebar-example`, `ui-showcase-input-otp`) immediately found a bug the unit tests missed —
this is the highest-yield-per-effort item on the list.

Expect B1–B3 to move many baselines. Source commit, then a baselines-only commit.

### Phase C — generalise the scoped-value machinery

**C1. One generic provider** replacing seven hand-written stacks:

```kotlin
context.provide(LocalTextStyle to style, LocalFont to font) { ... }   // scoped, cannot leak
```

Keep `pushTheme`/`pushFont` as thin wrappers so nothing breaks. This is Awake's honest equivalent
of `CompositionLocal` — the concept already exists, it just has no general form.

**C2. Record provenance on resolved values**, so "declared here" vs "inherited" is a property
rather than something reconstructed by comparison (see coupling note 4). Retires `c984011e`'s
workaround and prevents the whole bug class.

**C3. Fold `textStyleToken` into `textStyle`** so the two cannot diverge.

### Phase D — API shape

**D1. `shadcnTheme { }` as a scoped block** — achievable and worth doing once C1 lands, because the
theme stack already *is* a scoped provider. This gives the `ShadcnTheme { … }` reading you asked
about.

**D2. `App()` / `setContent` is NOT adoptable.** That syntax is Jetpack Compose: composition,
recomposition, composable trees. Awake is immediate-mode — `beginFrame` → build → `endFrame`, every
frame. `Game()` in particular is a frame loop owning a renderer and an ECS world, not a widget; the
real relationship is the inverse, with the runtime driving the UI each frame. A `shadcnTheme { }`
block is the part of that ergonomics worth having; the rest would be a facade over a different
execution model.

### Phase E — packaging (samples are libraries, not apps)

**Confirmed:** `samples/ui-showcase` and `samples/scene3d-playground` both apply
`libs.plugins.android.library.kmp`, and **no module in the repo declares `applicationId` or
`android.application`** — so nothing is installable on a device. Both also carry an `appMain`
source set (`UiShowcaseVulkanBootstrap.kt`), i.e. they are shaped like apps and packaged as
libraries. `samples/studio` applies neither and has no Android target at all.

**E1.** Decide per sample: a real `com.android.application` module, or an explicit
`:samples:<name>:app` launcher that depends on the library. A launcher module is the smaller change
and keeps the sample reusable as a dependency.

**E2.** Give `samples/studio` an Android target if it should run on device; today it cannot.

**E3.** Verify with `installDebug` on a real device — the current suite cannot catch this class,
since library modules test fine.

### Phase F — cleanup, no user impact

- delete `surface`'s dead overloads (`AbsoluteScope`/`BoxScope` are `modifier = modifier`); fold
  `ColumnScope`/`RowScope` into one `UiScope.surface`
- generate the per-scope main/cross-axis rules from one source instead of mirroring them
- widen the sizing matrix to `row`, `box`, `scrollPanel`, and to `WrapContent` parents (the cell
  that reproduces the sentinel class)
- audit `ui-headless` (42 files) and `ui-showcase` (17); `ui-designsystem` is already audited and is
  **not** redundant — 2 existence-only files, 32 of 244 assertions misplaced geometry
- dark-mode `Danger` sits at 2.89:1 (`KNOWN_BELOW_FLOOR`); upstream pairs `text-white` with
  `dark:bg-destructive/60` and we paint full opacity. Needs `UiThemeValues` to expose darkness, or
  the dark palette to carry the alpha

## Method rules (earned, in the skills)

- **Exact values, never thresholds** — four sidebar tests passed on "more than 48px"
- **A matrix beats hand-picked cases** — 12 cells found 8 defects; 4 targeted tests found none
- **Instrument before hypothesising** — every guess this session was wrong; every measurement landed
- **One rule, one place** — duplication per scope/container caused four separate bugs
- **Loud over silent** — a dropped modifier must throw
- **Render it** — both previews added today found bugs no unit test saw
- **Baselines in their own commit** — never let a source change approve itself
