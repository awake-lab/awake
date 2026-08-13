plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.graphics"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:ui:ui-api"))
            implementation(project(":awake:core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":awake:ui:ui-core"))
            implementation(project(":awake:ui:testing"))
        }
    }
}
