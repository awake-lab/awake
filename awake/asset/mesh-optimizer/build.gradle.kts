// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import org.gradle.api.tasks.JavaExec

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

application {
    mainClass.set("io.github.ronjunevaldoz.awake.asset.meshoptimizer.MainKt")
}

dependencies {
    implementation(project(":awake:core:geometry"))
    implementation(project(":awake:asset:gltf"))
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}

// Mirrors :awake:ui:tailwind-generator's own `generateTailwindScale` task -- `application`'s
// own `run` task doesn't set a stable workingDir, and CLI args (input/output/ratio) are
// naturally passed the same way. Run via
// `./gradlew :awake:asset:mesh-optimizer:decimate --args="in.gltf out.gltf 0.5"`.
tasks.register<JavaExec>("decimate") {
    group = "generation"
    description = "Simplify a .gltf mesh's triangle count -- args: <input.gltf> <output.gltf> <targetRatio>"
    dependsOn("classes")
    workingDir = projectDir
    mainClass.set("io.github.ronjunevaldoz.awake.asset.meshoptimizer.MainKt")
    classpath = sourceSets.main.get().runtimeClasspath
    if (project.hasProperty("args")) {
        args((project.property("args") as String).split(" "))
    }
}
