# ASL — Kotlin DSL generating WGSL, feeding the existing naga pipeline

Status: vision sketch, scoped down from two earlier drafts of this doc. First draft proposed
a from-scratch Kotlin-to-(GLSL+WGSL) compiler without checking Awake already had a
single-source pipeline. Second draft corrected that and proposed closing gaps without a
compiler. This version captures the actual current direction: **ASL emits WGSL text only,
then the existing `syncAwakeShaders`/naga pipeline does what it already does** — WGSL →
SPIR-V for Vulkan, WGSL copied as-is for WebGPU. ASL never touches GLSL or SPIR-V directly.
This is a real, meaningfully smaller scope than either earlier draft.

## Why targeting WGSL-only is the right cut

- **One output language, not two.** ASL only needs a WGSL emitter. No GLSL codegen, no
  SPIR-V emission, no maintaining two backend-specific code generators the way Kool's KSL
  does (per [2026-08-19-asl-single-source-shader-sketch.md]'s prior revision — Kool owns a
  generator per target language; naga already owns WGSL→SPIR-V, and Awake already depends on
  it via `awake.shader-pipeline-convention.gradle.kts`).
- **naga is ASL's type checker, not something ASL reimplements.** `validateAwakeShaders`
  already runs naga's validator over every `.wgsl` file. ASL doesn't need a full WGSL type
  system in Kotlin — it needs enough structure to emit *syntactically valid* WGSL text (right
  keywords, matched braces, correct attribute syntax); naga catches genuine type errors
  (mismatched vec sizes, wrong swizzle, etc.) at the same build step it already runs at
  today. ASL's own compile-time value is catching *structural* Kotlin-level mistakes
  (referencing a varying that was never declared, wrong binding index) before naga even runs
  — a smaller, cheaper target than full type-checking.
- **Zero new Gradle plumbing needed for the transpile step** — only for generation. ASL's
  Kotlin definitions produce `.wgsl` text; wherever that text lands inside
  `src/commonMain/shaders/` (the existing `sourceDirectory` `syncAwakeShaders`/
  `validateAwakeShaders` already scan), the rest of the pipeline runs completely unchanged.

## What ASL usage would look like — grounded against a real shader

`triangle.wgsl` (`samples/studio/src/commonMain/shaders/triangle.wgsl`, verbatim) is the
concrete target: ASL should be able to regenerate this exact shader (or a
structurally-equivalent one naga accepts identically) from a Kotlin definition.

```kotlin
// awake:asset:shaders:asl (new submodule) or inline in a sample's build script for a first spike
val TriangleShader = AslShader("triangle") {
    val uniforms = uniformBlock("Uniforms", group = 0, binding = 0) {
        field("mvp", GpuDataShape.Mat4)
        // vec4f, not vec3f -- avoids WGSL's implicit 16-byte alignment padding a vec3f field
        // would insert, matching the real file's own doc comment reasoning exactly.
        field("lightDirection", GpuDataShape.Vec4)
        field("lightColor", GpuDataShape.Vec4)
    }

    val vertexOut = varyings("VertexOutput") {
        builtinPosition()
        varying("color", GpuDataShape.Vec3, location = 0)
        varying("normal", GpuDataShape.Vec3, location = 1)
    }

    vertex(entryPoint = "vertexMain") {
        val inPosition = input("inPosition", GpuDataShape.Vec3, location = 0)
        val inNormal = input("inNormal", GpuDataShape.Vec3, location = 1)
        val inColor = input("inColor", GpuDataShape.Vec3, location = 2)

        vertexOut.position set uniforms.mvp * vec4(inPosition, 1f.lit)
        vertexOut.color set inColor
        vertexOut.normal set inNormal
    }

    val ambientStrength = const("AMBIENT_STRENGTH", 0.08f)

    fragment(entryPoint = "fragmentMain") {
        val n = let("n", normalize(vertexOut.normal))
        val l = let("l", normalize(uniforms.lightDirection.xyz))
        val diffuse = let("diffuse", max(dot(n, l), 0f.lit))
        val shade = let("shade", ambientStrength + (1f.lit - ambientStrength) * diffuse)
        val lit = let("lit", vertexOut.color * shade * uniforms.lightColor.xyz)
        val mapped = let("mapped", lit / (lit + vec3(1f.lit)))
        colorOutput(vec4(mapped, 1f.lit))
    }
}

// Build-time: TriangleShader.emitWgsl() -> String, written to a .wgsl file.
```

