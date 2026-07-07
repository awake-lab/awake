import Deps.lwjgl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    id("signing-publication-conventions")
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.ronjunevaldoz.awake.core"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
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
                    baseName = "awake-core"
                }
            }
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.components.resources)
            implementation(libs.napier)
            implementation(project(":awake-vulkan"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
            implementation(project.dependencies.platform(lwjgl.bom))
            implementation(lwjgl.lwjgl)
            implementation(lwjgl.glfw)
            implementation(lwjgl.opengl)
            implementation(lwjgl.stb)
            implementation(lwjgl.natives.lwjgl)
            implementation(lwjgl.natives.glfw)
            implementation(lwjgl.natives.opengl)
            implementation(lwjgl.natives.stb)
        }
    }
}


// all items included here will be uploaded once isMainHost=true
// ./gradlew publishAllPublicationsToSonatypeRepository -PisMainHost=true
val publicationsFromMainHost =
    listOf("android", "desktop", "iosArm64", "iosSimulatorArm64", "kotlinMultiplatform")

publishing {
    publications {
        matching { it.name in publicationsFromMainHost }.all {
            val targetPublication = this@all
            tasks.withType<AbstractPublishToMaven>()
                .matching { it.publication == targetPublication }
                .configureEach {
                    onlyIf { findProperty("isMainHost") == "true" }
                }
        }
    }
}

afterEvaluate {
    // TODO find a better way to fix publishAllPublicationsToSonatypeRepository without below config
    tasks.withType<PublishToMavenRepository> {
        dependsOn(tasks.signKotlinMultiplatformPublication)
        val platforms = listOf(
            "AndroidDebug",
            "AndroidRelease",
            "Desktop",
            "IosArm64",
            "IosSimulatorArm64",
        )
        platforms.forEach { platform ->
            dependsOn("sign${platform}Publication")
        }
    }
}