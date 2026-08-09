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
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.engine.application"
    }

    // GameApplication.kt: the backend-neutral render bootstrap that
    // VulkanGameApplication (awake-backend-vulkan) and WebGpuGameApplication
    // (awake-backend-webgpu) both extend, plus the Game interface a game implements and
    // injects into it -- deliberately has zero ECS/scene-graph/UI knowledge (see
    // docs/MVP_PLAN.md's decision log, "GameApplication a standalone render
    // bootstrap").
    sourceSets {
        commonMain.dependencies {
            // Application, FixedTimestepLoop (via awake-base transitively).
            api(project(":awake:base"))
            // Renderer/LineSegment -- Game.ready(renderer)'s parameter type and
            // drawDebugLines()'s plumbing, nothing scene/ECS-specific.
            api(project(":awake:engine:render:contract"))
            implementation(libs.kotlinx.coroutines.core)
        }
        named("androidMain") {
            dependencies {
                implementation(project(":awake:engine:ui:ui-core"))
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
