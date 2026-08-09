plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.scene.core"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:core"))
            api(project(":awake:ecs"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
