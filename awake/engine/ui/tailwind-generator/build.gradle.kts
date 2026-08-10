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
    mainClass.set("io.github.ronjunevaldoz.awake.tailwindgenerator.MainKt")
}

dependencies {
    implementation(libs.kotlinpoet)
}

// `application`'s own `run` task doesn't set a stable workingDir, and the generator writes to a
// path relative to this module's project dir -- register an explicit task so regeneration is
// reproducible from any cwd. Run via
// `./gradlew :awake:engine:ui:tailwind-generator:generateTailwindScale` and commit the diff.
tasks.register<JavaExec>("generateTailwindScale") {
    group = "generation"
    description = "Regenerate Tw.kt from the vendored Tailwind spacing scale -- run this and " +
        "commit the diff if the vendored scale changes."
    dependsOn("classes")
    workingDir = projectDir
    mainClass.set("io.github.ronjunevaldoz.awake.tailwindgenerator.MainKt")
    classpath = sourceSets.main.get().runtimeClasspath
}
