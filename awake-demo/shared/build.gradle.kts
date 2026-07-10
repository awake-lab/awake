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
    id("awake.glsl-validator-convention")
}

kotlin {
    jvmToolchain(17)

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
    iosArm64 {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.components.resources)
            implementation(project(":awake-scene"))
            implementation(project(":awake-vulkan"))
            implementation(project(":awake-core"))
            // Legacy OpenGL demo path (App.kt/DemoApplication.kt/scene/Demo*.kt) -- see
            // docs/MVP_PLAN.md's Decision Log, D11 follow-up, for why this moved out of
            // awake-core.
            implementation(project(":awake-opengl"))
        }
        androidMain.dependencies {
            api(libs.androidx.activity.compose)
            api(libs.androidx.appcompat)
            api(libs.androidx.core.ktx)
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.common)
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
