import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SourcesJar
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.vanniktech.publish)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.ronjunevaldoz.awake.core"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    // iosX64 (Intel simulator) dropped: Compose Multiplatform stopped publishing it
    // after 1.11.0-alpha01 (Apple Silicon only going forward)
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "awake-core"
        }
    }

    jvm("desktop")

    // Web demo (see docs/MVP_PLAN.md's decision log): demo/SceneRuntimeHost.kt
    // (awake-demo:shared, commonMain) uses Input/Key/FixedTimestepLoop from this module
    // and is reused by the wasmJs-only WebGpuApplication.
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:base"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
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
        description.set("Cross-platform app lifecycle: game loop, Application contract, surface glue")
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
