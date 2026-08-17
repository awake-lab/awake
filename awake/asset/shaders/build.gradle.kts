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

// No Kotlin plugin -- this module is shader text only, consumed as a plain on-disk directory by
// samples/*'s own syncAwakeShaders/validateAwakeShaders tasks (see SyncWgslShaderPipelineTask's
// additionalSourceDirectories), not compiled or resolved as a code/resource dependency.
plugins {
    id("awake.shader-pipeline-convention")
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
