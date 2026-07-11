import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)

    // See awake-demo/shared/build.gradle.kts's matching comment: adding the appMain
    // dependsOn() edge below silently disables the default hierarchy template project-wide
    // otherwise.
    applyDefaultHierarchyTemplate()

    android {
        namespace = "io.github.ronjunevaldoz.awake.sample.hellocube"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    jvm("desktop")

    // Same XCFramework/SPM distribution + MoltenVK linker-opts-repetition pattern as
    // awake-demo/shared/build.gradle.kts -- see that file's comments for the full
    // rationale (linkerOpts don't propagate through project() dependencies to a
    // downstream consumer's own framework link step).
    val xcf = XCFramework("Sample")
    val moltenVkStaticDir = mapOf(
        "iosArm64" to project(":awake-backend-vulkan").file(
            "ios-native/MoltenVK/Package/Release/MoltenVK/static/MoltenVK.xcframework/ios-arm64"
        ),
        "iosSimulatorArm64" to project(":awake-backend-vulkan").file(
            "ios-native/MoltenVK/Package/Release/MoltenVK/static/MoltenVK.xcframework/" +
                "ios-arm64_x86_64-simulator"
        ),
    )
    fun moltenVkLinkerOpts(targetName: String) = listOf(
        "-L${moltenVkStaticDir.getValue(targetName).path}", "-lMoltenVK", "-lc++",
        "-framework", "Metal",
        "-framework", "QuartzCore",
        "-framework", "IOSurface",
        "-framework", "CoreGraphics",
        "-framework", "Foundation",
        "-framework", "UIKit",
    )
    iosArm64 {
        binaries.framework {
            baseName = "Sample"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosArm64"))
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Sample"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosSimulatorArm64"))
            xcf.add(this)
        }
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        // appMain: shared by desktop/Android/iOS -- these three drive VulkanGameApplication
        // (awake-backend-vulkan), which doesn't publish a wasmJs variant. Same pattern
        // awake-demo/shared/build.gradle.kts already uses, for the same reason.
        val appMain = create("appMain") {
            dependsOn(commonMain.get())
        }
        appMain.dependencies {
            // api, not implementation: sample-hello-cube:androidApp needs VulkanView
            // (awake-engine) and Application (also awake-engine, via VulkanGameApplication's
            // supertype) visible transitively -- see that module's build.gradle.kts comment.
            api(project(":awake-engine"))
            api(project(":awake-backend-vulkan"))
        }
        named("desktopMain") {
            dependsOn(appMain)
            dependencies {
                // Provides Dispatchers.Main -- VulkanGameApplication.create() uses
                // MainScope().launch internally.
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
        named("androidMain") { dependsOn(appMain) }
        named("iosMain") { dependsOn(appMain) }

        named("wasmJsMain") {
            dependencies {
                implementation(project(":awake-engine"))
                implementation(project(":awake-backend-webgpu"))
                implementation(libs.wgpu4k)
                implementation(libs.wgpu4k.toolkit)
                implementation(libs.kotlinx.browser)
            }
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
    dependsOn("compileKotlinDesktop")
    mainClass.set("MainKt")
    classpath = files(
        layout.buildDirectory.dir("classes/kotlin/desktop/main"),
        kotlin.jvm("desktop").compilations.getByName("main").runtimeDependencyFiles
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
