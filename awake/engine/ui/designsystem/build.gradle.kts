plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-ownership-convention")
    id("awake.ui-authored-units-convention")
    id("awake.test-resources-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.designsystem"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":awake:core"))
            // Design-system signatures may expose stable UI values, never raw runtime types.
            api(project(":awake:engine:ui:ui-api"))
            api(project(":awake:engine:ui:headless"))
        }
        commonTest.dependencies {
            implementation(project(":awake:core"))
            implementation(project(":awake:engine:ui:testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test"))
        }
    }
}

awakeTestResources {
    roots.from(layout.projectDirectory.dir("src/commonMain/resources"))
}
