// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import org.gradle.api.tasks.JavaExec

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

application {
    mainClass.set("io.github.ronjunevaldoz.awake.fontatlasgenerator.MainKt")
}

dependencies {
    implementation(libs.kotlinpoet)
}

// `application`'s own `run` task doesn't set a stable workingDir, and the generator writes to a
// path relative to this module's project dir (matching :awake:engine:ui:tailwind-generator's
// Main.kt convention) -- register an explicit task so regeneration is reproducible from any cwd.
// Run via `./gradlew :awake:engine:ui:font-atlas-generator:generateFontAtlas` and commit the diff.
tasks.register<JavaExec>("generateFontAtlas") {
    group = "generation"
    description = "Regenerate RobotoRegularUiFontData.kt from the Roboto TTF's own outline " +
        "geometry -- run this and commit the diff after any font or atlas metric change."
    dependsOn("classes")
    workingDir = projectDir
    mainClass.set("io.github.ronjunevaldoz.awake.fontatlasgenerator.MainKt")
    classpath = sourceSets.main.get().runtimeClasspath
}
