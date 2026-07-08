/*
 * Awake
 * Awake.awake-vulkan
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
    alias(libs.plugins.android.library.kmp)
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.ronjunevaldoz.awake.vulkan"
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
            baseName = "awake-vulkan"
        }
    }

    jvm("desktop")

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            // CMake/NDK build + bundled validation layers (AGP 9 KMP plugin
            // has no externalNativeBuild support, so a plain library owns it)
            api(project(":awake-vulkan:android-native"))
            implementation(libs.leakcanary.android)
        }
    }
}

// Phase 1b: desktop native build (see awake-vulkan/desktop-native/CMakeLists.txt for the
// Vulkan-SDK-per-host-OS rationale). Manual/on-demand, like android-native's
// generateJniBindings -- NOT wired as an automatic dependency of compileKotlinDesktop or
// desktopTest, because CMake configure+build is slow and this native lib only changes when
// the C++ sources themselves change, not on every Kotlin edit. Run explicitly:
//   ./gradlew :awake-vulkan:configureDesktopNative :awake-vulkan:buildDesktopNative
val desktopNativeBuildDir = layout.buildDirectory.dir("desktop-native")
val desktopNativeLibDir = layout.buildDirectory.dir("desktop-native-libs")

tasks.register<Exec>("configureDesktopNative") {
    group = "native"
    description = "Configure the desktop native build (CMake) -- run after any C++ source change."
    workingDir = desktopNativeBuildDir.get().asFile.also { it.mkdirs() }
    commandLine(
        "cmake",
        "-S", layout.projectDirectory.dir("desktop-native").asFile.absolutePath,
        "-B", desktopNativeBuildDir.get().asFile.absolutePath,
        "-DCMAKE_BUILD_TYPE=Debug"
    )
}

tasks.register<Exec>("buildDesktopNative") {
    group = "native"
    description = "Build the desktop native library (.dylib/.so/.dll) and copy it where the " +
        "desktop JVM's System.loadLibrary(\"awake-vulkan\") can find it (-Djava.library.path)."
    dependsOn("configureDesktopNative")
    workingDir = desktopNativeBuildDir.get().asFile
    commandLine("cmake", "--build", desktopNativeBuildDir.get().asFile.absolutePath)
    doLast {
        val libDir = desktopNativeLibDir.get().asFile.also { it.mkdirs() }
        val built = desktopNativeBuildDir.get().asFile.walkTopDown()
            .filter { it.isFile && it.name.matches(Regex("lib?awake-vulkan\\.(dylib|so|dll)")) }
            .firstOrNull()
            ?: throw GradleException("Built awake-vulkan native library not found under $desktopNativeBuildDir")
        built.copyTo(File(libDir, built.name), overwrite = true)
        println("Desktop native library copied to: ${File(libDir, built.name)}")
    }
}

// Always points java.library.path at desktop-native-libs for desktop tests -- a no-op if
// buildDesktopNative hasn't been run (System.loadLibrary just fails with its usual
// UnsatisfiedLinkError in that case, same as if this weren't set at all).
tasks.named<Test>("desktopTest") {
    jvmArgs("-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
}
