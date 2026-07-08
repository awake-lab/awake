# Phase 1b: desktop native build. Compiles the SAME sources android-native builds
# (../src/main/cpp) as a host shared library (.dylib/.so/.dll) instead of an Android
# .so, loaded via System.loadLibrary from awake-vulkan's desktopMain Vulkan.kt.
#
# Vulkan SDK detection per host OS:
#   - macOS has no native Vulkan; MoltenVK (a Vulkan-on-Metal translation layer) is
#     linked directly rather than going through the Vulkan loader, so no ICD/loader
#     environment configuration is needed at run time. Install via `brew install
#     molten-vk` (also provides vulkan/vulkan.h via its bundled headers).
#   - Windows/Linux go through the standard `find_package(Vulkan)` / Vulkan SDK path,
#     since both have a real native Vulkan driver + loader.
#
# See docs/decisions/ for the Android-only code (VkAndroidSurfaceCreateInfoKHRAccessor,
# vkCreateAndroidSurfaceKHR's JNI export) that's guarded out of this build with
# `#ifdef __ANDROID__` rather than duplicated -- desktopMain's Vulkan.kt deliberately
# leaves `vkCreateAndroidSurfaceKHR` as a non-external TODO stub (surface creation on
# desktop is a separate, not-yet-started GLFW-based task), so no JNI symbol for it is
# ever looked up here.
cmake_minimum_required(VERSION 3.22.1)
project(awake-vulkan-desktop)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(JNI REQUIRED)
# Global (not just on the awake-vulkan target): vulkan-kotlin's own CMakeLists.txt
# (add_subdirectory'd below) also needs jni.h for its Accessor/Mutator sources, and it's
# vendored/generated code we'd rather not hand-edit just to add an include path.
include_directories(${JNI_INCLUDE_DIRS})

if(APPLE)
    find_library(MOLTENVK_LIBRARY MoltenVK
            PATHS /opt/homebrew/opt/molten-vk/lib /usr/local/opt/molten-vk/lib)
    if(NOT MOLTENVK_LIBRARY)
        message(FATAL_ERROR "MoltenVK not found -- install via 'brew install molten-vk'")
    endif()
    set(VULKAN_LINK_TARGET ${MOLTENVK_LIBRARY})
    include_directories(/opt/homebrew/include /usr/local/include)
else()
    find_package(Vulkan REQUIRED)
    set(VULKAN_LINK_TARGET Vulkan::Vulkan)
endif()

add_subdirectory(../src/main/cpp/vulkan-kotlin vulkan-kotlin-desktop-build)

add_library(awake-vulkan SHARED
        ../src/main/cpp/awake-vulkan.cpp
        ../src/main/cpp/generated/VulkanBuffers_jni.gen.cpp
        ../src/main/cpp/generated/VulkanDescriptors_jni.gen.cpp
        ../src/main/cpp/generated/VulkanImages_jni.gen.cpp)

target_include_directories(awake-vulkan PRIVATE
        ${JNI_INCLUDE_DIRS}
        ${CMAKE_CURRENT_SOURCE_DIR}/../../tools/jni-binding-generator/scripts)

target_link_libraries(awake-vulkan
        vulkan_kotlin
        ${VULKAN_LINK_TARGET})
