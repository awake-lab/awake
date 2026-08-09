plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    id("awake.publish-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

private val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "FreeBSD", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) }) {
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            } else {
                "natives-linux"
            }

        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
            "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"

        arrayOf("Windows").any { name.startsWith(it) } ->
            if (arch.contains("64")) {
                "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
            } else {
                "natives-windows-x86"
            }

        else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.ronjunevaldoz.awake.opengl"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
        withHostTest {}
    }
    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    // iosX64 (Intel simulator) dropped: Compose Multiplatform stopped publishing it
    // after 1.11.0-alpha01 (Apple Silicon only going forward)
    val appleTargets = listOf(
        iosArm64,
        iosSimulatorArm64
    )

    appleTargets.forEach { target ->
        with(target) {
            binaries {
                framework {
                    baseName = "awake-opengl"
                }
            }
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            // AwakeContext/Config/rendering wrappers need Bitmap/readResourceBytes/Mat4 --
            // see docs/MVP_PLAN.md's Decision Log, D11 follow-up, for the split this module
            // boundary comes from.
            implementation(project(":awake:core"))
            // AwakeContext.init mirrors fps/ups into EngineConfigHolder so awake-engine's
            // GameLoop actuals keep working without depending on this (or any) backend.
            implementation(project(":awake:core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.components.resources)
            implementation(libs.napier)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
            implementation(project.dependencies.platform(libs.lwjgl.bom))
            implementation(libs.lwjgl.asProvider())
            implementation(libs.lwjgl.glfw)
            implementation(libs.lwjgl.opengl)
            implementation(libs.lwjgl.stb)
            implementation("${libs.lwjgl.asProvider().get()}:$lwjglNatives")
            implementation("${libs.lwjgl.glfw.get()}:$lwjglNatives")
            implementation("${libs.lwjgl.opengl.get()}:$lwjglNatives")
            implementation("${libs.lwjgl.stb.get()}:$lwjglNatives")
        }
    }
}

mavenPublishing {
    pom {
        name.set("Awake OpenGL")
        description.set("Awake's legacy OpenGL rendering backend")
    }
}
