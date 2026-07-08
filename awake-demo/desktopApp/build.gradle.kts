import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {
    jvmToolchain(17)
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            // awake-core in intended for compose project, should be remove in the future
            implementation(project(":awake-core"))
            implementation(project(":awake-demo:shared"))
            // VulkanWindow (GLFW) for VulkanDesktopMain.kt's real Vulkan window/render loop.
            implementation(project(":awake-vulkan"))
        }
    }

}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KotlinMultiplatformComposeDesktopApplication"
            packageVersion = "1.0.0"
        }
        if (System.getProperty("os.name").lowercase().contains("mac")) {
            jvmArgs("-XstartOnFirstThread")
        }
    }
}

// Runs VulkanDesktopMain.kt (a real GLFW window + VulkanApplication, not the OpenGL/AWT
// "MainKt" demo above) -- same environment/JVM-arg requirements as awake-vulkan's
// verifyGlfwMain task (see that task's comments for why each one is needed):
// java.library.path for the desktop-native .dylib, VK_ICD_FILENAMES +
// DYLD_FALLBACK_LIBRARY_PATH for MoltenVK/the Vulkan loader, -XstartOnFirstThread for
// Cocoa's window-creation-on-main-thread requirement.
tasks.register<JavaExec>("runVulkanDesktop") {
    group = "application"
    description = "Run the real GLFW + Vulkan desktop demo (VulkanDesktopMain.kt)."
    dependsOn("compileKotlinJvm")
    mainClass.set("VulkanDesktopMainKt")
    classpath = files(
        layout.buildDirectory.dir("classes/kotlin/jvm/main"),
        kotlin.jvm().compilations.getByName("main").runtimeDependencyFiles
    )
    val desktopNativeLibDir = project(":awake-vulkan").layout.buildDirectory.dir("desktop-native-libs")
    val moltenVkIcdPath = fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
        .plus(fileTree("/usr/local/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") })
        .files.firstOrNull()?.absolutePath
    if (moltenVkIcdPath != null) {
        environment("VK_ICD_FILENAMES", moltenVkIcdPath)
    }
    environment("DYLD_FALLBACK_LIBRARY_PATH", "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib")
    val jvmArgsList = mutableListOf("-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        jvmArgsList += "-XstartOnFirstThread"
    }
    jvmArgs(jvmArgsList)
}