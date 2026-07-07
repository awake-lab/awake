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
    alias(libs.plugins.android.library)
}

kotlin {
    jvmToolchain(17)

    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "awake-vulkan"
        }
    }

    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                // this will ensure that all vulkan layer validation .so is included
                implementation(fileTree("src/main/jniLibs") {
                    include("**/*.so")
                })
                implementation(libs.leakcanary.android)
            }
        }
    }
}

android {
    namespace = "io.github.ronjunevaldoz.awake.vulkan"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    ndkVersion = "26.1.10909125"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].jniLibs.srcDirs("src/main/jniLibs")

    defaultConfig {
        minSdk = 24
        externalNativeBuild {
            cmake {
                // 64-bit only: the generated JNI marshalling code assumes pointer-sized
                // Vulkan handles, which 32-bit ABIs (uint64_t handles) don't satisfy
                abiFilters += listOf("arm64-v8a", "x86_64")
                cppFlags += listOf("-DVK_USE_PLATFORM_ANDROID_KHR", "-lvulkan")
//                "-DVKB_VALIDATION_LAYERS=OFF"
                arguments += listOf("-DANDROID_TOOLCHAIN=clang", "-DANDROID_STL=c++_static")
            }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
            buildStagingDirectory("build-native")
        }
    }
    buildTypes {
        debug {
            isJniDebuggable = true
        }
    }
    buildFeatures {
        viewBinding = true
        prefab = true
    }
}