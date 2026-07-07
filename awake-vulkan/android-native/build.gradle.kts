/*
 * Awake
 * Awake.awake-vulkan.android-native
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

// Plain Android library that owns the CMake/NDK build and bundled Vulkan
// validation layers. Split out of :awake-vulkan because AGP 9's
// com.android.kotlin.multiplatform.library plugin does not support
// externalNativeBuild; :awake-vulkan's androidMain depends on this module.
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.ronjunevaldoz.awake.vulkan.jni"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    ndkVersion = "26.1.10909125"

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        externalNativeBuild {
            cmake {
                // 64-bit only: the generated JNI marshalling code assumes pointer-sized
                // Vulkan handles, which 32-bit ABIs (uint64_t handles) don't satisfy
                abiFilters += listOf("arm64-v8a", "x86_64")
                cppFlags += listOf("-DVK_USE_PLATFORM_ANDROID_KHR", "-lvulkan")
                arguments += listOf("-DANDROID_TOOLCHAIN=clang", "-DANDROID_STL=c++_static")
            }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("../src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildTypes {
        debug {
            isJniDebuggable = true
        }
    }
}
