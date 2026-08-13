// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-ownership-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.tailwind"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:ui:ui-api"))
            api(project(":awake:ui:headless"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
