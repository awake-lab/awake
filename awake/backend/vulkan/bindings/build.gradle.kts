/*
 * Awake
 * Awake.awake-backend-vulkan.bindings
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

// Raw generated Vulkan API surface, split out of :awake:backend:vulkan (see
// docs/tasks/2026-08-09-application-seam-and-module-naming-plan.md, Part 3): gen/ (JNI-backed
// vkCreate*/vkDestroy* calls), handles/, models/, enums/, plus the small hand-authored
// vulkan/ root (Vulkan, VkArray, Version), utils/, and VulkanSurface's per-platform surface
// creation. Zero Awake engine opinions -- GraphicsDevice/SwapchainManager/RenderPipeline and
// everything else that decides HOW to use these calls stays in :awake:backend:vulkan, which
// depends on this module. Standalone-publishable for a consumer who wants raw Vulkan access
// without Awake's renderer.
//
// All the native/CMake/cinterop machinery below is bindings-layer entirely, confirmed by
// reading desktop-native/CMakeLists.txt's own add_library() call: it compiles ONLY
// awake-vulkan.cpp and the four gen/*_jni.gen.cpp JNI glue files, linked against vulkan_kotlin
// (the per-struct accessor/mutator tree matching models/). There is no engine-layer C++ --
// GraphicsDevice/RenderPipeline/etc. are pure Kotlin calling into this module's `Vulkan.*`
// functions, which this native library backs.

plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.vulkan"
    }

    // See :awake:backend:vulkan's build.gradle.kts history for the full MoltenVK rationale
    // (static vs dynamic linking, why iosX64 is dropped). Unchanged by the split -- MoltenVK
    // backs the raw Vulkan-over-Metal calls this module wraps, not anything engine-specific.
    val moltenVkIncludeDir = file("ios-native/MoltenVK/Package/Release/MoltenVK/include")
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
        "iosArm64",
        "iosSimulatorArm64"
    ).forEach { targetName ->
        val staticDir = moltenVkStaticDir.getValue(targetName)
        val target =
            kotlin.targets.getByName(targetName) as org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

        val generatedDefFile =
            layout.buildDirectory.file("cinterop/MoltenVK-$targetName.def").get().asFile
        generatedDefFile.parentFile.mkdirs()
        val moltenVkLinkerOpts = listOf(
            "-L${staticDir.path}", "-lMoltenVK", "-lc++",
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

    sourceSets {
        commonMain.dependencies {
            // Buffer/memory types the raw Vk*/gen wrappers marshal through.
            implementation(project(":awake:core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            // CMake/NDK build + bundled validation layers (AGP 9 KMP plugin has no
            // externalNativeBuild support, so a plain library owns it).
            api(project(":awake:backend:vulkan:bindings:android-native"))
        }
    }
}

// Manual/on-demand, like android-native's generateJniBindings -- NOT wired as an automatic
// dependency of compileKotlinDesktop or desktopTest, since CMake configure+build is slow and
// this native lib only changes when the C++ sources themselves change. Run explicitly:
//   ./gradlew :awake:backend:vulkan:bindings:configureDesktopNative :awake:backend:vulkan:bindings:buildDesktopNative
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

// See :awake:backend:vulkan's original build.gradle.kts history for the macOS Vulkan
// loader/ICD rationale -- unchanged by the split.
val moltenVkIcdPath =
    fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
        .plus(fileTree("/usr/local/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") })
        .files.firstOrNull()?.absolutePath
val desktopVulkanEnv = buildMap {
    if (moltenVkIcdPath != null) put("VK_ICD_FILENAMES", moltenVkIcdPath)
    put(
        "DYLD_FALLBACK_LIBRARY_PATH",
        "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib"
    )
}

tasks.named<Test>("desktopTest") {
    jvmArgs("-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
    environment(desktopVulkanEnv)
}

val startOnFirstThread = if (System.getProperty("os.name").lowercase().contains("mac")) {
    listOf("-XstartOnFirstThread")
} else {
    emptyList()
}

// Manual diagnostic task (same convention as checkJniBindings) -- proves GLFW window + Vulkan
// surface creation actually works, which desktopTest itself cannot safely check (see
// GlfwManualVerify.kt's doc comment for the macOS main-thread reason). Run via
// `./gradlew :awake:backend:vulkan:bindings:verifyGlfwMain` after buildDesktopNative.
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
