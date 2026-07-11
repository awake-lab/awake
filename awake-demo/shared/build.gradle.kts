/*
 * Awake
 * Awake.awake-demo.shared
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

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.glsl-validator-convention")
}

kotlin {
    jvmToolchain(17)

    // Web demo (see docs/MVP_PLAN.md's decision log): adding a manual dependsOn() edge
    // below (appMain) silently disables the default hierarchy template's own automatic
    // wiring project-wide otherwise -- confirmed the hard way once already this session
    // (awake-backend-vulkan's iosMain briefly lost its parent). Calling this explicitly
    // restores that template wiring; appMain is layered on top as an *additional* parent.
    applyDefaultHierarchyTemplate()

    android {
        namespace = "io.github.ronjunevaldoz.awake.demo.common"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    jvm("desktop")

    // iosX64 (Intel simulator) dropped: Compose Multiplatform stopped publishing it
    // after 1.11.0-alpha01 (Apple Silicon only going forward)
    // Distributed as an XCFramework via SPM (kotlin-multiplatform-xcframework-spm skill),
    // not CocoaPods -- SPM binary targets don't need Kotlin/Gradle/CocoaPods installed
    // on the iOS-only-consumer's machine, and Xcode resolves them natively.
    val xcf = XCFramework("Shared")
    // MoltenVK's own linkerOpts (set in awake-backend-vulkan/build.gradle.kts's cinterop
    // .def file) do NOT propagate through the project(":awake-backend-vulkan") dependency to
    // this module's own framework link step -- confirmed the hard way (Shared.framework's
    // binary still showing _vkCreateInstance as an undefined symbol via `nm -g` even after
    // that fix). Kotlin/Native only applies a cinterop klib's linkerOpts to the binary built
    // by the module that OWNS the cinterop; a downstream consumer's final link needs the
    // same flags repeated here.
    val moltenVkStaticDir = mapOf(
        "iosArm64" to project(":awake-backend-vulkan").file(
            "ios-native/MoltenVK/Package/Release/MoltenVK/static/MoltenVK.xcframework/ios-arm64"
        ),
        "iosSimulatorArm64" to project(":awake-backend-vulkan").file(
            "ios-native/MoltenVK/Package/Release/MoltenVK/static/MoltenVK.xcframework/" +
                "ios-arm64_x86_64-simulator"
        ),
    )
    fun moltenVkLinkerOpts(targetName: String) = listOf(
        "-L${moltenVkStaticDir.getValue(targetName).path}", "-lMoltenVK", "-lc++",
        "-framework", "Metal",
        "-framework", "QuartzCore",
        "-framework", "IOSurface",
        "-framework", "CoreGraphics",
        "-framework", "Foundation",
        "-framework", "UIKit",
    )
    iosArm64 {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosArm64"))
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosSimulatorArm64"))
            xcf.add(this)
        }
    }

    // Web demo (see docs/MVP_PLAN.md's decision log): the bare-canvas WebGPU entry point
    // (wasmJsMain/main.kt) does not go through App()/DemoScene()/AwakeCanvas at all --
    // Compose Multiplatform's wasmJs target has no first-class API to embed a native
    // <canvas> inside the Compose layout tree the way UIKitView/AndroidView do.
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.components.resources)
            implementation(project(":awake-scene"))
            implementation(project(":awake-engine"))
            implementation(libs.kotlinx.coroutines.core)
        }
        // Web demo (see docs/MVP_PLAN.md's decision log): App.kt/AwakeCanvas.kt (the
        // expect fun)/demo/DemoScene.kt/demo/VulkanApplication.kt all depend on
        // awake-backend-vulkan and/or awake-opengl, neither of which publishes a wasmJs
        // variant by design -- a commonMain.dependencies entry on either would fail Gradle
        // dependency resolution the moment wasmJs becomes a declared target of this
        // module, not just fail at compile time. This intermediate source set (shared by
        // desktop/Android/iOS only) is where those 4 files and their 2 dependencies live
        // instead -- the exact pattern slice 2 removed from awake-backend-vulkan, needed
        // here one layer up for a different reason. demo/SceneRuntimeHost.kt stays in true
        // commonMain: it only ever touched the backend-neutral Renderer interface (fixed
        // this same session), so it's genuinely reusable by both VulkanApplication here
        // and the wasmJs-only WebGpuApplication.
        val appMain = create("appMain") {
            dependsOn(commonMain.get())
        }
        appMain.dependencies {
            implementation(project(":awake-backend-vulkan"))
            // Legacy OpenGL demo path (App.kt/DemoApplication.kt/scene/Demo*.kt) -- see
            // docs/MVP_PLAN.md's Decision Log, D11 follow-up, for why this moved out of
            // awake-engine.
            implementation(project(":awake-opengl"))
        }
        named("desktopMain") { dependsOn(appMain) }
        named("androidMain") { dependsOn(appMain) }
        named("iosMain") { dependsOn(appMain) }
        androidMain.dependencies {
            api(libs.androidx.activity.compose)
            api(libs.androidx.appcompat)
            api(libs.androidx.core.ktx)
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.common)
        }
        named("wasmJsMain") {
            dependencies {
                implementation(project(":awake-backend-webgpu"))
                // awake-backend-webgpu depends on these via `implementation`, not `api`,
                // so main.kt (which talks to wgpu4k/the DOM directly to resolve the
                // WGPUContext before handing it to WebGpuApplication) needs its own copies.
                implementation(libs.wgpu4k)
                implementation(libs.wgpu4k.toolkit)
                implementation(libs.kotlinx.browser)
            }
        }
    }
}

tasks.register<JavaExec>("runVulkanCpp") {
    mainClass.set("io.github.ronjunevaldoz.awake.vulkan_generator.MainKt")
    classpath = project(":awake-vulkan-generator").sourceSets["main"].runtimeClasspath
    args(project(":awake-vulkan-generator").rootDir.path)
}
// glslValidator is a manual step, run explicitly after changing any .vert/.frag under
// src/commonMain/resources/assets/shader/vulkan -- NOT wired as an automatic dependency
// of any compile task. The shared build-logic plugin registers it so every module sees
// the same shader workflow without duplicating the task wiring here.
