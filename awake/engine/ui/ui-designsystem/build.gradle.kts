
plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-authored-units-convention")
    id("awake.test-resources-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.designsystem"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":awake:base"))
            api(project(":awake:engine:ui:ui-core"))
            api(project(":awake:engine:ui:ui-headless"))
        }
        commonTest.dependencies {
            implementation(project(":awake:base"))
            implementation(project(":awake:engine:ui:ui-testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test"))
        }
    }
}

awakeTestResources {
    roots.from(layout.projectDirectory.dir("src/commonMain/resources"))
}
