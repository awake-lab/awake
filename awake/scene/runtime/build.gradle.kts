plugins {
    id("awake.kmp-library-convention")
    alias(libs.plugins.kotlin.serialization)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.test-resources-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.scene.runtime"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:scene:core"))
            api(project(":awake:scene:rendering"))
            api(project(":awake:base"))
            api(project(":awake:ecs"))
            api(project(":awake:engine:game"))
            api(project(":awake:engine:ui:ui-core"))
            // Needed by SceneGameFrame.kt's frameStats() -- textLayoutCacheStats() lives in
            // ui-headless, not ui-core.
            implementation(project(":awake:engine:ui:ui-headless"))
            api(project(":awake:engine:render-api"))
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

awakeTestResources {
    roots.from(layout.projectDirectory.dir("src/commonTest/resources"))
}
