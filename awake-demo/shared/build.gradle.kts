/*
 * Awake
 * Awake.awake-demo.shared
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

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.download)
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.ronjunevaldoz.awake.demo.common"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    jvm("desktop")

    // iosX64 (Intel simulator) dropped: Compose Multiplatform stopped publishing it
    // after 1.11.0-alpha01 (Apple Silicon only going forward)
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "Awake Demo Compose"
        homepage = "io.github.ronjunevaldoz/awake-demo"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
        extraSpecAttributes["resources"] =
            "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.components.resources)
            implementation(project(":awake-vulkan"))
            implementation(project(":awake-core"))
        }
        androidMain.dependencies {
            api(libs.androidx.activity.compose)
            api(libs.androidx.appcompat)
            api(libs.androidx.core.ktx)
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.common)
        }
    }
}


val glslangDownload =
    tasks.register<de.undercouch.gradle.tasks.download.Download>("glslangDownload") {
        val osName = System.getProperty("os.name").lowercase()
        val hostFile = when {
            osName.contains("mac") -> "main-osx"
            osName.contains("win") -> "master-windows"
            osName.contains("linux") -> "main-linux"
            else -> throw Exception("$osName not supported")
        }
        src("https://github.com/KhronosGroup/glslang/releases/download/main-tot/glslang-$hostFile-Release.zip")
        dest(layout.buildDirectory.file("glslang.zip"))
    }

val glslangDownloadCopy = tasks.register<Copy>("glslangDownloadCopy") {
    dependsOn(glslangDownload)
    from(zipTree(layout.buildDirectory.file("glslang.zip")))
    into(layout.buildDirectory.dir("glslang"))
}

// A plain Exec task only ever runs the LAST commandLine(...) call configured on it --
// calling commandLine(...) once per shader inside a loop silently discards every prior
// call, so only one shader (whichever the filesystem happens to enumerate last) was ever
// actually compiled. Runs each glslangValidator invocation directly instead, so every
// .frag/.vert under shaderDir gets a fresh .spv.
tasks.register("glslValidator") {
    dependsOn(glslangDownloadCopy)

    val bin = layout.buildDirectory.dir("glslang/bin").get().asFile.path
    val shadersDir = file("src/commonMain/resources/assets/shader/vulkan")
    val shaders = project.fileTree(shadersDir) {
        include("**/*.frag", "**/*.vert")
    }

    doLast {
        shaders.forEach { shaderFile ->
            val spvFile = File(shadersDir, "${shaderFile.name}.spv")
            val process = ProcessBuilder(
                "$bin/glslangValidator", "-V", shaderFile.absolutePath, "-o", spvFile.absolutePath
            ).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw RuntimeException("glslangValidator failed for ${shaderFile.name} (exit $exitCode):\n$output")
            }
        }
    }
}

tasks.register<JavaExec>("runVulkanCpp") {
    mainClass.set("io.github.ronjunevaldoz.awake.vulkan_generator.MainKt")
    classpath = project(":awake-vulkan-generator").sourceSets["main"].runtimeClasspath
    args(project(":awake-vulkan-generator").rootDir.path)
}
// glslValidator is a manual step, run explicitly after changing any .vert/.frag under
// src/commonMain/resources/assets/shader/vulkan -- NOT wired as an automatic dependency
// of any compile task. It used to (incorrectly) target `JavaCompile`, a task type this
// KMP module (Kotlin/Android/desktop only, no java sources) never has, so it silently
// never ran and shader edits could go uncompiled without any build failure to catch it
// (see docs/decisions/D10-codegen-derisk-findings.md, Round 6, for how this surfaced).
// Same manual convention as :awake-vulkan:android-native's generateJniBindings.