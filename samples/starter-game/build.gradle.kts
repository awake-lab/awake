import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.kotlin.serialization)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)
    applyDefaultHierarchyTemplate()

    android {
        namespace = "io.github.ronjunevaldoz.awake.sample.startergame"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }

    jvm("desktop")

    val moltenVkStaticDir = mapOf(
        "iosArm64" to project(":awake:backend:vulkan").file(
            "ios-native/MoltenVK/Package/Release/MoltenVK/static/MoltenVK.xcframework/ios-arm64"
        ),
        "iosSimulatorArm64" to project(":awake:backend:vulkan").file(
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
            baseName = "StarterGame"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosArm64"))
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "StarterGame"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosSimulatorArm64"))
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":awake:engine:game-dsl"))
            implementation(project(":awake:scene-dsl"))
            implementation(project(":awake:engine:ui-dsl"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        val appMain = create("appMain") {
            dependsOn(commonMain.get())
        }
        appMain.dependencies {
            implementation(project(":awake:engine"))
            implementation(project(":awake:backend:vulkan"))
        }

        named("desktopMain") {
            dependsOn(appMain)
            dependencies {
                implementation(project(":samples:server"))
            }
        }

        named("androidMain") {
            dependsOn(appMain)
            dependencies {
                api(project(":awake:engine"))
                api(project(":awake:backend:vulkan"))
            }
        }

        named("iosMain") {
            dependsOn(appMain)
        }

        named("wasmJsMain") {
            dependencies {
                implementation(project(":awake:engine"))
                implementation(project(":awake:backend:webgpu"))
            }
            resources.srcDir(project(":awake:backend:webgpu").file("src/wasmJsMain/resources"))
        }
    }
}

val desktopNativeLibDir = project(":awake:backend:vulkan").layout.buildDirectory.dir("desktop-native-libs")
val moltenVkIcdPath = fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
    .plus(fileTree("/usr/local/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") })
    .files.firstOrNull()?.absolutePath
val dyldFallbackLibraryPath = "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib"

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the Awake starter-game sample."
    dependsOn("compileKotlinDesktop", "desktopProcessResources")
    mainClass.set("io.github.ronjunevaldoz.awake.sample.startergame.app.MainKt")
    classpath = files(
        layout.buildDirectory.dir("classes/kotlin/desktop/main"),
        layout.buildDirectory.dir("processedResources/desktop/main"),
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
