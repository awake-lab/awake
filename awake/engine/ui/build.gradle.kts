plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.ronjunevaldoz.awake.ui"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "awake-engine-ui"
        }
    }

    jvm("desktop")

    wasmJs {
        browser()
    }

    // Plain Kotlin only, no Vulkan/WebGPU imports -- same backend-neutral-facade role
    // awake-engine-render-api plays for Mesh/Material/DrawCall/Renderer. UiContext talks
    // only to awake-base's Input (hit-testing); each backend module implements the actual
    // draw path (DynamicMesh/UiRenderPipeline) against this module's UiDrawPrimitive.
    sourceSets {
        commonMain.dependencies {
            implementation(project(":awake:base"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
