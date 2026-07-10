/*
 * Awake
 * Awake.awake-vulkan.android-native
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Plain Android library that owns the CMake/NDK build and bundled Vulkan
// validation layers. Split out of :awake-vulkan because AGP 9's
// com.android.kotlin.multiplatform.library plugin does not support
// externalNativeBuild; :awake-vulkan's androidMain depends on this module.
plugins {
    alias(libs.plugins.android.library)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
}

android {
    namespace = "io.github.ronjunevaldoz.awake.vulkan.jni"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    ndkVersion = "26.1.10909125"

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        externalNativeBuild {
            cmake {
                // 64-bit only: the generated JNI marshalling code assumes pointer-sized
                // Vulkan handles, which 32-bit ABIs (uint64_t handles) don't satisfy
                abiFilters += listOf("arm64-v8a", "x86_64")
                cppFlags += listOf("-DVK_USE_PLATFORM_ANDROID_KHR", "-lvulkan")
                arguments += listOf("-DANDROID_TOOLCHAIN=clang", "-DANDROID_STL=c++_static")
            }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("../src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildTypes {
        debug {
            isJniDebuggable = true
        }
    }
}

// Generates JNI marshalling C++ from external fun declarations in :awake-vulkan's Kotlin
// source, using a vendored copy of jni-binding-generator (tools/jni-binding-generator —
// see that directory's README for why it's vendored rather than referenced by path).
//
// --kotlin-source points at the whole module (needed so the generator's struct/enum
// pre-pass can see types declared anywhere, e.g. models/info/VkBufferCreateInfo.kt), but
// --package-filter scopes actual *generation* to io.github.ronjunevaldoz.awake.vulkan.gen —
// the new Phase 1d package. The legacy io.github.ronjunevaldoz.awake.vulkan.Vulkan (58
// functions, awake-vulkan-generator-backed) is deliberately left alone: some of its
// existing signatures (e.g. Array<VkLayerProperties> as a *return type*, as opposed to a
// struct field) use shapes jni-binding-generator doesn't support at the function level yet.
// New Vulkan API surface should be added to the .gen package; see
// docs/decisions/D10-codegen-derisk-findings.md for the full history.
//
// IMPORTANT — neither task below runs automatically before the native build.
// The generated *_jni.gen.cpp's JNI entry-point bodies are TODO stubs ("Call into your
// native library using the marshalled values above") that get hand-edited with the real
// Vulkan calls (see the header comment in generated/VulkanBuffers_jni.gen.cpp —
// jni-binding-generator's own bundled examples work the same way; none show a filled-in
// body either, this is the tool's actual intended usage, not a workaround).
//
// This also means `--check` (byte-for-byte diff against a fresh generation) CANNOT be
// wired in as an automatic build gate: once a hand-edit exists, `--check` fails forever,
// even when the Kotlin signature hasn't changed — it has no way to tell "signature drift"
// apart from "TODO body intentionally filled in". `checkJniBindings` below is kept as a
// manually-run diagnostic (most useful right after changing a .gen signature, before
// re-applying hand edits, to see the true diff) — it is NOT a build dependency. The actual
// safety net for signature drift is the C++ compiler itself: if a .gen struct's shape
// changes incompatibly, the hand-written body referencing its old fields fails to compile,
// pointing at the exact mismatch.
val kotlinSourceForJni = layout.projectDirectory.dir("../src")
// NOTE: this must resolve to awake-backend-vulkan/src/main/cpp/generated (NOT
// android-native/src/main/cpp/generated) because externalNativeBuild.cmake.path above
// points CMake's project root at "../src/main/cpp/CMakeLists.txt" — i.e.
// awake-backend-vulkan/src, a sibling of this module, not a subdirectory of it.
val jniOutputDir = layout.projectDirectory.dir("../src/main/cpp/generated")
val jniScriptsDir = rootProject.layout.projectDirectory.dir("tools/jni-binding-generator/scripts")
val jniScript = jniScriptsDir.file("jni-binding-generator.py")
val jniPackageFilter = "io.github.ronjunevaldoz.awake.vulkan.gen"

tasks.register<Exec>("generateJniBindings") {
    group = "jni"
    description = "Generate JNI marshalling C++ from Kotlin external functions (jni-binding-generator). " +
        "Run manually after changing a .gen package signature, then re-apply hand-written native bodies."

    inputs.dir(jniScriptsDir)
    inputs.dir(kotlinSourceForJni)
    outputs.dir(jniOutputDir)

    commandLine(
        "python3",
        jniScript.asFile.absolutePath,
        "--kotlin-source", kotlinSourceForJni.asFile.absolutePath,
        "--output", jniOutputDir.asFile.absolutePath,
        "--package-filter", jniPackageFilter,
    )
}

tasks.register<Exec>("checkJniBindings") {
    group = "jni"
    description = "Fail if committed generated JNI bindings are stale relative to the Kotlin source " +
        "(run generateJniBindings + re-apply native bodies if this fails)"

    inputs.dir(jniScriptsDir)
    inputs.dir(kotlinSourceForJni)

    commandLine(
        "python3",
        jniScript.asFile.absolutePath,
        "--kotlin-source", kotlinSourceForJni.asFile.absolutePath,
        "--output", jniOutputDir.asFile.absolutePath,
        "--package-filter", jniPackageFilter,
        "--check",
    )
}
