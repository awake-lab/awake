# D10 — jni-binding-generator de-risk findings (2026-07-07)

Phase 1a of [MVP_PLAN.md](../MVP_PLAN.md) called for a week-one de-risk: run
[jni-binding-generator](https://github.com/ronjunevaldoz/jni-binding-generator) against
`VkGraphicsPipelineCreateInfo` (the nastiest nested Vulkan struct) before committing to it
as the replacement for the bespoke `awake-vulkan-generator`. This documents what was found.

**Status: resolved and wired into the real build (round 4, v1.6.10, 2026-07-08).** Round 1
(v1.6.8) added struct marshalling and fixed the silent enum-guessing heuristic; round 2
re-verification found three further gaps (annotation stripping, enum-typed struct fields,
array-of-struct fields), fixed in round 3 (v1.6.9). Round 4 actually wired the tool into the
Gradle/CMake build for a genuinely new function (`vkCreateBuffer`/`vkDestroyBuffer`), which
found two more generator gaps (`typealias` resolution, enum-field package correctness — both
fixed in v1.6.10) plus several Awake-side wiring issues (CMake path scoping, the
`--check`-vs-hand-edit incompatibility, `expect`/`actual` completeness). See "Round 4" below
for the full account. Re-running the original de-risk repro recovers **all 18 real fields**
of `VkGraphicsPipelineCreateInfo` (previously 5), and all **58 real functions** in
`androidMain/Vulkan.kt` parse correctly with annotations stripped and no truncation. The
tool's own test suite (267 tests), the real JNI-header compile-check integration test, and
drift checks against all 3 bundled examples all pass. See "Round 3" below for the fix
details. **D10 is now closed: proceed with jni-binding-generator for Phase 1a**, option (a)
from the original three (the gap turned out to be closeable, not a from-scratch rebuild).

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

## Round 2 — re-verification against v1.6.8 (commit `615b04d`, 2026-07-08)

The tool's changelog for this commit claims both requested parts landed: the silent enum
fallback fixed, and generic struct marshalling added (flat structs, nested structs,
count-paired arrays via `--struct-config`, nullable nested structs). Re-tested directly
against the real `awake-vulkan` source rather than trusting the changelog.

### What genuinely works now

- **Part 1 fix confirmed.** Unknown capitalized types now require an actual `enum class`
  declaration (scanned across the source set) to be treated as an enum; anything else raises
  `UnknownTypeError` instead of silently guessing.
- **Core struct recursion is solid.** A flat struct, a struct containing another struct, and
  a struct containing a *nullable* nested struct (`Inner?` → `std::optional<JNI_Inner>`) all
  generate correct, sensible C++ (`extract_X`/`make_X` pairs, topologically ordered so
  dependencies are emitted first). Verified with hand-written test structs (no domain
  annotations).

### Three new gaps found testing against the real code

**1. Annotation stripping is absent — severe, affects most real fields and params.**
Confirmed empirically: `collect_struct_types()` run against the real
`VkGraphicsPipelineCreateInfo.kt` (15 constructor properties) recovers only **5** fields —
every property with a leading `@field:VkArray(...)`, `@VkPointer`, or `@field:VkHandleRef(...)`
annotation (same-line or own-line) is silently dropped, because `_try_parse_prop()` requires
the property chunk to literally start with `"val "`/`"var "`. This is **not** limited to the
new struct path — the pre-existing function-parameter splitter has the same gap plus a second
bug: `_split_params()` only tracks `<>` depth, not `()` depth, so a same-line annotation with
parenthesized args (`@VkHandleRef("VkDevice") device: Long`, the dominant style in this
codebase's `androidMain actual external fun` declarations) crashes the parser on the comma
inside the annotation's own argument list (`could not parse parameter '@VkHandleRef("VkDevice"'`).
A same-line annotation *without* parens (bare `@VkPointer`) doesn't even error — it gets
absorbed into the parameter name, emitting invalid generated C++ (`jlong @VkPointer device`)
that only a downstream C++ compiler would catch. Net effect: pointing the tool at this
codebase's real Kotlin as-is either drops most struct fields silently, crashes on most
annotated function params, or emits broken C++ — depending on annotation style.

**2. Enum-typed struct fields unsupported.** The new struct generator
(`_struct_gen.py`) has its own separate field-type table that doesn't reuse `_types.py`'s
enum-aware resolution. A field like `sType: VkStructureType` (a real enum, and the first
field of nearly every Vulkan `*CreateInfo` struct) falls through to the "unsupported field"
path, which emits a field descriptor string with a comment literally baked into the runtime
JNI type-signature (`env->GetFieldID(cls, "kind", "Ljava/lang/Object;  /* TODO: ... */")`) —
not valid, and would fail to resolve the field / likely throw at runtime.

**3. Array-of-struct fields unsupported, `--struct-config` or not.** This is the single most
common real-Vulkan-struct shape (`pStages: Array<VkPipelineShaderStageCreateInfo>`,
`pSubmits: Array<VkSubmitInfo>`, etc.) and was explicitly requested (count-paired arrays).
Verified: `--struct-config`'s `count_field` hint only adds a documentation *comment* to
already-supported **primitive** arrays (`IntArray`, etc.). There is no code path in
`_struct_gen.py`'s field-type resolution for `Array<StructName>` at all — it falls through to
the same "unsupported field type" stub as gap 2.

### Updated verdict

The tool is meaningfully better (Part 1 is a real fix; the struct-recursion core is correctly
designed) but **still not usable against this codebase's real structs and functions**, because
gap 1 alone would corrupt or crash on the majority of real signatures, and gaps 2–3 remove two
of the three struct shapes Vulkan needs most (enum fields, array-of-struct fields). This
doesn't change the three options above, but does inform them: option (a) "extend the tool"
is now a *smaller* remaining gap than at round 1 (annotation stripping + two field-type
extensions, not "add struct support from zero") — the calculus may be shifting.

## Round 3 — fixed directly in jni-binding-generator (v1.6.9, commit `b19d555`, 2026-07-08)

All three round-2 gaps were fixed in the jni-binding-generator repo itself (generically —
no Vulkan-specific naming anywhere in the fix), verified there, and re-verified here against
the real Awake source.

**Root cause of the annotation bug was one level deeper than round 2's diagnosis.** The
actual break wasn't (only) `_split_params`'s comma-handling — the whole-function matching
regex (`_EXTERNAL_FUN_RE`) used a non-greedy `\((.*?)\)` to capture the parameter list, which
truncates at the **first** `)` in the source. Any annotation with parenthesized args
(`@VkHandleRef("VkDevice")`) appearing before the parameter list's real closing paren broke
the capture — explaining the exact garbled error seen in round 2
(`'@VkHandleRef("VkDevice"'`, missing its own closing paren). Fixed by locating
`external fun NAME(` via regex, then finding the true closing paren via paren-balancing
(the same technique the struct-constructor parser already used), before running comma-split
and annotation-stripping on the correctly-bounded parameter text. A new shared
`_strip_leading_annotations()` helper (handling `@Foo`, `@Foo(args)`, `@site:Foo(args)`,
stacked, same-line or own-line) is now used by both the struct-property parser and the
function-parameter parser, so this class of bug can't reappear in one path after being fixed
in the other.

Enum-typed struct fields and `Array<StructName>` fields were both wired into
`_struct_gen.py`'s field-type resolution (previously each fell through to an "unsupported
field type" stub). A new `enum_from_ordinal()` helper was added to `jni-utils.h`, mirroring
the existing `values()`/`GetObjectArrayElement` pattern already used for enum function
returns. Fixing this round also surfaced (via this round's own compile-check pass) and fixed
one more pre-existing bug: non-nullable nested-struct fields in `make_<Struct>` called
`.has_value()` on a plain (non-`std::optional`) value, which would not compile.

**Re-verification against the real Awake source:**
- `VkGraphicsPipelineCreateInfo` (18 real constructor properties): **all 18 recovered**
  (was 5 in round 2).
- `androidMain/Vulkan.kt` (58 real `actual external fun` declarations, most annotated):
  **all 58 parse correctly**, no truncation, no crashes, no absorbed annotations.
- jni-binding-generator's own suite: 259 tests pass (245 existing + 14 new covering exactly
  these three gaps), zero regressions; the real compile-check integration test (against
  actual JDK `jni.h` headers) and drift checks against all 3 bundled examples pass; `ruff
  check`/`format --check` clean.
- Additionally hand-verified: generated C++ covering a flat struct, nested struct, nullable
  enum field, nullable nested-struct field, annotated array-of-struct field, and annotated
  function params all compiled cleanly with `clang++ -std=c++17 -fsyntax-only` against real
  JDK headers.

**Decision: D10 is closed.** jni-binding-generator can now be pointed at this codebase's real
Vulkan structs and functions.

## Round 4 — actually wiring it into the Gradle/CMake build (2026-07-08)

Re-verifying against copied-out source is not the same as wiring the tool into the real
build for a genuinely new function. Doing that (`vkCreateBuffer`/`vkDestroyBuffer`, backed
by a new `VkBufferCreateInfo` struct) surfaced several more real issues — two of them new
generator gaps, fixed the same way as rounds 2–3 (directly in the vendored tool, generically).

**Structural decision: new functions go in a separate `...vulkan.gen` package, not the
legacy `Vulkan` object.** `--kotlin-source` must point at the whole module (the struct/enum
pre-pass needs full visibility), but the legacy object's 58 functions include shapes
jni-binding-generator can't generate at the *function* level yet (e.g.
`Array<VkLayerProperties>` as a return type — only supported as a struct *field*, which is
what rounds 2–3 actually added). `--package-filter` scopes generation to the new package
while the pre-pass still sees everything, so the legacy object is left alone entirely.

**Gap 4 — `typealias` was never resolved.** `VkBufferCreateInfo.size: VkDeviceSize` (where
`typealias VkDeviceSize = Long`) fell through to "unsupported field type", identically for
`VkBufferUsageFlags`/`VkBufferCreateFlags` (aliases of `VkFlags = Int`). The generator
worked purely off the literal type name as written, with no concept of `typealias` at all.
Fixed generically in the vendored tool: `collect_typealiases()` (driver pre-pass) +
`resolve_typealias()` (chain-following: `VkBufferUsageFlags -> VkFlags -> Int`), applied
before every type lookup for both function params/returns and struct fields.

**Gap 5 — enum struct fields assumed the wrong package.** Round 3 documented this as a
known limitation ("assumed to be in the same package as the struct that contains it");
wiring against the real codebase showed it's not an edge case here — it's the norm (enums
live in `enums/`, structs in `models/info/`). `VkBufferCreateInfo.sharingMode: VkSharingMode`
was marshalled with `Lio/github/ronjunevaldoz/awake/vulkan/models/info/VkSharingMode;` — the
struct's package, not the enum's real one
(`io/github/ronjunevaldoz/awake/vulkan/enums/VkSharingMode`). Fixed generically:
`collect_enum_packages()` tracks each enum's actual declaring package (mirroring how
`KotlinStruct.package` already works); the struct generator uses it, falling back to the
referencing struct's package only if genuinely unknown. Both gaps fixed and verified
end-to-end (267 tests, compile-check, all example drift checks) before re-vendoring; see
jni-binding-generator's own CHANGELOG v1.6.10 for the fix in isolation.

**Non-generator issues found while wiring (Awake-side, not the tool's):**

- **CMake path mismatch.** `:awake-vulkan:android-native`'s `externalNativeBuild.cmake.path`
  points at `../src/main/cpp/CMakeLists.txt` (i.e. the *sibling* `awake-vulkan/src/`
  directory, not a subdirectory of `android-native/`) — a consequence of the AGP 9 module
  split done in the toolchain migration (see the AGP9/Kotlin2.4 migration lessons file).
  The Gradle output directory for generated JNI code must match that same root
  (`awake-vulkan/src/main/cpp/generated/`), not `android-native/src/main/cpp/generated/` —
  the latter is a path CMake never looks at. Easy to get wrong since both look plausible.
- **`--check` cannot be an automatic build gate once hand-edited.** The generated file's
  JNI bodies are meant to be hand-filled with real Vulkan calls (matching
  jni-binding-generator's own bundled examples, none of which show a filled-in body
  either — this is intended usage, not a workaround). But `--check` is a byte-for-byte
  diff against a fresh generation, so once any hand-edit exists it fails forever, with no
  way to distinguish "the Kotlin signature actually changed" from "the TODO body was
  intentionally filled in". Resolution: `checkJniBindings` is kept as a manual diagnostic
  task only, not wired to `dependsOn` the native build; the real safety net for signature
  drift is the C++ compiler itself — an incompatible struct-shape change fails to compile
  against the stale hand-written body, pointing at the exact mismatch.
- **`expect object` needs an `actual` in every source set.** Adding `VulkanBuffers` as
  `expect object` in `commonMain` immediately broke `compileKotlinDesktop` (missing
  actual) even though only `androidMain` had real work to do. Added `TODO()`-stub actuals
  for `desktopMain`/`iosMain` matching the legacy `Vulkan` object's own convention for
  not-yet-implemented platforms.
- **Enum-marshalling ordinal-vs-value hazard (Awake-specific, not a generator bug):**
  jni-binding-generator marshals confirmed enums via **ordinal position** — correct
  behavior for the tool, since it has no way to know an enum carries a separate
  `.value: Int`. But this codebase's `VkStructureType` has ordinal == value only up to
  entry 48 (`VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SUBGROUP_PROPERTIES`, ordinal 49, has value
  `1000094000`) — extension types break the correspondence. Auto-marshalling
  `VkStructureType` (or `VkFormat`, most `*EXT`/`*KHR` enums) this way would silently write
  the wrong structure-type tag. `VkBufferCreateInfo` was deliberately designed without an
  `sType`/`pNext` field (hardcoded in the hand-written native body instead — it's a
  compile-time constant per struct type anyway) and uses `VkSharingMode` (verified
  ordinal == value for both its entries, and the Vulkan spec has never extended it) as the
  only real enum field. See the Phase 1d hazard note in [MVP_PLAN.md](../MVP_PLAN.md) for
  the concrete rule going forward.

**Verification:** Android demo APK builds clean (generated file compiles for both
arm64-v8a/x86_64, links against the legacy 58-function native code with no symbol
conflicts); desktop jar, legacy generator module, and detekt all still pass.

## Round 5 — real-device runtime confirmation (2026-07-08)

Compile/link success isn't proof the generated marshalling is semantically correct at
runtime. Verified with a temporary call inserted right after `vkCreateDevice` in the demo
app (`VulkanBuffers.vkCreateBuffer(device, VkBufferCreateInfo(size = 256L, usage =
VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, sharingMode = VK_SHARING_MODE_EXCLUSIVE))`, followed by
`vkDestroyBuffer`), run on two targets:

- **Android emulator (`Medium_Phone_API_36.1`):** blocked before reaching the call at all —
  `pickPhysicalDevice` throws `"Cannot find suitable gpu!"` (`vkEnumeratePhysicalDevices`
  finds zero devices). Confirmed this is a **pre-existing environment limitation, not
  related to this work** — the original triangle demo fails at the identical line
  regardless of any of these changes; this specific emulator instance has no working
  Vulkan ICD/software fallback registered.
- **Real device (Samsung Galaxy S25 Ultra, `SM-S938B`, Adreno GPU, wireless-debugging-
  enabled, `adb`-connected):** succeeded completely. Logcat:
  ```
  AdrenoVK-0: Application Name    : Awake Vulkan - Application
  System.out: AWAKE_VERIFY vkCreateBuffer -> -422876570089160701 (nonzero = success)
  System.out: AWAKE_VERIFY vkDestroyBuffer completed without throwing
  ```
  A genuine non-zero `VkBuffer` handle from a real Adreno driver, cleanly destroyed. This
  is definitive confirmation — not just compile/link success — that the
  jni-binding-generator-generated marshalling (struct field extraction, the
  `VkSharingMode` enum-via-ordinal path, the generated JNI entry point) and the
  hand-written native `vkCreateBuffer`/`vkDestroyBuffer` calls are correct end-to-end
  against a real Vulkan driver. The verification snippet was removed after confirming
  (temporary, never committed).

**D10 and the Phase 1a `vkCreateBuffer` proof-of-concept are now fully closed** — both at
the generator level (rounds 1–4) and at the real-device runtime level (round 5).

## Round 6 — vertex-buffer-driven triangle, Phase 1 exit criteria (2026-07-08)

Extended `VulkanBuffers` with `vkCmdBindVertexBuffers(commandBuffer, firstBinding, buffers:
LongArray, offsets: LongArray)` — `bindingCount` is implicit from `buffers.size`, so no
separate count param is exposed. Regenerating for this addition reproduced the
previously-documented "regeneration wipes hand-written bodies" risk (Round 4/5 lesson);
this time the re-merge was done with a small Python script that extracts each `extern "C"
JNIEXPORT ... Java_...` function block by regex from a pre-regeneration backup and splices
it back into the freshly generated file, rather than hand re-typing all 8 previous bodies.
It correctly matched and restored all 8.

**New pitfall found by this round:** that merge script only replaces function *bodies* — it
does not diff or restore the file's top-of-file `#include` block. Regeneration had also
dropped `#include <vulkan/vulkan.h>`, `#include <cstring>`, and `#include
"exception_utils.h"` (present in the hand-edited version, absent from the tool's raw
per-function template, which only assumes `jni.h`/STL headers). This surfaced immediately
and loudly as a C++ compile error (`unknown type name 'VkBuffer'`, `undeclared identifier
'exception_utils'`, etc.) rather than silently — so it's a build-time-caught issue, not a
runtime-hazard one, but still worth the process note: **a regenerate-then-remerge pass must
re-diff the include block, not just function bodies.**

**Proved the actual Phase 1 exit criteria — a real vertex-buffer-driven triangle, not just
an isolated function call.** Previously, `vkCreateBuffer` etc. were verified with a
throwaway buffer never actually used for rendering. This round rewired the demo's real
triangle pipeline end-to-end:
- `triangle.vert` changed from two hardcoded `vec2[3]`/`vec3[3]` arrays indexed by
  `gl_VertexIndex` to `layout(location=0) in vec2 inPosition` / `layout(location=1) in vec3
  inColor`.
- `VulkanApplication.kt`'s `VkPipelineVertexInputStateCreateInfo` (previously all defaults —
  no bindings/attributes at all, only possible because the shader ignored them) now
  describes one binding (stride 20 bytes = 5 floats) and two attributes
  (`VK_FORMAT_R32G32_SFLOAT` @ offset 0 for position, `VK_FORMAT_R32G32B32_SFLOAT` @ offset
  8 for color).
- A real vertex buffer (3 interleaved position+color vertices) is created once in
  `createVertexBuffer()` at setup time via the full `VulkanBuffers` chain
  (`vkCreateBuffer` → `vkGetBufferMemoryRequirements` → `findMemoryType` →
  `vkAllocateMemory` → `vkBindBufferMemory` → `writeBufferMemoryFloats`), and destroyed in
  `destroy()` via `vkDestroyBuffer`/`vkFreeMemory`.
- `recordCommandBuffer()` now calls `VulkanBuffers.vkCmdBindVertexBuffers(commandBuffer, 0,
  longArrayOf(vertexBuffer), longArrayOf(0L))` every frame, immediately before the existing
  `Vulkan.vkCmdDraw(commandBuffer, 3, 1, 0, 0)`.

**Confirmed on real hardware** (Samsung Galaxy S25 Ultra, Adreno GPU): installed and
launched the rebuilt APK, no crash in logcat, and a device screenshot
(`adb exec-out screencap -p`) shows the same red/green/blue triangle rendering correctly —
now genuinely sourced from a GPU-resident vertex buffer rather than baked into the shader.
This closes Phase 1a/1d's `vkCmdBindVertexBuffers` item and the "vertex-buffer triangle on
Android" milestone from the Phase 1 exit criteria.

## Round 7 — descriptor sets / uniform buffer, and two build-hygiene bugs found while verifying (2026-07-08)

Added `VulkanDescriptors` (new object, same `.gen` package/generator pipeline as
`VulkanBuffers`): `vkCreateDescriptorSetLayout`, `vkDestroyDescriptorSetLayout`,
`vkCreateDescriptorPool`, `vkDestroyDescriptorPool`, `vkAllocateDescriptorSet` (single set),
`vkUpdateDescriptorSetBuffer` (single buffer-type write), `vkCmdBindDescriptorSet`. New
structs: `VkDescriptorSetLayoutBinding`, `VkDescriptorSetLayoutCreateInfo` (with
`pBindings: Array<VkDescriptorSetLayoutBinding>`), `VkDescriptorPoolSize`,
`VkDescriptorPoolCreateInfo` (with `pPoolSizes: Array<VkDescriptorPoolSize>`),
`VkDescriptorBufferInfo`. `descriptorType`/`stageFlags` modeled as plain `Int` (new
`VkDescriptorType` object), not jni-binding-generator enum fields, per the established
ordinal-vs-value hazard rule.

**First real exercise of array-of-struct-field marshalling against actual Awake structs.**
Rounds 3/4 added and unit-tested this capability in the generator itself, but no function
wired into Awake so far had actually used it (`VkBufferCreateInfo`/`VkMemoryAllocateInfo`
have no array fields). `VkDescriptorSetLayoutCreateInfo.pBindings` and
`VkDescriptorPoolCreateInfo.pPoolSizes` are both `Array<Struct>` fields — the generated
`extract_*`/`make_*` functions built a real `std::vector<JNI_VkDescriptorSetLayoutBinding>`
etc. and round-tripped correctly on the first generation, no hand-fixing needed beyond the
usual native-body fill-in.

**Regeneration re-merge, round 3:** same procedure as Round 6 (backup before regenerating,
regex-based function-body splice back in), plus this time explicitly re-diffed and restored
the `#include` block per the Round 6 lesson — confirmed that pitfall doesn't recur when
deliberately checked for.

**Verified end-to-end with a real uniform buffer wired into the actual triangle shader,
not an isolated call** — per the testing policy now in MVP_PLAN.md. `triangle.frag` reads
`layout(binding=0) uniform UBO { vec4 tint; }`; the demo creates a real uniform buffer,
descriptor set layout/pool/set, writes `tint = (0.5, 0.5, 1.0, 0.0)`, and binds the
descriptor set every frame before `vkCmdDraw`.

**On real hardware (Galaxy S25 Ultra), the expected effect was invisible to the eye** — a
linear ×0.5 tint displays as ×0.5^(1/2.2) ≈ ×0.73 through the framebuffer's sRGB gamma
curve, which reads as "looks the same" at a glance. Confirmed instead by sampling exact
pixel RGB values (Python/PIL) at matching triangle-interior coordinates across two
screenshots (before/after the UBO change): R and G channels scaled ~0.72–0.73× (matches the
gamma-corrected expectation for a linear 0.5×), B unchanged (matches `tint.b = 1.0`) —
precise numeric confirmation the uniform buffer data reaches the shader through the real
descriptor-set pipeline. **Lesson for the testing policy: a screenshot alone isn't proof for
a subtle shader effect (tint/scale) the way it is for a structural one (triangle present vs.
absent, right vs. wrong shape) — sample actual pixel values in that case.**

**Two pre-existing, unrelated bugs found and fixed while trying to get this visual
verification to work at all:**
1. `awake-demo/shared/build.gradle.kts`'s `glslValidator` `Exec` task called
   `commandLine(...)` once per shader file inside a `forEach` loop — `Exec.commandLine` is a
   single mutable property, so every call overwrites the previous one, and only the last
   shader iterated (filesystem-order-dependent, not deterministic) was ever actually
   compiled. It was also wired via `tasks.withType(JavaCompile::class.java) { dependsOn(...)
   }`, but this Kotlin-Multiplatform module (Android/desktop/iOS targets only) has no
   `JavaCompile` tasks, so that dependency silently never activated either — the task never
   ran automatically at all. Net effect: committed `.spv` files could go stale relative to
   their `.frag`/`.vert` source with no build failure surfacing it, which is exactly what
   had happened to `triangle.frag.spv` after this session's shader edit (the packaged APK
   was still shipping the pre-UBO shader, confirmed via `spirv-dis` showing no `UBO`
   in the disassembly). Fixed by running one real `ProcessBuilder` subprocess per shader
   (matching the logic the already-correct, but entirely unused,
   `buildSrc/glslangvalidator-conventions.gradle.kts` convention script had all along), and
   removed the dead `JavaCompile` wiring — `glslValidator` is now an explicitly manual step,
   same convention as `generateJniBindings`/`checkJniBindings`.
