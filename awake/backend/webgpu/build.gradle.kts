/*
 * Awake
 * Awake.awake-backend-webgpu
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


import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)

    // Module restructuring slice 2 (see docs/MVP_PLAN.md): physically split out of
    // awake-vulkan (now awake-backend-vulkan) -- this module owns only the wgpu4k/WebGPU
    // backend, wasmJs is its sole target. No android/desktop/iOS targets, so no android
    // library plugin here (unlike awake-backend-vulkan/awake-engine-render-api).
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":awake:core"))
            // Mesh/Material/Renderer implement the same narrow backend-neutral interfaces
            // awake-backend-vulkan's do -- see awake-engine-render-api's module doc.
            api(project(":awake:engine:render:contract"))
            // Reusable-Application gap fix (see docs/MVP_PLAN.md's Decision Log): same
            // reasoning as awake-backend-vulkan's VulkanGameApplication -- see that
            // module's build.gradle.kts comment.
            implementation(project(":awake:core"))
            implementation(project(":awake:scene"))
            implementation(libs.kotlinx.coroutines.core)
            // WebGpuGameApplication now extends GameApplication (see
            // docs/MVP_PLAN.md's decision log for the duplication this replaces). `api`,
            // not `implementation`: same reasoning as awake-backend-vulkan's identical
            // dependency -- it's a supertype, so consumers need it on their classpath too.
            api(project(":awake:engine:game"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        named("wasmJsMain") {
            dependencies {
                implementation(libs.wgpu4k)
                implementation(libs.wgpu4k.toolkit)
                implementation(libs.kotlinx.browser)
            }
        }
    }
}
