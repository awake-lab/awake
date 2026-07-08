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
    alias(libs.plugins.download) apply false
    // dokka comes from the buildSrc classpath (needed by signing-publication-conventions),
    // so it must not be re-requested here with a version
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.vanniktech.publish) apply false
}

allprojects {
    group = "io.github.ronjunevaldoz"
    version = "1.0.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        toolVersion = rootProject.libs.versions.detekt.get()
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        baseline = file("detekt-baseline.xml")
    }

    // the default detekt task only scans src/main/kotlin; widen it to all KMP source sets
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        setSource(files("src"))
        include("**/*.kt")
        exclude("**/build/**", "**/attic/**")
    }
    tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
        setSource(files("src"))
        include("**/*.kt")
        exclude("**/build/**", "**/attic/**")
    }
}