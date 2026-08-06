/*
 * Awake
 * Awake.awake-scene
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

import java.util.Properties

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("awake.kmp-library-convention")
    alias(libs.plugins.kotlin.serialization)
    id("awake.publish-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.scene"
    }

    sourceSets {
        commonMain.dependencies {
            // Re-exported for facade consumers (see docs/tasks/2026-08-05-scene-module-split-
            // proposal.md) -- `:awake:scene`'s own remaining source (the deprecated
            // `SceneRuntime` bootstrap, `NavMesh`) doesn't itself need controls/physics/
            // runtime, but published `awake-scene` consumers still expect them.
            api(project(":awake:scene:core"))
            api(project(":awake:scene:controls"))
            api(project(":awake:scene:physics"))
            api(project(":awake:scene:rendering"))
            api(project(":awake:scene:runtime"))
            // Needed directly by SceneRuntime.kt/NavMesh.kt.
            api(project(":awake:base"))
            api(project(":awake:ecs"))
            // Module restructuring slice 1 (see docs/MVP_PLAN.md): RenderSystem/MeshRenderer
            // only ever touch the backend-neutral Mesh/Material/Renderer/DrawCall contract,
            // never awake-vulkan's concrete Vulkan bindings -- depending on just the
            // interface module (instead of all of awake-vulkan) is the actual point of this
            // restructuring.
            api(project(":awake:engine:render-api"))
            // PhysicsBody/PhysicsSystem only ever touch the backend-neutral PhysicsWorld
            // contract, never a concrete backend (`awake:backend:jolt`) -- same restructuring
            // rationale as the render-api dependency above.
            api(project(":awake:physics:api"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

mavenPublishing {
    pom {
        name.set("Awake Scene")
        description.set("Awake ECS scene components and systems")
    }
}
