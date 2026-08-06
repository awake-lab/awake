plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.scene.rendering"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:scene:core"))
            api(project(":awake:engine:render-api"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
