plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-ownership-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.core"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:ui:ui-api"))
            api(project(":awake:ui:ui-graphics"))
            api(project(":awake:ui:ui-text"))
            implementation(project(":awake:core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
