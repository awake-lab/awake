# D10 — jni-binding-generator de-risk findings (2026-07-07)

Phase 1a of [MVP_PLAN.md](../MVP_PLAN.md) called for a week-one de-risk: run
[jni-binding-generator](https://github.com/ronjunevaldoz/jni-binding-generator) against
`VkGraphicsPipelineCreateInfo` (the nastiest nested Vulkan struct) before committing to it
as the replacement for the bespoke `awake-vulkan-generator`. This documents what was found.

## What the tool actually is

jni-binding-generator parses Kotlin `external fun` **function signatures** and generates the
JNI marshalling for the *parameters and return value* — primitives, strings, primitive/boxed
arrays, `List`/`Set`/`Map`, and enums (via ordinal). Its own type matrix
(`jni-binding-generator/docs/type-support-matrix.md`) confirms this is the complete list —
there is no entry, in code or docs, for marshalling a Kotlin class/struct's *fields*.

`awake-vulkan-generator` solves a different, harder problem: given a Vulkan struct
(`VkInstanceCreateInfo`, `VkGraphicsPipelineCreateInfo`, etc. — dozens of fields, nested
structs, arrays-as-pointer-with-count-field, optional/nullable pointers), it emits a C++
**Accessor** (Kotlin object → C struct) and **Mutator** (C struct → Kotlin object) class per
type. That is field-by-field struct marshalling, not function-call marshalling.

## Reproduced failure modes

Tested against the real `Vulkan.kt` signatures (both the `commonMain expect` and the
`androidMain actual external fun` declarations).

### 1. Array-of-struct params fail loudly (safe, but blocking)

```kotlin
external fun vkCreateGraphicsPipelines(
    device: Long, pipelineCache: Long,
    createInfos: Array<VkGraphicsPipelineCreateInfo>
): LongArray
```

```
Error: unrecognized parameter type 'Array<VkGraphicsPipelineCreateInfo>'.
Add a mapping for 'Array<VkGraphicsPipelineCreateInfo>' to TYPE_MAP in jni-binding-generator.py.
```

This is the *good* outcome — it refuses to guess.

### 2. Bare struct params fail silently (dangerous)

```kotlin
external fun vkCreateShaderModule(
    device: Long,
    createInfo: VkShaderModuleCreateInfo
): Long
```

Generates, with no warning or error:

```cpp
extern "C" JNIEXPORT jlong JNICALL
Java_..._vkCreateShaderModule(JNIEnv* env, jclass clazz, jlong device, jobject createInfo) {
    void* device_ptr = reinterpret_cast<void*>(device);
    int32_t createInfo_val = enum_ordinal(env, createInfo);   // <-- wrong: not an enum
    if (env->ExceptionCheck()) return 0;
    ...
}
```

Root cause: `_types.py`'s enum fallback is a **naive regex heuristic**
(`_ENUM_RE = re.compile(r"^[A-Z][A-Za-z0-9_]*$")`, "any capitalized identifier with no
generics") — not real enum detection. Any unrecognized simple type name, including a struct,
matches it. The generated code compiles cleanly (calls a real helper, `enum_ordinal`, with
the right JNI signature) but is semantically wrong: it would call `.ordinal()`-equivalent
reflection on an object with no such method, at best throwing at runtime, at worst reading
garbage if the object happens to have a compatible method by coincidence.

## Blast radius (measured against the current 58-function `Vulkan.kt` API)

| Category | Count | Outcome if run naively today |
|---|---:|---|
| Bare `Vk*` struct param (real structs) | 18 | **Silent miscompile** (`enum_ordinal` on a struct) |
| Bare `Vk*` param that's a genuine enum | 2 | Correctly handled (coincidentally, by the same heuristic) |
| `Array<Vk*>` struct param | 4 | Fails loudly (blocking, not dangerous) |
| Remaining functions | 34 | Only primitive/handle (`Long`) params — fine today |

20 of 58 functions (34%) touch a `Vk*`-typed parameter; 18 of those would silently miscompile.
And this is only today's ~58-function surface — Phase 1d's planned buffer/memory/descriptor
API additions are almost entirely struct-parameter-heavy (`VkBufferCreateInfo`,
`VkMemoryAllocateInfo`, `VkDescriptorSetLayoutCreateInfo`, ...), so the fraction only grows.

## Why this matters for the Phase 1a plan as written

The original plan assumed extending jni-binding-generator's type map for "nested struct
support" was comparable in scope to adding one more entry (the way its `jni-add-type` skill
handles, say, adding `Char` support). It is not: struct support requires the tool to grow an
entirely new capability — recursive field marshalling, nested-struct composition,
array-as-pointer-with-count-field (`@VkArray("stageCount")`), optional-pointer-via-array
(`@VkPointer`), and handle-typed fields (`@VkHandleRef`) — which is most of what
`awake-vulkan-generator`'s `CreateVulkanAccessor`/`CreateVulkanMutator`/`VulkanCppBuilder`
already implements, purpose-built for exactly this Vulkan struct shape.

## Options (see D10 in MVP_PLAN.md for the recommendation ask)

**(a) Extend jni-binding-generator with real struct support.** Gains: one tool, its Gradle
integration, drift detection, generated test files, and community reuse if ever open-sourced
further. Cost: multi-week effort essentially re-deriving `awake-vulkan-generator`'s struct
model inside a differently-shaped codebase (function-signature-first vs. struct-first).

**(b) Keep `awake-vulkan-generator` for structs; jni-binding-generator for what it's actually
for.** `awake-vulkan-generator` already works today (it produced the ~100 Accessor/Mutator
files backing the current Android build). Modernize it only as needed (Kotlin 2.x reflection
API compat — already done in the toolchain migration). Use jni-binding-generator where its
real strength applies: simple, non-struct JNI surfaces — the clearest fit being the Phase 8
physics facade (`~20 hand-designed functions`, deliberately primitive-typed per the D5
coarse-grained-binding design, `world.step(dt): Unit`, batched buffer reads — no struct
params by design).

**(c) Hybrid.** jni-binding-generator handles leaf-level primitive/enum/array fields within
a struct; an outer (hand-written or lightly templated) layer composes the struct
Accessor/Mutator by calling into per-field generated helpers. Reduces boilerplate without
requiring the tool to understand Vulkan's nested-struct shape end-to-end. More design work
than (b), less than (a).

No default recommendation is baked in above — this determines multiple weeks of Phase 1a
engineering direction and should be a deliberate call, not an assumed one.
