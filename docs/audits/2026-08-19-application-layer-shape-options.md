# Application layer — full shape survey and options

Status: draft, not implemented. Scope widened from
[2026-08-19-render-feature-strategy-plan.md](2026-08-19-render-feature-strategy-plan.md)
(Renderer only) to the whole application stack: `WindowApplication` → `GameApplication` →
`Game` → `VulkanGameApplication`/`WebGpuGameApplication`.

## Layer-by-layer audit — keep what's already correct

| Class | Pattern already in place | Verdict |
|---|---|---|
| `WindowApplication` (`core/graphics`) | Bridge — abstraction (`create`/`update`/`resize`/`dispose`) decoupled from per-platform window impl | **Keep as-is.** Minimal, stable, no smell. |
| `GameApplication` (`engine/game`) | Template Method (`final` lifecycle calling abstract `createBackendResources`/`destroyBackend`) + Mediator (keeps window/`Renderer`/`Game` from referencing each other) | **Keep as-is.** Already documented in `skills/awake-render-pipeline/SKILL.md` §4. No god-class symptoms — 113 lines, single responsibility. |
| `Game` (`engine/game`) | Strategy — injected behavior object, not a base class | **Keep as-is.** 18 lines, a pure contract. |

None of these three need a shape change. Don't refactor a class just because it's nearby —
only `VulkanGameApplication`/`WebGpuGameApplication` show a real symptom.

## The actual problem: duplicated telescoping constructors

