/*
 * Awake
 * Awake.awake-backend-jolt
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
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.ronjunevaldoz.awake.physics.jolt"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    // Jolt Physics integration slice 1 (see docs/MVP_PLAN.md's decision log): jolt-jni
    // covers desktop+Android for real this slice. iOS (JoltC cinterop) and wasmJs
    // (JoltPhysics.js) are deferred -- these two targets get an explicit TODO()-throwing
    // stub `JoltPhysicsWorld` here just so the module compiles on every target, no cinterop
    // or JS interop of any kind lives here yet.
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "awake-backend-jolt"
        }
    }

    jvm("desktop")

    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:physics:api"))
            // QuatEuler.kt's pure quaternion-to-Euler conversion takes/returns Vec3.
            implementation(project(":awake:base"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        // jolt-jni is a JVM-only native binding (desktop+Android) -- duplicated verbatim
        // between desktopMain/androidMain rather than a shared intermediate source set,
        // same convention awake:scene's recast4j DemoNavMesh actuals already established
        // (this repo's default hierarchy template doesn't group desktop+Android under one
        // JVM-shared source set the way it auto-groups js+wasmJs under `webMain`).
        named("desktopMain") {
            dependencies {
                // JVM library (Java classes) -- see libs.versions.toml's comment for why
                // MacOSX_ARM64's artifact ID is used (this dev machine's platform); the
                // classes themselves are identical across all 8 desktop platform artifact
                // IDs jolt-jni publishes.
                implementation(libs.jolt.jni.desktop)
                // Native library matching this dev machine (macOS Apple Silicon), Release
                // build + single-precision ("Sp") flavor -- see jolt-jni's own "add to an
                // existing project" doc for the Debug/Release and Sp/Dp axes.
                runtimeOnly("com.github.stephengold:jolt-jni-MacOSX_ARM64:5.2.0:ReleaseSp")
                // Extracts + loads the native library above at runtime (see JoltNative.kt).
                implementation(libs.snaploader)
                implementation(libs.oshi.core)
            }
        }
        named("androidMain") {
            dependencies {
                // Self-contained AAR (Java classes + all Android native ABIs) -- unlike
                // desktop, no separate native-library artifact or snaploader needed;
                // System.loadLibrary("joltjni") finds it via the AAR's own jniLibs layout.
                // "SpDebug", not "SpRelease" (confirmed the hard way): the published
                // 5.2.0 "SpRelease" AAR's classes.jar has been R8-shrunk down to zero
                // .class files (only its bundled Metal/Vulkan shader resources survive) --
                // presumably built as a standalone library with no consumer keep-rules, so
                // R8 treated every class as unreachable. jolt-jni's own "add to an existing
                // project" doc suggests starting with the Debug AAR regardless, so this
                // isn't a workaround so much as the documented default.
                implementation("com.github.stephengold:jolt-jni-Android:5.2.0:SpDebug@aar")
            }
        }
    }
}
