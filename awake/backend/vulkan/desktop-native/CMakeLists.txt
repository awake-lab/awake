# Phase 1b: desktop native build. Compiles the SAME sources android-native builds
# (../src/main/cpp) as a host shared library (.dylib/.so/.dll) instead of an Android
# .so, loaded via System.loadLibrary from awake-vulkan's desktopMain Vulkan.kt.
#
# Vulkan SDK detection per host OS:
#   - macOS has no native Vulkan; MoltenVK (`brew install molten-vk`) provides the ICD.
#     Originally linked directly against libMoltenVK.dylib (bypassing the loader) --
#     that's enough for a bare vkCreateInstance, but GLFW's own Vulkan support detection
#     (glfwVulkanSupported/glfwGetRequiredInstanceExtensions) specifically dlopen()s the
#     standard Vulkan *loader* library by name and fails to find "vulkan support" if only
#     MoltenVK is linked directly (confirmed empirically -- glfwGetRequiredInstanceExtensions
#     returned null even though vkCreateInstance via direct-linked MoltenVK worked fine).
#     Fixed by linking the real loader (`brew install vulkan-loader`) instead, with
#     VK_ICD_FILENAMES pointing the loader at MoltenVK's ICD manifest at run time (set in
#     awake-vulkan/build.gradle.kts's desktopTest/verifyGlfwMain tasks) -- the standard
#     Vulkan loader/ICD mechanism, not a workaround.
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
    find_library(VULKAN_LOADER_LIBRARY NAMES vulkan vulkan.1
            PATHS /opt/homebrew/opt/vulkan-loader/lib /usr/local/opt/vulkan-loader/lib
                  /opt/homebrew/lib /usr/local/lib)
    if(NOT VULKAN_LOADER_LIBRARY)
        message(FATAL_ERROR "Vulkan loader not found -- install via 'brew install vulkan-loader'")
    endif()
    # MoltenVK itself only needs to be present on disk for the loader to find via its ICD
    # manifest (VK_ICD_FILENAMES, set at run time) -- not linked here.
    find_path(MOLTENVK_ICD_DIR MoltenVK_icd.json
            PATHS /opt/homebrew/Cellar/molten-vk/*/etc/vulkan/icd.d
                  /usr/local/Cellar/molten-vk/*/etc/vulkan/icd.d)
    if(NOT MOLTENVK_ICD_DIR)
        message(FATAL_ERROR "MoltenVK ICD manifest not found -- install via 'brew install molten-vk'")
    endif()
    message(STATUS "MoltenVK ICD manifest: ${MOLTENVK_ICD_DIR}/MoltenVK_icd.json (set VK_ICD_FILENAMES to this at run time)")
    set(VULKAN_LINK_TARGET ${VULKAN_LOADER_LIBRARY})
    include_directories(/opt/homebrew/include /usr/local/include)
else()
    find_package(Vulkan REQUIRED)
    set(VULKAN_LINK_TARGET Vulkan::Vulkan)
endif()

# GLFW (`brew install glfw`) provides windowing + Vulkan surface creation
# (VulkanWindow_jni.gen.cpp / glfwCreateWindowSurface) for the desktop demo.
find_library(GLFW_LIBRARY glfw PATHS /opt/homebrew/opt/glfw/lib /usr/local/opt/glfw/lib)
find_path(GLFW_INCLUDE_DIR GLFW/glfw3.h PATHS /opt/homebrew/opt/glfw/include /usr/local/opt/glfw/include)
if(NOT GLFW_LIBRARY OR NOT GLFW_INCLUDE_DIR)
    message(FATAL_ERROR "GLFW not found -- install via 'brew install glfw'")
endif()
include_directories(${GLFW_INCLUDE_DIR})

add_subdirectory(../src/main/cpp/vulkan-kotlin vulkan-kotlin-desktop-build)

# io_github_..._VulkanBuffers/Descriptors/Images -- fully-qualified filenames because
# jni-binding-generator qualifies output whenever more than one Kotlin source file shares
# a class name (androidMain and desktopMain both declare real `actual external fun` for
# these). See that file's own header comment. This is the SAME file android-native
# compiles, unmodified -- these Vulkan calls are identical on both platforms.
add_library(awake-vulkan SHARED
        ../src/main/cpp/awake-vulkan.cpp
        ../src/main/cpp/generated/io_github_ronjunevaldoz_awake_vulkan_gen_VulkanBuffers_jni.gen.cpp
        ../src/main/cpp/generated/io_github_ronjunevaldoz_awake_vulkan_gen_VulkanDescriptors_jni.gen.cpp
        ../src/main/cpp/generated/io_github_ronjunevaldoz_awake_vulkan_gen_VulkanImages_jni.gen.cpp
        ../src/main/cpp/generated/VulkanWindow_jni.gen.cpp)

target_include_directories(awake-vulkan PRIVATE
        ${JNI_INCLUDE_DIRS}
        ${CMAKE_CURRENT_SOURCE_DIR}/../../tools/jni-binding-generator/scripts)

target_compile_definitions(awake-vulkan PRIVATE GLFW_INCLUDE_VULKAN)

target_link_libraries(awake-vulkan
        vulkan_kotlin
        ${VULKAN_LINK_TARGET}
        ${GLFW_LIBRARY})
