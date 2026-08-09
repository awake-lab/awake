/*
 * Awake
 * Awake.awake-engine-app
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
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.engine.app"
    }

    // AwakeApplication.kt: the top-of-graph expect/actual seam (see
    // docs/tasks/2026-08-09-application-seam-and-module-naming-plan.md, Part 1) -- picks
    // VulkanGameApplication (android/ios/desktop) or WebGpuGameApplication (wasmJs) so
    // commonMain call sites never import a backend module directly. `vulkanMain` is a manual
    // intermediate source set (not part of Kotlin's default hierarchy template, which has no
    // group spanning android+ios+jvm while excluding wasmJs) shared by every target
    // `:awake:backend:vulkan` itself supports, so the actual is written once instead of
    // once per target.
    sourceSets {
        val vulkanMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(project(":awake:backend:vulkan"))
            }
        }
        named("androidMain") { dependsOn(vulkanMain) }
        named("desktopMain") { dependsOn(vulkanMain) }
        named("iosArm64Main") { dependsOn(vulkanMain) }
        named("iosSimulatorArm64Main") { dependsOn(vulkanMain) }

        commonMain.dependencies {
            api(project(":awake:engine:game"))
            api(project(":awake:engine:render:contract"))
        }
        named("wasmJsMain") {
            dependencies {
                api(project(":awake:backend:webgpu"))
            }
        }
    }
}