2. `VulkanApplication.kt`'s `setupDebugMessenger()` had its entire Vulkan validation-layer
   callback body commented out (it called `android.util.Log`, unavailable in `commonMain` —
   likely commented out just to get the file compiling, with the side effect of silently
   disabling all validation-layer log output). Every previous round's "no validation errors
   seen" claim was therefore unverifiable — there was no path for validation output to reach
   logcat at all. Replaced with `println(...)` (proven to surface as `System.out` in logcat
   per Round 5) so validation messages are actually visible going forward. This is kept
   permanently as diagnostic infrastructure, not reverted as a temporary snippet.

## Round 8 — images/samplers, a real bug caught by an intentional throw (2026-07-08)

Added `VulkanImages` (new object, same `.gen` package/pipeline): `vkCreateImage`,
`vkDestroyImage`, `vkGetImageMemoryRequirements`, `vkBindImageMemory`, `vkCreateSampler`,
`vkDestroySampler`, `vkTransitionImageLayout`, `vkCmdCopyBufferToImage`. Plus
`VulkanDescriptors.vkUpdateDescriptorSetImage` (combined-image-sampler write) and
`VulkanBuffers.writeBufferMemoryBytes` (byte-array sibling of `writeBufferMemoryFloats`).
New structs: `VkImageCreateInfo`, `VkSamplerCreateInfo`, `VkDescriptorImageInfo`,
`VkBufferImageCopy`, plus a new `VkImageLayout2` plain-`Int` object (deliberately separate
from the existing enum-typed `VkImageLayout` used by swapchain/render-pass code, per the
ordinal-vs-value hazard rule).

