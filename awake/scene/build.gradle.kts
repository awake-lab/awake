/*
 * Awake
 * Awake.awake-scene
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SourcesJar
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.vanniktech.publish)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.ronjunevaldoz.awake.scene"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "awake-scene"
        }
    }

    jvm("desktop")

    // Web demo (see docs/MVP_PLAN.md's decision log): demo/SceneRuntimeHost.kt
    // (awake-demo:shared, commonMain) depends on this module and is reused by the
    // wasmJs-only WebGpuApplication.
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:base"))
            api(project(":awake:ecs"))
            api(project(":awake:engine:game"))
            api(project(":awake:engine:ui-core"))
            // Module restructuring slice 1 (see docs/MVP_PLAN.md): RenderSystem/MeshRenderer
            // only ever touch the backend-neutral Mesh/Material/Renderer/DrawCall contract,
            // never awake-vulkan's concrete Vulkan bindings -- depending on just the
            // interface module (instead of all of awake-vulkan) is the actual point of this
            // restructuring.
            api(project(":awake:engine:render-api"))
            // PhysicsBody/PhysicsSystem only ever touch the backend-neutral PhysicsWorld
            // contract, never a concrete backend (`awake:backend:jolt`) -- same restructuring
            // rationale as the render-api dependency above.
            api(project(":awake:physics:api"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        // MVP1a NavMesh slice (see docs/MMORPG_ROADMAP.md): recast4j is a plain-Java
        // Recast/Detour port with zero native code -- it only runs on the JVM, so it's
        // scoped to desktop+Android's own source sets rather than commonMain (the same
        // "JVM-only dependency can't live in commonMain" lesson wgpu4k/kotlinx-browser
        // already taught this project, just for the opposite platform set). iOS/wasmJs get
        // a `null`-returning actual instead (see navigation/DemoNavMesh.kt).
        named("desktopMain") {
            dependencies {
                implementation(libs.recast4j.recast)
                implementation(libs.recast4j.detour)
            }
        }
        named("androidMain") {
            dependencies {
                implementation(libs.recast4j.recast)
                implementation(libs.recast4j.detour)
            }
        }
    }
}

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
        name.set("Awake Scene")
        description.set("Awake ECS scene components and systems")
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
