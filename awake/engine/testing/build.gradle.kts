/*
 * Awake
 * Awake.awake-engine-testing
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
        namespace = "io.github.ronjunevaldoz.awake.testing"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "awake-engine-testing"
        }
    }

    jvm("desktop")

    wasmJs {
        browser()
    }

    // No platform-specific code -- comparePixels is pure common Kotlin over ByteArray
    // (readPixels/TextureAsset already return the same RGBA8 layout on every backend, so
    // there's nothing platform-specific to bridge here). Baseline bytes are loaded via
    // awake:base's readResourceBytes, already expect/actual'd for all 5 targets.
    sourceSets {
        commonMain.dependencies {
            implementation(project(":awake:base"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