`vkTransitionImageLayout`'s native body is deliberately narrow — it only implements the two
layout transitions a texture upload actually needs (`UNDEFINED -> TRANSFER_DST_OPTIMAL`,
`TRANSFER_DST_OPTIMAL -> SHADER_READ_ONLY_OPTIMAL`), the same simplification
vulkan-tutorial.com's own reference implementation uses, rather than a fully generic
`VkImageMemoryBarrier` with a complete access-mask/pipeline-stage lookup table this MVP
doesn't need yet. Any other transition throws `IllegalArgumentException("...unsupported
layout transition")` instead of silently doing something wrong.

**That throw caught a real bug on the very first on-device run.** `VkImageLayout2.
VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL` was hand-written as `6` (a copy/paste-adjacent typo
next to `SHADER_READ_ONLY_OPTIMAL = 5`) — the real Vulkan value is `7`; `6` is actually
`VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL`. The demo's `createTextureImage()` requested a
transition to what it thought was `TRANSFER_DST_OPTIMAL` (really `TRANSFER_SRC_OPTIMAL`),
which the native function's two-case check didn't recognize, so it threw immediately with
the bad value visible in the Java stack trace (`SurfaceView: Exception configuring
surface`, `IllegalArgumentException: vkTransitionImageLayout: unsupported layout
transition`), rather than rendering a blank/corrupted texture with no diagnostic at all.
**This is the concrete payoff of the plain-`Int`-with-explicit-error-path pattern**: an
enum-ordinal version of this same typo would have silently picked a different
*valid-looking* layout and produced wrong pixels with zero errors — much harder to catch
than a same-session crash with the exact bad constant in the stack trace.

