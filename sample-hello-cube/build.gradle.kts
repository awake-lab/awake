plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)
    jvm()

    sourceSets {
        jvmMain.dependencies {
            // Application/DesktopGameLoop (app-lifecycle glue).
            implementation(project(":awake-engine"))
            // VulkanGameApplication (the reusable bootstrap this sample demonstrates) +
            // VulkanWindow (GLFW) for the bare desktop window/render loop -- same pattern
            // awake-demo:desktopApp's VulkanDesktopMain.kt already uses.
            implementation(project(":awake-backend-vulkan"))
            // Provides Dispatchers.Main -- VulkanGameApplication.create() uses
            // MainScope().launch internally (see libs.versions.toml's own comment on this
            // entry for the full "Dispatchers.Main is missing" story).
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

// Same MoltenVK ICD lookup awake-demo:desktopApp's runVulkanDesktop task uses -- see that
// module's build.gradle.kts for the full rationale.
val desktopNativeLibDir = project(":awake-backend-vulkan").layout.buildDirectory.dir("desktop-native-libs")
val moltenVkIcdPath = fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
    .plus(fileTree("/usr/local/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") })
    .files.firstOrNull()?.absolutePath
val dyldFallbackLibraryPath = "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib"

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the minimal hello-cube sample (a single static Vulkan cube, no texture)."
    dependsOn("compileKotlinJvm")
    mainClass.set("MainKt")
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
