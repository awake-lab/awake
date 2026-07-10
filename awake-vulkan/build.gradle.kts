/*
 * Awake
 * Awake.awake-vulkan
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

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
}

kotlin {
    jvmToolchain(17)

    // Explicit, not implicit: adding a manual dependsOn() edge anywhere in this project
    // (vulkanMain, below) silently disables the default hierarchy template's own automatic
    // wiring project-wide -- confirmed the hard way (iosArm64Main/iosSimulatorArm64Main lost
    // their "iosMain" parent entirely, breaking the real MoltenVK actuals that already lived
    // under src/iosMain). Calling this explicitly restores that template wiring; vulkanMain
    // is then layered on top as an *additional* parent, not a replacement.
    applyDefaultHierarchyTemplate()

    android {
        namespace = "io.github.ronjunevaldoz.awake.vulkan"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    // iosX64 (Intel simulator) dropped: Compose Multiplatform stopped publishing it
    // after 1.11.0-alpha01 (Apple Silicon only going forward)
    //
    // MoltenVK (Vulkan-over-Metal, Phase 6) is vendored as a git submodule at
    // ios-native/MoltenVK (KhronosGroup/MoltenVK) rather than a committed prebuilt
    // binary -- matches this module's existing desktop-native/android-native convention
    // of building/linking against real native toolchains rather than vendoring binaries.
    // One-time setup, from awake-vulkan/ios-native/MoltenVK:
    //   ./fetchDependencies --ios --iossim && make ios && make iossim
    // (each `make` target repackages Package/Release/MoltenVK/*.xcframework from
    // scratch, so `make ios` must run *after* `make iossim` -- or vice versa run twice --
    // to end up with both platform slices in one xcframework; confirmed empirically.)
    val moltenVkIncludeDir = file("ios-native/MoltenVK/Package/Release/MoltenVK/include")
    // Static, not dynamic: a dynamic MoltenVK.framework needs to be embedded into an app
    // bundle's Frameworks/ dir (an "Embed Frameworks" build phase) or dyld can't find it at
    // runtime -- confirmed the hard way (`dyld: Library not loaded: @rpath/MoltenVK.framework/
    // MoltenVK`) trying to run a plain Kotlin/Native test executable, which has no app
    // bundle/embed step at all. Static linking has no such runtime-loading step.
    val moltenVkStaticDir = mapOf(
        "iosArm64" to file(
            "ios-native/MoltenVK/Package/Release/MoltenVK/static/MoltenVK.xcframework/ios-arm64"
        ),
        "iosSimulatorArm64" to file(
            "ios-native/MoltenVK/Package/Release/MoltenVK/static/MoltenVK.xcframework/" +
                "ios-arm64_x86_64-simulator"
        ),
    )
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        val staticDir = moltenVkStaticDir.getValue(target.name)
        target.binaries.framework {
            baseName = "awake-vulkan"
        }
        // Gradle's binaries.X { linkerOpts(...) } only affects binaries THIS module
        // builds directly -- it does not propagate to a downstream consumer's own final
        // link step (e.g. awake-demo:shared's Shared.xcframework), confirmed the hard way
        // (undefined vk* symbols linking a real iOS app against Shared.framework despite
        // this module compiling clean). The linker flags cinterop actually propagates to
        // consumers are the ones embedded in the .def file's own `linkerOpts` directive --
        // so a per-target .def file is generated here (paths differ: device vs simulator
        // ship separate binary slices) rather than reusing one static file.
        val generatedDefFile = layout.buildDirectory.file("cinterop/MoltenVK-${target.name}.def").get().asFile
        generatedDefFile.parentFile.mkdirs()
        val moltenVkLinkerOpts = listOf(
            "-L${staticDir.path}", "-lMoltenVK", "-lc++",
            // MoltenVK's static lib references these Apple frameworks directly
            // (CAMetalLayer, IOSurface, color-space constants, etc.) -- a dynamic
            // .framework resolves its own dependencies at load time, but a static .a
            // needs them named explicitly at link time.
            "-framework", "Metal",
            "-framework", "QuartzCore",
            "-framework", "IOSurface",
            "-framework", "CoreGraphics",
            "-framework", "Foundation",
            "-framework", "UIKit",
        ).joinToString(" ")
        generatedDefFile.writeText(
            project.file("src/nativeInterop/cinterop/MoltenVK.def").readText() +
                "\nlinkerOpts = $moltenVkLinkerOpts\n"
        )
        target.compilations.getByName("main") {
            cinterops {
                create("MoltenVK") {
                    defFile(generatedDefFile)
                    includeDirs(moltenVkIncludeDir)
                }
            }
        }
    }

    jvm("desktop")

    // Phase 2.5 (Web/WebGPU, decision D7): scaffolding only -- wasmJsMain's actuals are
    // TODO() stubs until a real WebGPU implementation lands (see docs/MVP_PLAN.md). Added
    // here, not a new sibling module, so the wasmJs target sees the same expect seam
    // (GraphicsDevice/Mesh/Material/etc.) the other four platforms implement.
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // Renderer/DrawCall/TextureLoader (moved in from awake-core) need Mat4/Camera
            // and Bitmap/readResourceBytes -- see docs/MVP_PLAN.md's Decision Log, D11, for
            // the awake-core split this module boundary comes from.
            implementation(project(":awake-base"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        // Phase 2.5 (Web/WebGPU, decision D7) milestone 1: intermediate source set shared
        // by every platform whose actual Vulkan calls go through the same expect/actual
        // `Vulkan.vkXxx` low-level API (desktop/Android/iOS) -- NOT the default hierarchy
        // template (that only groups native/Apple targets; desktop+Android+iOS-vs-web is a
        // custom split this module needs). This is where GraphicsDevice/SwapchainManager/
        // Mesh/Material/RenderPipeline/Texture/TransferContext/Renderer's real bodies live
        // now that they're `expect class` in commonMain -- one shared body for all four
        // non-web platforms, same as before this split (zero behavior change), because they
        // already only called the already-platform-agnostic `Vulkan.vkXxx` surface.
        val vulkanMain = create("vulkanMain") {
            dependsOn(commonMain.get())
        }
        // applyDefaultHierarchyTemplate() (above) guarantees "iosMain" exists here --
        // layering vulkanMain onto it (rather than the two concrete iOS source sets
        // separately) is the more direct expression of "one shared body for every
        // Vulkan-backed platform" now that the template is restored.
        named("desktopMain") { dependsOn(vulkanMain) }
        named("androidMain") { dependsOn(vulkanMain) }
        named("iosMain") { dependsOn(vulkanMain) }
        androidMain.dependencies {
            // CMake/NDK build + bundled validation layers (AGP 9 KMP plugin
            // has no externalNativeBuild support, so a plain library owns it)
            api(project(":awake-vulkan:android-native"))
            implementation(libs.leakcanary.android)
        }
    }
}

// Phase 1b: desktop native build (see awake-vulkan/desktop-native/CMakeLists.txt for the
// Vulkan-SDK-per-host-OS rationale). Manual/on-demand, like android-native's
// generateJniBindings -- NOT wired as an automatic dependency of compileKotlinDesktop or
// desktopTest, because CMake configure+build is slow and this native lib only changes when
// the C++ sources themselves change, not on every Kotlin edit. Run explicitly:
//   ./gradlew :awake-vulkan:configureDesktopNative :awake-vulkan:buildDesktopNative
val desktopNativeBuildDir = layout.buildDirectory.dir("desktop-native")
val desktopNativeLibDir = layout.buildDirectory.dir("desktop-native-libs")

tasks.register<Exec>("configureDesktopNative") {
    group = "native"
    description = "Configure the desktop native build (CMake) -- run after any C++ source change."
    workingDir = desktopNativeBuildDir.get().asFile.also { it.mkdirs() }
    commandLine(
        "cmake",
        "-S", layout.projectDirectory.dir("desktop-native").asFile.absolutePath,
        "-B", desktopNativeBuildDir.get().asFile.absolutePath,
        "-DCMAKE_BUILD_TYPE=Debug"
    )
}

tasks.register<Exec>("buildDesktopNative") {
    group = "native"
    description = "Build the desktop native library (.dylib/.so/.dll) and copy it where the " +
        "desktop JVM's System.loadLibrary(\"awake-vulkan\") can find it (-Djava.library.path)."
    dependsOn("configureDesktopNative")
    workingDir = desktopNativeBuildDir.get().asFile
    commandLine("cmake", "--build", desktopNativeBuildDir.get().asFile.absolutePath)
    doLast {
        val libDir = desktopNativeLibDir.get().asFile.also { it.mkdirs() }
        val built = desktopNativeBuildDir.get().asFile.walkTopDown()
            .filter { it.isFile && it.name.matches(Regex("lib?awake-vulkan\\.(dylib|so|dll)")) }
            .firstOrNull()
            ?: throw GradleException("Built awake-vulkan native library not found under $desktopNativeBuildDir")
        built.copyTo(File(libDir, built.name), overwrite = true)
        println("Desktop native library copied to: ${File(libDir, built.name)}")
    }
}

// On macOS, the Vulkan loader (linked in desktop-native/CMakeLists.txt instead of MoltenVK
// directly -- see that file's comment for why) needs VK_ICD_FILENAMES to find MoltenVK's
// ICD manifest at run time. Located via a filesystem glob since the exact MoltenVK version
// (part of the Cellar path) varies by machine/install.
val moltenVkIcdPath = fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
    .plus(fileTree("/usr/local/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") })
    .files.firstOrNull()?.absolutePath
// GLFW's macOS Vulkan support check dlopen()s "libvulkan.1.dylib" by bare name at run
// time (confirmed via `strings`/`otool -L` -- its install name is the absolute Homebrew
// path, not a bare name, so a plain dlopen() only succeeds if a directory containing it is
// on DYLD_FALLBACK_LIBRARY_PATH). Without this, glfwGetRequiredInstanceExtensions/
// glfwVulkanSupported silently fail even though a direct vkCreateInstance call (which
// doesn't go through GLFW's own loader-finding logic) works fine.
val desktopVulkanEnv = buildMap {
    if (moltenVkIcdPath != null) put("VK_ICD_FILENAMES", moltenVkIcdPath)
    put("DYLD_FALLBACK_LIBRARY_PATH", "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib")
}

// Always points java.library.path at desktop-native-libs for desktop tests -- a no-op if
// buildDesktopNative hasn't been run (System.loadLibrary just fails with its usual
// UnsatisfiedLinkError in that case, same as if this weren't set at all).
tasks.named<Test>("desktopTest") {
    jvmArgs("-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
    environment(desktopVulkanEnv)
}

val startOnFirstThread = if (System.getProperty("os.name").lowercase().contains("mac")) {
    listOf("-XstartOnFirstThread")
} else {
    emptyList()
}

// Manual diagnostic task (same convention as checkJniBindings) -- proves GLFW window +
// Vulkan surface creation actually works, which desktopTest itself cannot safely check
// (see GlfwManualVerify.kt's doc comment for the macOS main-thread reason). Run via
// `./gradlew :awake-vulkan:verifyGlfwMain` after buildDesktopNative.
tasks.register<JavaExec>("verifyGlfwMain") {
    group = "native"
    description = "Manually verify GLFW window + Vulkan surface creation on the real OS main " +
        "thread (see GlfwManualVerify.kt) -- run after buildDesktopNative."
    dependsOn("compileTestKotlinDesktop")
    mainClass.set("GlfwManualVerifyKt")
    classpath = files(
        layout.buildDirectory.dir("classes/kotlin/desktop/test"),
        kotlin.jvm("desktop").compilations.getByName("test").runtimeDependencyFiles
    )
    jvmArgs(startOnFirstThread + "-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
    environment(desktopVulkanEnv)
}
