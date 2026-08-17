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
        namespace = "io.github.ronjunevaldoz.awake.core.animation"
    }

    sourceSets {
        commonMain.dependencies {
            // Mat4/Quat/Vec3 -- bone transforms, keyframe interpolation, joint-palette math.
            implementation(project(":awake:core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

mavenPublishing {
    pom {
        name.set("Awake Core Animation")
        description.set("Engine-neutral skeletal animation: skeleton/clip/pose sampling, crossfade blending")
    }
}