**Verified with a real 2x2 RGBA8 checkerboard texture**, uploaded via a staging buffer
(`writeBufferMemoryBytes` -> `vkCmdCopyBufferToImage` after an
`UNDEFINED->TRANSFER_DST_OPTIMAL` transition, then `->SHADER_READ_ONLY_OPTIMAL` before first
use), sampled in `triangle.frag` via a new `layout(binding=1) uniform sampler2D texSampler`
combined multiplicatively with the existing UBO tint and per-vertex color. The vertex
format grew a third attribute (`fragUV`, `vec2`), stride 5->7 floats. **Confirmed on real
hardware** (Galaxy S25 Ultra) after fixing the layout-value bug: a grid of pixel samples
across the rendered triangle (Python/PIL) shows a smooth darkening gradient toward the
corner whose UV coordinate (~(0,1)) samples the checkerboard's black texel, and a
correspondingly lighter gradient toward the corner sampling its white texel — matching
bilinear filtering of a real 2x2 texture, not a coincidental lighting difference.

This closes the "textured" half of Phase 1's textured-cube exit criteria. Remaining for
that milestone: actual cube geometry (indexed draw) and an MVP-matrix uniform buffer
(replacing today's flat tint vector) — everything else (buffers, vertex input, descriptors,
uniform buffers, images/samplers) is now proven end-to-end on real hardware.

