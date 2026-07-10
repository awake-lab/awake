import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
}

kotlin {
    jvmToolchain(17)
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            // VulkanDesktopMain.kt needs Application/DesktopGameLoop (app-lifecycle glue).
            implementation(project(":awake-core"))
            // runOpenGlFrameDemo() (main.kt) uses AwakeContext.init/createFrame, both now
            // in awake-opengl (see docs/MVP_PLAN.md's Decision Log, D11 follow-up).
            implementation(project(":awake-opengl"))
            implementation(project(":awake-demo:shared"))
            // VulkanWindow (GLFW) for VulkanDesktopMain.kt's real Vulkan window/render loop.
            implementation(project(":awake-vulkan"))
        }
    }

}

// Same MoltenVK ICD lookup `runVulkanDesktop` (below) has always done -- factored out so
// the plain `run`/compose.desktop.application task can also hand these paths to
// DesktopVulkanCompanionWindow (as system properties, since that code spawns ITS OWN child
// process and needs to set the child's environment explicitly rather than rely on
// inherited env vars).
val desktopNativeLibDir = project(":awake-vulkan").layout.buildDirectory.dir("desktop-native-libs")
val moltenVkIcdPath = fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
    .plus(fileTree("/usr/local/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") })
    .files.firstOrNull()?.absolutePath
val dyldFallbackLibraryPath = "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib"

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KotlinMultiplatformComposeDesktopApplication"
            packageVersion = "1.0.0"
        }
        // Deliberately NOT setting -XstartOnFirstThread here (see the same flag on
        // runVulkanDesktop below for the case where it IS needed): empirically, forcing it
        // on THIS process makes Compose Desktop's first GraphicsEnvironment init hang
        // forever inside native CGLGraphicsConfig.getCGLConfigInfo on this JDK/Skiko
        // combination (confirmed via jstack -- reproducible, unrelated to screen lock,
        // which an earlier round wrongly blamed). The companion GLFW window
        // (DesktopVulkanCompanionWindow) runs in its own child process with its own
        // -XstartOnFirstThread, so this process doesn't need it for GLFW's sake either.
        val jvmArgsList = mutableListOf(
            "-Dawake.vulkan.nativeLibsDir=${desktopNativeLibDir.get().asFile.absolutePath}",
            "-Dawake.vulkan.dyldFallbackLibraryPath=$dyldFallbackLibraryPath"
        )
        if (moltenVkIcdPath != null) {
            jvmArgsList += "-Dawake.vulkan.icdFilenames=$moltenVkIcdPath"
        }
        jvmArgs(*jvmArgsList.toTypedArray())
    }
}

// Runs VulkanDesktopMain.kt directly (a real GLFW window + VulkanApplication, not the
// OpenGL/AWT "MainKt" demo above, and not launched as a companion process the way the
// Compose demo's "Enable Vulkan" switch does via DesktopVulkanCompanionWindow) -- same
// environment/JVM-arg requirements as awake-vulkan's verifyGlfwMain task (see that task's
// comments for why each one is needed): java.library.path for the desktop-native .dylib,
// VK_ICD_FILENAMES + DYLD_FALLBACK_LIBRARY_PATH for MoltenVK/the Vulkan loader,
// -XstartOnFirstThread for Cocoa's window-creation-on-main-thread requirement.
tasks.register<JavaExec>("runVulkanDesktop") {
    group = "application"
    description = "Run the real GLFW + Vulkan desktop demo (VulkanDesktopMain.kt) standalone."
    dependsOn("compileKotlinJvm")
    mainClass.set("VulkanDesktopMainKt")
    classpath = files(
        layout.buildDirectory.dir("classes/kotlin/jvm/main"),
        kotlin.jvm().compilations.getByName("main").runtimeDependencyFiles
    )
    if (moltenVkIcdPath != null) {
        environment("VK_ICD_FILENAMES", moltenVkIcdPath)
    }
    environment("DYLD_FALLBACK_LIBRARY_PATH", dyldFallbackLibraryPath)
    val jvmArgsList = mutableListOf("-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        jvmArgsList += "-XstartOnFirstThread"
    }
    jvmArgs(jvmArgsList)
}
