plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

// Distinct from :awake:core:animation's published coordinate -- both projects' Gradle path
// leaf is "animation", and the root's shared `group = "io.github.awake-lab"` made them publish
// as the SAME io.github.awake-lab:animation coordinate. Gradle silently conflict-resolves that
// to one project, and the other's classes never reach any consumer's runtime/link classpath --
// the actual cause of NoClassDefFoundError/IrLinkageError crashes referencing
// UiAnimatedVisibilityKt (ui-headless's withGraphicsLayerAlpha and friends).
group = "io.github.awake-lab.ui"

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.animation"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:ui:ui-core"))
            implementation(project(":awake:core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":awake:ui:ui-core"))
            implementation(project(":awake:ui:testing"))
        }
    }
}