## Round 9 — textured cube: indexed drawing, depth buffer, real MVP matrix (2026-07-08)

Added `vkCmdBindIndexBuffer`/`vkCmdDrawIndexed` (to `VulkanBuffers`, same jni-binding-
generator pipeline) and `vkDeviceWaitIdle` (needed to safely serialize frames around a
single, not per-frame-in-flight, uniform buffer that now gets rewritten every frame with a
fresh MVP matrix -- a deliberate MVP-scope simplification over double-buffering the UBO,
noted as Phase 2 renderer-abstraction follow-up). Wired a real depth buffer: a new
`VkFormat.VK_FORMAT_D32_SFLOAT` image (via `VulkanImages`), a second render-pass attachment
with `pDepthStencilAttachment` (a field that already existed on `VkSubpassDescription` but
had never been used), and `VkPipelineDepthStencilStateCreateInfo` — which already existed,
fully modeled, defaulting to depth-test/write enabled — finally wired into
`VkGraphicsPipelineCreateInfo.pDepthStencilState` instead of sitting commented out. The
demo's flat `tint` uniform was replaced with a real per-frame `model * view * projection`
matrix (4x4, `mat4` in the shader), computed via `awake-core`'s existing `Mat4`/`Vec3` math
types — not a demo-local reimplementation.

