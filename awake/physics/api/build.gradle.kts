/*
 * Awake
 * Awake.awake-physics-api
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
        namespace = "io.github.ronjunevaldoz.awake.physics"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "awake-physics-api"
        }
    }

    jvm("desktop")

    wasmJs {
        browser()
    }

    // No platform-specific code at all in this module (mirrors awake:engine:render-api's own
    // module-restructuring rationale, see docs/MVP_PLAN.md) -- every declaration here is a
    // plain interface/data class, implemented per-platform-binding by each physics backend
    // module (awake-backend-jolt today; JoltC/JoltPhysics.js backends deferred to a later
    // slice, see that decision log entry).
    sourceSets {
        commonMain.dependencies {
            // BodyTransform/RaycastHit/PhysicsWorld all take/return Vec3 (portable math).
            implementation(project(":awake:base"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
