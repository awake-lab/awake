/*
 * Awake
 * Awake.awake-vulkan-generator
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
    alias(libs.plugins.kotlin.jvm)
    application
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

sourceSets["main"].kotlin.srcDir("src/main/kotlin")
// Was "awake-backend-vulkan/src/main/cpp/vulkan-kotlin/" -- stale since a much earlier module
// rename (that directory name hasn't existed in a long time); corrected here to the real
// current location while updating this file for the bindings split anyway.
sourceSets["main"].resources.srcDir("../bindings/src/main/cpp/vulkan-kotlin/")

application { // Specify the main class using the application plugin
    mainClass.set("io.github.ronjunevaldoz.awake.vulkan_generator.MainKt")
}

dependencies {
    // models.* (what MainKt reflects over to generate C++ accessors/mutators) now lives in
    // the bindings module -- see docs/tasks/2026-08-09-application-seam-and-module-naming-
    // plan.md, Part 3.
    implementation(project(":awake:backend:vulkan:bindings"))
    implementation(kotlin("reflect"))
}