**First real multi-matrix-multiply caller of `awake-core`'s `Mat4`, and it exposed a real,
previously-invisible bug in that code.** `Mat4.data` is column-major (`data[col*4+row] =
M[row][col]`), matching GLSL's own `mat4` layout — this part is correct and necessary.
But the `times` operator's inner loops (`sum += data[i*4+k] * other.data[k*4+j]`) index the
array as if it were row-major. Working through the index algebra: for any two matrices A
and B, the Kotlin expression `A * B` evaluates to the *conventional* matrix product `B * A`
— operand order is silently reversed. This had apparently never been caught before because
no prior caller multiplied two non-trivial (non-identity, non-commuting) `Mat4`s together
and visually verified the result — every existing use was presumably single-matrix
transforms or commutative-enough cases that masked it.

**Symptom was a distinct failure signature worth naming for the testing policy:** the first
on-device attempt rendered a **completely blank Vulkan surface** — not a wrong shape, wrong
color, or wrong position, just nothing at all, every frame, with zero validation-layer
errors and zero crashes. Computing the MVP as `projection * view * model` (the natural,
*wrong*, Kotlin-side expression given the bug) silently produces the reverse conventional
product `model * view * projection`, which sends every vertex somewhere degenerate relative
to the clip-space frustum — Vulkan happily rasterizes nothing, because there's nothing valid
to rasterize. **"Nothing renders, no error" reliably means a transform/frustum problem, not
a broken draw call** — worth checking the math before re-auditing the Vulkan API surface
when a previously-working render goes fully blank.

