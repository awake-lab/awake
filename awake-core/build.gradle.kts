import Deps.lwjgl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    id("signing-publication-conventions")
}

kotlin {
    jvmToolchain(17)

    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    val iosArm64 = iosArm64()
    val iosX64 = iosX64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    val appleTargets = listOf(
        iosX64,
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
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.components.resources)
                implementation(libs.napier)
                implementation(project(":awake-vulkan"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(platform(lwjgl.bom))
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
}


// all items included here will be uploaded once isMainHost=true
// ./gradlew publishAllPublicationsToSonatypeRepository -PisMainHost=true
val publicationsFromMainHost =
    listOf("android", "desktop", "iosX64", "iosArm64", "iosSimulatorArm64", "kotlinMultiplatform")

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

android {
    namespace = "io.github.ronjunevaldoz.awake.core"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        multipleVariants {
            withSourcesJar()
            withJavadocJar()
            allVariants()
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
            "IosX64",
            "IosArm64",
            "IosSimulatorArm64",
        )
        platforms.forEach { platform ->
            dependsOn("sign${platform}Publication")
        }
    }
}