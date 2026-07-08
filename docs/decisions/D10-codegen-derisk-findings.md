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