**Fixed at the call site** (`model * view * projection` in Kotlin, to get the conventional
`projection * view * model` this bug's reversal actually produces), not inside `Mat4.times`
itself — an unknown number of other `awake-core` callers may already depend on (or
accidentally tolerate) the current reversed behavior, so fixing the operator is a separate,
deliberate cleanup task, not something to fold into a rendering milestone.

**Second, unrelated, expected issue:** `Mat4.perspective` follows the OpenGL NDC convention
(`+Y` up); Vulkan's NDC has `+Y` down. `projection.m11 *= -1f` at the call site corrects
this — a well-known Vulkan/OpenGL difference every Vulkan renderer built on OpenGL-style
math utilities has to apply itself, not a bug in the math library.

**Confirmed on real hardware** (Galaxy S25 Ultra) after both fixes: a proper 3D perspective
cube renders with correct depth-tested face occlusion (only the nearer of any two
overlapping faces visible, no z-fighting), smooth per-vertex color interpolation across
each face, and the checkerboard texture visibly darkening each face's shading — the actual
Phase 1 "textured cube with uniform-buffer MVP matrix" milestone, on Android. This device
session was also the first to use **wireless adb** (`adb tcpip 5555` + `adb connect
<ip>:5555`), confirmed working with the USB cable fully disconnected — set up specifically
because the USB transport had hiccupped earlier in this session (Round 8), and immune to
that class of problem going forward. Re-pairing is required again after the device reboots
or leaves the Wi-Fi network.
