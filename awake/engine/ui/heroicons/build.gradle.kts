plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-ownership-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.heroicons"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:engine:ui:ui-api"))
            api(project(":awake:engine:ui:ui-core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
