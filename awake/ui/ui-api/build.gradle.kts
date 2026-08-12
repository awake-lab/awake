plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.api"
    }

    sourceSets {
        commonMain.dependencies {
            // Theme value contracts use Awake's portable color value, not UI runtime types.
            api(project(":awake:core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
