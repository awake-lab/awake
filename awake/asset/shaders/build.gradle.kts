/*
 * Awake
 * Awake.awake-asset-shaders
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

// Two things live in this module now: the shared shader TEXT (consumed as a plain on-disk
// directory by samples/*'s own syncAwakeShaders/validateAwakeShaders tasks, unchanged), and
// Kotlin describing each shared shader's own Uniforms struct (TexturedUniformLayout.kt,
// LitShadowUniformLayout.kt) -- moved here from awake:engine:render:contract so the layout and
// the shader file it describes live in one module instead of the generic render API module
// carrying authored-shader-specific knowledge. A small, concrete step in the same direction as
// awake/core/README.md's "Proposed future modules" section (many small focused leaf modules
// instead of one module accreting unrelated concerns) -- see docs/tasks/
// 2026-08-17-awake-core-module-split-proposal.md for that broader (separate, awake:core-only)
// proposal this doesn't implement, just echoes the same instinct for.
plugins {
    id("awake.shader-pipeline-convention")
    id("awake.kmp-library-convention")
    id("awake.publish-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.asset.shaders"
    }

    sourceSets {
        commonMain.dependencies {
            // UniformField/UniformLayout/GpuDataShape -- the generic machinery
            // TexturedUniformLayout/LitShadowUniformLayout are built from. api, not
            // implementation: a consumer needs those types visible through this module too
            // (matches awake:backend:vulkan's own api(render:contract) for the same reason).
            api(project(":awake:engine:render:contract"))
            // readResourceBytes -- ShaderSource.resolveBytes()'s own implementation detail, not
            // part of this module's public API surface, so implementation (not api) is enough.
            implementation(project(":awake:core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

mavenPublishing {
    pom {
        name.set("Awake Asset Shaders")
        description.set("Shared WGSL shader sources and their Kotlin-side uniform layouts")
    }
}

// The convention plugin defaults sourceDirectory to the non-standard src/commonMain/shaders --
// this module keeps its shaders under the conventional KMP resources root instead (matching
// awake/ui/text/src/commonMain/resources/fonts's existing precedent), even though nothing here
// reads them via a KMP resource API.
val sharedShaderDirectory = layout.projectDirectory.dir("src/commonMain/resources/shaders")

tasks.named<ValidateWgslShadersTask>("validateAwakeShaders") {
    sourceDirectory.set(sharedShaderDirectory)
}

tasks.named<SyncWgslShaderPipelineTask>("syncAwakeShaders") {
    sourceDirectory.set(sharedShaderDirectory)
}
