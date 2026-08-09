plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.scene.physics"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:scene:scene-core"))
            api(project(":awake:physics:api"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
