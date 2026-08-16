// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("awake.kmp-library-convention")
    id("awake.publish-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.core.geometry"
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

mavenPublishing {
    pom {
        name.set("Awake Core Geometry")
        description.set("Portable mesh geometry math: integer-normalization decode, mesh simplification")
    }
}
