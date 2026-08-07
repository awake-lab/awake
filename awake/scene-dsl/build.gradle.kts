plugins {
    id("awake.kmp-library-convention")
    alias(libs.plugins.kotlin.serialization)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.scene.dsl"
    }

    sourceSets {
        commonMain.dependencies {
            // Depends on the specific scene leaf modules it actually uses, not the
            // `:awake:scene` facade -- see docs/tasks/2026-08-05-scene-module-split-proposal.md
            // Phase 5. `TransformSystem` lives in `:awake:scene:core`,
            // `PlayerControlSystem` lives in this module directly (it needs `ui-core`'s
            // `UiInputOwnership`, which `:awake:scene:controls` deliberately stays free of).
            api(project(":awake:scene:core"))
            api(project(":awake:scene:rendering"))
            api(project(":awake:scene:controls"))
            api(project(":awake:scene:runtime"))
            api(project(":awake:engine:game-dsl"))
            api(project(":awake:engine:ui:ui-core"))
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(project(":awake:base"))
            implementation(project(":awake:engine:ui:ui-headless"))
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
