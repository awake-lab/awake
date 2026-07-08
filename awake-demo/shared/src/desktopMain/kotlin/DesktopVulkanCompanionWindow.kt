/*
 * Awake
 * Awake.awake-demo.shared.desktopMain
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

// Launches VulkanDesktopMain (awake-demo:desktopApp's real GLFW+MoltenVK window) as a
// separate JVM process instead of driving GLFW's event loop from inside Compose Desktop's
// own process. Both GLFW (window creation/event polling) and AWT/Skiko need to own "the
// main thread" on macOS Cocoa -- interleaving the two loops in a single process would need
// real event-loop integration work, whereas a companion process sidesteps the conflict for
// free. Reuses the exact classpath already loaded into this process (VulkanDesktopMainKt
// lives in the same module/source set) plus the same java.library.path/
// -XstartOnFirstThread/VK_ICD_FILENAMES/DYLD_FALLBACK_LIBRARY_PATH requirements wired for
// the standalone `runVulkanDesktop` Gradle task.
object DesktopVulkanCompanionWindow {
    private var process: Process? = null

    fun launch() {
        if (process?.isAlive == true) return
        val javaBin = ProcessHandle.current().info().command().orElse("java")
        val classpath = System.getProperty("java.class.path")
        val nativeLibsDir = System.getProperty("awake.vulkan.nativeLibsDir")
        val icdFilenames = System.getProperty("awake.vulkan.icdFilenames")
        val dyldFallbackLibraryPath = System.getProperty("awake.vulkan.dyldFallbackLibraryPath")

        val args = mutableListOf(javaBin)
        if (nativeLibsDir != null) {
            args += "-Djava.library.path=$nativeLibsDir"
        }
        if (System.getProperty("os.name").lowercase().contains("mac")) {
            args += "-XstartOnFirstThread"
        }
        args += listOf("-cp", classpath, "VulkanDesktopMainKt")

        val builder = ProcessBuilder(args).inheritIO()
        // These are system properties (-D...) on THIS process, not inherited OS
        // environment variables, so they must be copied into the child's environment map
        // explicitly rather than relying on ProcessBuilder's default env inheritance.
        if (icdFilenames != null) {
            builder.environment()["VK_ICD_FILENAMES"] = icdFilenames
        }
        if (dyldFallbackLibraryPath != null) {
            builder.environment()["DYLD_FALLBACK_LIBRARY_PATH"] = dyldFallbackLibraryPath
        }
        process = builder.start()
    }

    fun close() {
        process?.destroy()
        process = null
    }
}
