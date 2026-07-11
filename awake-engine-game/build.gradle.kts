/*
 * Awake
 * Awake.awake-engine-game
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
        namespace = "io.github.ronjunevaldoz.awake.engine.application"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "awake-engine-game"
        }
    }

    jvm("desktop")

    wasmJs {
        browser()
    }

    // GenericGameApplication.kt: the backend-neutral bootstrap (scene loading, fixed-
    // timestep loop, UI/debug-line staging) that VulkanGameApplication (awake-backend-
    // vulkan) and WebGpuGameApplication (awake-backend-webgpu) both extend, instead of
    // hand-duplicating the same ~90% of each other's fields/lifecycle/hooks (see
    // docs/MVP_PLAN.md's decision log for the duplication this replaces).
    sourceSets {
        commonMain.dependencies {
            // Application, FixedTimestepLoop (via awake-base transitively).
            api(project(":awake-engine"))
            // World, SceneLoader, TransformSystem, RenderSystem, MeshRenderer, SceneInstance,
            // SceneRenderableRequest -- and transitively awake-engine-render-api's
            // Renderer/Mesh/Material/MeshGeometry/TextureAsset/LineSegment interfaces +
            // awake-engine-ui's UiContext/BitmapFont.
            api(project(":awake-scene"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
