/*
 * Awake
 * Awake.awake-base
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
    alias(libs.plugins.kotlin.serialization)
    id("awake.publish-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.base"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.napier)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        // Web demo (see docs/MVP_PLAN.md's decision log): readResourceBytes' wasmJs actual
        // needs a real browser fetch() -- kotlinx-browser wraps it.
        named("wasmJsMain") {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }
}

mavenPublishing {
    pom {
        name.set("Awake Base")
        description.set("Dependency-free foundation: math, input, fixed-timestep loop, glTF parsing, resource/bitmap I/O")
    }
}