`VulkanGameApplication` ([VulkanGameApplication.kt:43-132](../../awake/backend/vulkan/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/vulkan/application/VulkanGameApplication.kt#L43-L132))
and `WebGpuGameApplication` ([WebGpuGameApplication.kt:31-96](../../awake/backend/webgpu/src/wasmJsMain/kotlin/io/github/ronjunevaldoz/awake/webgpu/application/WebGpuGameApplication.kt#L31-L96))
each take the same 9 constructor params:

```
vertexShaderResourcePath, fragmentShaderResourcePath, vertexFormat, game,
vertexShaderEntryPoint, fragmentShaderEntryPoint, additionalPipelines,
wireframeSupport, instancedShaderSet, skinnedInstancedShaderSet,
skyboxShaderSet, particleShaderSet  [+ shadowShaderSet, Vulkan only]
```

Every WebGPU param's doc comment literally says "mirrors `VulkanGameApplication.X`". This is
hand-synchronized duplication: adding one new opt-in feature (say, post-process bloom) means
editing both constructors, both secondary `constructor(shaderSet: GameShaderSet, ...)`
overloads, and remembering to keep every doc comment in sync across two files in two
different backend modules. Real cost already paid twice — `shadowShaderSet` exists only on
the Vulkan side today, so the two classes have already begun drifting.

This is the same root issue the `RenderFeature` plan is fixing on the `Renderer` side, one
layer up: a flat list of nullable params standing in for what should be a data structure.

## Options (pick one — not additive)

### Option A — shared `GameShaderSetSpec` data class in `engine/game`

First draft of this option used one fixed named field per feature (`shadow`, `instanced`,
`skybox`, `particle`...). That's a closed set — Open/Closed–violating: adding a genuinely new
opt-in feature (say, GPU-driven decals) still means editing this data class, same cost as
editing the constructor it replaces, just in one place instead of two. Real fix: make the
optional-feature slice of the spec an **open registry**, keyed by an un-sealed marker
interface any module (including a future third-party consumer, matching
`render-extensibility.md`'s stated "consumer builds their own content" library goal) can add
a case to without touching `GameShaderSetSpec` itself:

```kotlin
// engine/game/GameShaderSetSpec.kt (new file)

/** Open on purpose -- NOT sealed. A sealed interface would still be a closed set (only this
 * module could add cases), which is exactly the Option A first-draft mistake one level down.
 * Built-in keys are `object`s; a consumer module can define its own the same way. */
interface RenderFeatureKey

object ShadowFeatureKey : RenderFeatureKey
object InstancedFeatureKey : RenderFeatureKey
object SkinnedInstancedFeatureKey : RenderFeatureKey
object SkyboxFeatureKey : RenderFeatureKey
object ParticleFeatureKey : RenderFeatureKey

data class GameShaderSetSpec(
    val primary: GameShaderSet,
    val vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
    val wireframeSupport: Boolean = false,
    val additionalPipelines: Map<VertexFormat, GameShaderSet> = emptyMap(),
    /** A new opt-in feature is a new `RenderFeatureKey` object, used at the call site --
     * zero edits to this class. `additionalPipelines`/`wireframeSupport`/`vertexFormat` stay
     * separate named fields because they aren't opt-in *features* in the same sense (they
     * shape the primary pipeline itself, not add a new pass) -- collapsing them into the
     * registry too would blur that distinction for no real gain. */
    val features: Map<RenderFeatureKey, GameShaderSet> = emptyMap(),
)

open class VulkanGameApplication(
    spec: GameShaderSetSpec,
    game: Game,
) : GameApplication(spec.primary.vulkan.vertexResourcePath, ..., spec.vertexFormat, game) {
    // createBackendResources reads spec.features[ShadowFeatureKey], etc. -- interprets known
    // built-in keys into concrete RenderPipeline/RenderFeature objects (see the Renderer
    // plan's ShadowFeature/SkyboxRenderFeature). shadow being Vulkan-only is now just "the
    // WebGPU subclass never looks up ShadowFeatureKey" -- no separate type needed.
}
```

- **Pro:** one definition, shared by both backends, and now genuinely open — a new feature
  never requires editing `GameShaderSetSpec`. `shadow` being Vulkan-only stops being a type
  problem: WebGPU's `createBackendResources` simply never reads `ShadowFeatureKey` from the
  map, no separate spec type or footgun field.
- **Con — stated honestly, not solved here:** this only removes the *shape* problem (closed
  set of named params). It does NOT let a third-party consumer supply their own GPU pipeline
  construction logic for a custom key — `createBackendResources` still needs an explicit
  `when`/lookup branch per key it knows how to interpret into real Vulkan objects. True
  "bring your own render feature" extensibility (a consumer's `RenderFeatureKey` producing a
  consumer-authored `RenderPipeline`) is a larger scope than this doc — flagged as future
  work, not blocked by anything decided here.

### Option B — builder/DSL (matches `kmp-api-mimicry`'s slot-builder shape)

```kotlin
val app = vulkanGameApplication(game = MyGame()) {
    shaders(vertexShaderResourcePath = "...", fragmentShaderResourcePath = "...")
    vertexFormat = VertexFormat.PositionColorUv
    wireframe()
    instanced(instancedShaderSet)
    skybox(skyboxShaderSet)
    shadow(shadowShaderSet)   // Vulkan-only builder method, doesn't exist on the WebGPU builder
}
```

- **Pro:** most discoverable API (autocomplete-driven), naturally supports Vulkan-only
  builder methods without polluting a shared data class, reads well for consumers.
  Precedent already exists in this repo's DSL conventions (`scene { }`, `Modifier()`).
- **Con:** most implementation work — needs a real builder class per backend, plus the
  `@DslMarker` discipline `awake-ecs-authoring`'s skill already flags as load-bearing
  (nested builder receiver leakage bugs have happened here before). Higher risk for a config
  surface that's otherwise simple key-value opt-ins.

### Option C — leave the constructor, extract only the duplication

Smallest diff: keep both classes' flat constructors exactly as they are, but move the
doc-comment-duplicated *reasoning* (why each param defaults null, why shadow needs matching
vertex attributes, etc.) into one shared doc reference both classes link to, instead of
repeating full paragraphs. Reduces the maintenance burden of keeping prose in sync without
touching the actual API shape or call sites.

- **Pro:** zero consumer-facing breakage, zero risk, can ship today.
- **Con:** does not fix the actual problem — adding a 10th opt-in feature is still a
  two-file, two-constructor-overload edit. This is a docs patch, not an architecture fix.

## Recommendation

**Option A** — shared `GameShaderSetSpec` in `engine/game`, with the optional-feature slice
as the open `RenderFeatureKey`-keyed registry above (not fixed named fields). Reasoning:

- Matches this round's ask (best pattern, future-proof, real separation of concerns) better
  than Option C, without Option B's `@DslMarker`-risk implementation cost for what is
  fundamentally a plain data-holding concern, not a nested-scope-building one.
- Composes directly with the `RenderFeature` plan: once that lands, `VulkanGameApplication`'s
  `createBackendResources` turns `GameShaderSetSpec`'s optional fields into the
  `List<RenderFeature>` it already needs to build (§4 of that plan) — same data, one
  interpretation step, instead of two independent flat-param lists feeding two independent
  ad-hoc `buildList { }` blocks as sketched there today.
- Directly kills the measured duplication: every "mirrors `VulkanGameApplication.X`" doc
  comment in `WebGpuGameApplication.kt` goes away, replaced by one doc comment on the shared
  field.

## Sequencing with the Renderer plan

1. `RenderFeature`/`RenderFrameContext` port (already planned) — ships first, backend-local,
   lowest risk.
2. `GameShaderSetSpec` (this doc, Option A) — ships second, consumes the vocabulary
   `RenderFeature` introduced (`ShadowFeature`, `SkyboxRenderFeature`, etc.) as the thing
   `createBackendResources` builds from the spec.
3. WebGPU mirrors both, once Vulkan proves out — same deferred-parity stance as open
   question #1 in the Renderer plan.

## Open questions — resolved

1. **`engine/game`, right next to `GameShaderSet`.** Checked:
   `GameShaderSet` is defined in
   [ShaderProgramResources.kt](../../awake/engine/game/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/engine/game/ShaderProgramResources.kt),
   inside `engine/game`, which both `awake:backend:vulkan` and `awake:backend:webgpu` already
   depend on (they import `GameShaderSet` today). No new module needed —
   `GameShaderSetSpec.kt` belongs in that same package, next to the type it's built from. A
   new shared module would add a dependency edge that doesn't need to exist.

2. **Collapses — and further than first proposed.** `VulkanGameApplication` has *two*
   constructors today: a primary one taking raw `vertexShaderResourcePath`/
   `fragmentShaderResourcePath` strings, and a secondary `constructor(shaderSet: GameShaderSet, ...)`
   that unpacks a `GameShaderSet` into those same raw paths before delegating. Once
   `GameShaderSetSpec.primary` is typed as `GameShaderSet` (not raw paths), the *raw-path
   primary constructor itself* has no reason to exist either — every caller already has a
   `GameShaderSet` (that's what `shaderSet.vulkan.vertexResourcePath` reads from), so keeping
   a raw-paths entry point is a second way to do the same thing with more room for the two
   paths and the format to fall out of sync. Recommendation: one constructor,
   `VulkanGameApplication(spec: GameShaderSetSpec, game: Game)`, full stop — no overload.
   `GameShaderSetSpec(primary = shaderSet)` (letting every other field default) already *is*
   the convenience shortcut the secondary constructor exists for today; a second constructor
   duplicating that convenience would just be the telescoping-constructor problem re-appearing
   one level up.
