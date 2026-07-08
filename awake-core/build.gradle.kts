import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SourcesJar
import java.util.Properties
import Deps.lwjgl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.vanniktech.publish)
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


// iOS artifacts can only be built/signed for real on a macOS host -- gate those
// publications so a non-mac CI runner (or a mac runner not doing the release) doesn't
// attempt them. All items included here will be uploaded once isMainHost=true.
// ./gradlew publishToMavenCentral -PisMainHost=true
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

// Sonatype's legacy OSSRH staging API (s01.oss.sonatype.org) was sunset in June 2025; the
// vanniktech plugin publishes through the current Central Portal instead. Its credentials
// are a Central Portal user token (from central.sonatype.com/account), not the old Sonatype
// JIRA username/password. The plugin reads them under its own property names
// (mavenCentralUsername/mavenCentralPassword/signingInMemoryKey*) via normal Gradle project
// property resolution, so local dev just needs them in local.properties and CI needs them
// as ORG_GRADLE_PROJECT_-prefixed environment variables.
val secretPropsFile = rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
}

mavenPublishing {
    // No host parameter as of plugin 0.36+: Sonatype's legacy OSSRH staging API is gone, so
    // Central Portal is the only (and therefore default) target.
    publishToMavenCentral()
    signAllPublications()

    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = SourcesJar.Sources(),
            androidVariantsToPublish = listOf("release"),
        )
    )

    pom {
        name.set("Awake")
        description.set("Cross-platform OpenGL & Vulkan graphics")
        url.set("https://ronjunevaldoz.github.io/awake")
        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        issueManagement {
            system.set("Github")
            url.set("https://github.com/ronjunevaldoz/awake/issues")
        }
        scm {
            connection.set("https://github.com/ronjunevaldoz/awake.git")
            url.set("https://github.com/ronjunevaldoz/awake")
        }
        developers {
            developer {
                name.set("Ron June Valdoz")
                email.set("ronjune.lopez@gmail.com")
            }
        }
    }
}