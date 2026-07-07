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

import org.gradle.kotlin.dsl.support.serviceOf

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
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("de.undercouch.download")
}

kotlin {
    jvmToolchain(17)

    androidTarget()

    jvm("desktop")

    iosX64()
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
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
                implementation(compose.components.resources)
                implementation(project(":awake-vulkan"))
                implementation(project(":awake-core"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.9.3")
                api("androidx.appcompat:appcompat:1.7.0")
                api("androidx.core:core-ktx:1.15.0")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "io.github.ronjunevaldoz.awake.demo.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(project(mapOf("path" to ":awake-vulkan")))
}

val glslangDownload =
    tasks.register<de.undercouch.gradle.tasks.download.Download>("glslangDownload") {
        val host: TargetMachine =
            gradle.serviceOf<org.gradle.nativeplatform.internal.DefaultTargetMachineFactory>()
                .host()
        val os = host.operatingSystemFamily
        val hostFile = when {
            os.isMacOs -> "main-osx"
            os.isWindows -> "master-windows"
            os.isLinux -> "main-linux"
            else -> throw Exception("${os.name} not supported")
        }
        src("https://github.com/KhronosGroup/glslang/releases/download/main-tot/glslang-$hostFile-Release.zip")
        dest(file("${buildDir}/glslang.zip"))
    }

val glslangDownloadCopy = tasks.register<Copy>("glslangDownloadCopy") {
    dependsOn(glslangDownload)
    from(zipTree(file("$buildDir/glslang.zip")))
    into(layout.buildDirectory.dir("glslang"))
}

tasks.create<Exec>("glslValidator") {
    dependsOn(glslangDownloadCopy)

    val commonResourceDir = android.sourceSets["main"].resources.srcDirs.first { srcDir ->
        srcDir.toString().contains("common")
    }

    val bin = "$buildDir/glslang/bin"

    val shadersDir = File(commonResourceDir, "assets/shader/vulkan")
    val outputSpv = shadersDir.path

    val shaders = project.fileTree(shadersDir) {
        include("**/*.frag", "**/*.vert")
    }

    shaders.forEach { shaderFile ->
        val shaderExt = shaderFile.extension
        val shaderName =
            if (shaderExt == "frag" || shaderExt == "vert") shaderFile.name else shaderFile.nameWithoutExtension
        val spvFile = File(outputSpv, "$shaderName.spv")
        commandLine(
            "$bin/glslangValidator",
            "-V",
            shaderFile.absolutePath,
            "-o",
            spvFile.absolutePath
        )
    }
}

tasks.register<JavaExec>("runVulkanCpp") {
    mainClass.set("io.github.ronjunevaldoz.awake.vulkan_generator.MainKt")
    classpath = project(":awake-vulkan-generator").sourceSets["main"].runtimeClasspath
    args(project(":awake-vulkan-generator").rootDir.path)
}
// After evaluating the project configuration
afterEvaluate {
    // Access the preCompileShaders task and make it run before Java compilation tasks
    tasks.withType(JavaCompile::class.java) {
//        dependsOn(tasks.named("runVulkanCpp"))
        dependsOn(tasks.named("glslValidator"))
    }
}