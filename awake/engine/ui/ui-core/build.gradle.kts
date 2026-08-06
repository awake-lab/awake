
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
            implementation(project(":awake:base"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
