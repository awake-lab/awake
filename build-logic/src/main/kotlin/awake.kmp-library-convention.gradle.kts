plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvmToolchain(17)

    android {
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
        withHostTest {}
    }

    jvm("desktop")

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = project.path.removePrefix(":").replace(":", "-")
        }
    }

    // Opt-out for modules whose API cannot exist in a browser (backend:vulkan has no wasm
    // actuals -- browsers speak WebGPU, not Vulkan). Set awake.target.wasmJs=false in the
    // module's gradle.properties.
    if (findProperty("awake.target.wasmJs") != "false") {
        @Suppress("OPT_IN_USAGE")
        wasmJs {
            browser()
        }
    }
}
