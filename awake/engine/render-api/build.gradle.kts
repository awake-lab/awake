/*
 * Awake
 * Awake.awake-engine-render-api
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
        namespace = "io.github.ronjunevaldoz.awake.render"
    }

    // No platform-specific code at all in this module (see docs/MVP_PLAN.md's module
    // restructuring notes) -- every declaration here is a plain interface/data class,
    // implemented by each backend module (awake-vulkan today; a future awake-backend-webgpu
    // once slice 2 physically splits that out).
    sourceSets {
        commonMain.dependencies {
            // DrawCall/Renderer.draw() take Mat4/Camera (portable math), and the resource-
            // loading `expect fun`s some backends' Texture implementations need come from
            // awake-base too.
            implementation(project(":awake:base"))
            // api, not implementation: Renderer.drawUi(primitives: List<UiDrawPrimitive>)
            // exposes UiDrawPrimitive in this module's own public interface, so consumers
            // implementing Renderer (awake-backend-vulkan, awake-backend-webgpu) need it
            // visible transitively.
            api(project(":awake:engine:ui:ui-core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
