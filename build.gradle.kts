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

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.library.kmp) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.vanniktech.publish) apply false
}

allprojects {
    group = "io.github.ronjunevaldoz"
    version = "1.0.0-SNAPSHOT"
}

tasks.register("developerDocs") {
    group = "documentation"
    description = "Build developer-facing API references and tutorial artifacts."
    dependsOn(
        ":awake:base:dokkaGeneratePublicationHtml",
        ":awake:ecs:dokkaGeneratePublicationHtml",
        ":awake:engine:game:dokkaGeneratePublicationHtml",
        ":awake:engine:game-dsl:dokkaGeneratePublicationHtml",
        ":awake:engine:game-dsl:desktopTest",
        ":awake:engine:game-dsl:gameDslTutorialDocsReport",
        ":awake:engine:game-dsl:uiDslTutorialDocsReport",
        ":awake:engine:render-api:dokkaGeneratePublicationHtml",
        ":awake:engine:ui-core:dokkaGeneratePublicationHtml",
        ":awake:engine:ui-designsystem:dokkaGeneratePublicationHtml",
        ":awake:engine:ui-unstyled:dokkaGeneratePublicationHtml",
        ":awake:physics:api:dokkaGeneratePublicationHtml",
        ":awake:scene:dokkaGeneratePublicationHtml",
        ":awake:engine:ui-unstyled:desktopTest",
        ":awake:engine:ui-unstyled:uiSnapshotReport",
        ":awake:engine:ui-unstyled:uiTutorialDocsReport"
    )
}
