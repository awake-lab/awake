// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

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