Note what's deliberately absent: no `attribute(VertexSemantic.Position)`-style binding to
`VertexFormat` the way the very first draft of this doc sketched. Kept as plain
`input("inPosition", ..., location = 0)` here because tying vertex *inputs* to the shared
`VertexFormat` type is a separate, valuable follow-up (closes the binding-drift gap from the
prior revision's step 1) but not required for the core ASL→WGSL→naga loop to work — worth
sequencing after the emitter itself is proven, not bundled into the first spike.

## Gradle wiring

```kotlin
// New task, generates into src/commonMain/shaders/ (or a generated/ subdirectory inside it --
// open question, see below) BEFORE naga ever runs.
val generateAslShaders = tasks.register<GenerateAslShadersTask>("generateAslShaders") {
    outputDirectory.set(layout.projectDirectory.dir("src/commonMain/shaders"))
}

// Only new line needed in awake.shader-pipeline-convention.gradle.kts:
validateAwakeShaders.configure { dependsOn(generateAslShaders) }
// syncAwakeShaders already depends on validateAwakeShaders -- transitively covered.
```

Everything downstream (`validateAwakeShaders`, `syncAwakeShaders`, the Vulkan SPIR-V output,
the WebGPU WGSL copy) needs **zero changes** — they already just scan `sourceDirectory` for
`.wgsl` files. This is the real payoff of targeting WGSL-only: the entire existing pipeline
becomes ASL's backend for free.

## Open questions

1. **Are generated `.wgsl` files committed, or purely build output?** Two options:
   - **Committed** (generator writes into `src/commonMain/shaders/`, output checked into git)
     — matches this repo's existing convention for the Vulkan bindings generator
     (`vulkan_generator` output lives in real committed `.kt` files under
     `bindings/src/commonMain/kotlin/...`, not `build/`). Reviewable diffs, but two sources
     of truth in the diff (Kotlin DSL change + regenerated WGSL change) on every commit.
   - **Build-only** (generator writes into `build/generated/shaders/`, that directory added
     to `sourceDirectory`'s scan, never committed) — one source of truth in git, but a shader
     bug is harder to spot by reading a diff (the generated WGSL never appears in review).
   Given this repo's own generator precedent leans committed, that's the likely answer, but
   worth confirming explicitly before building the task — changes the task's output-path
   design.
2. **Module placement** — `awake:asset:shaders:asl` (new submodule) vs. growing
   `awake:asset:shaders` in place. Lean toward a new submodule: the DSL/emitter is a
   meaningfully different capability (Kotlin AST → text) from the existing typed
   `VertexFormat`/`UniformLayout` contracts, and keeping it separate lets a consumer depend
   on the typed contracts without pulling in the code-generation machinery.
3. **Acceptance bar for a first spike** — regenerate `triangle.wgsl` byte-for-byte (or
   naga-validate-identical) as the proof case before attempting `lit_shadow.wgsl`'s greater
   complexity (PBR math, shadow sampling, more uniform fields). Don't generalize the emitter
   past what one real shader has proven it needs.

## Sequencing with the prior revision's smaller wins

This doesn't replace the three smaller, cheaper steps from the prior revision of this doc
(generate WGSL *boilerplate* from `VertexFormat`/`UniformLayout`, add a binding-drift
verifier, dedupe `triangle.wgsl`'s per-sample copies, migrate `awake:backend:vulkan`'s
built-in shaders onto the pipeline) — those remain valid, cheaper, and independently
shippable before or alongside a full ASL spike. ASL is the larger bet; the three smaller
steps de-risk and improve the existing pipeline regardless of whether ASL is ever built.

## Relationship to the RenderFeature plan

[2026-08-19-render-feature-strategy-plan.md](2026-08-19-render-feature-strategy-plan.md) is
unrelated to this at the design level — no dependency either direction, different layers (how
render passes are organized vs. how shader source is authored). The one contact point: each
`RenderFeature` sketched there is constructed from a `ShaderPair`/resource path, not shader
source directly — if ASL ships, it changes where those `.wgsl`/`.spv` files come from
(generated vs. hand-written), not how `RenderFeature` consumes them. Neither doc needs the
other to land first.
