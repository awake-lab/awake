/*
 * Awake
 * Awake
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

rootProject.name = "Awake"

include(":awake:base")
include(":awake:engine")
include(":awake:backend:opengl")
include(":awake:ecs")
include(":awake:ecs:benchmark")
include(":awake:scene")
include(":awake:engine:render-api")
include(":awake:engine:ui")
include(":awake:engine:game")
include(":awake:backend:vulkan")
include(":awake:backend:vulkan:android-native")
include(":awake:backend:webgpu")
include(":awake:backend:vulkan-generator")
include(":awake:physics:api")
include(":awake:backend:jolt")
include(":samples:hello-cube")
include(":samples:hello-cube:androidApp")


pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        // wgpu4k (Phase 2.5 spike, see docs/MVP_PLAN.md) has no stable release yet (last tag
        // v0.1.1, June 2025) -- only snapshots via Sonatype's current Central Portal
        // snapshot repo (not the legacy oss.sonatype.org ones above).
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}
