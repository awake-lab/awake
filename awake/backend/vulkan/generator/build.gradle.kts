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
sourceSets["main"].resources.srcDir("awake-backend-vulkan/src/main/cpp/vulkan-kotlin/")

application { // Specify the main class using the application plugin
    mainClass.set("io.github.ronjunevaldoz.awake.vulkan_generator.MainKt")
}

dependencies {
    implementation(project(":awake:backend:vulkan"))
    implementation(kotlin("reflect"))
}
