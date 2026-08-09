import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("awake.shader-pipeline-convention")
    id("awake.test-resources-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    jvmToolchain(17)
    applyDefaultHierarchyTemplate()

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                val port =
                    if (mode == org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.PRODUCTION) 8087 else 8086
                devServer = devServer?.copy(port = port)
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":awake:engine:game-authoring"))
            implementation(project(":awake:engine:render:contract"))
            implementation(project(":awake:core"))
            implementation(project(":awake:ecs"))
            implementation(project(":awake:scene"))
            implementation(project(":awake:scene:authoring"))
            implementation(project(":awake:engine:ui:ui-core"))
            implementation(project(":awake:engine:ui:designsystem"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        val appMain = create("appMain") {
            dependsOn(commonMain.get())
        }
        appMain.dependencies {
            implementation(project(":awake:core"))
            implementation(project(":awake:backend:vulkan"))
        }

        named("desktopMain") {
            dependsOn(appMain)
        }

        named("wasmJsMain") {
            dependencies {
                implementation(project(":awake:core"))
                implementation(project(":awake:backend:webgpu"))
                implementation(libs.kotlinx.browser)
            }
            resources.srcDir(project(":awake:backend:webgpu").file("src/wasmJsMain/resources"))
        }


    }
}

val desktopNativeLibDir =
    project(":awake:backend:vulkan:bindings").layout.buildDirectory.dir("desktop-native-libs")
val moltenVkIcdPath =
    fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
        .plus(fileTree("/usr/local/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") })
        .files.firstOrNull()?.absolutePath
val dyldFallbackLibraryPath = "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib"

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the Awake studio sample."
    dependsOn("desktopMainClasses")
    mainClass.set("io.github.ronjunevaldoz.awake.studio.app.MainKt")
    classpath = files(
        layout.buildDirectory.dir("classes/kotlin/desktop/main"),
        layout.buildDirectory.dir("processedResources/desktop/main"),
        kotlin.jvm("desktop").compilations.getByName("main").runtimeDependencyFiles
    )
    if (moltenVkIcdPath != null) {
        environment("VK_ICD_FILENAMES", moltenVkIcdPath)
    }
    environment("DYLD_FALLBACK_LIBRARY_PATH", dyldFallbackLibraryPath)
    val jvmArgsList =
        mutableListOf("-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        jvmArgsList += "-XstartOnFirstThread"
    }
    jvmArgs(jvmArgsList)
}

// appMain holds compiled Vulkan SPIR-V; commonMain holds hand-authored assets (models, scene
// documents) every target loads. Both need serving to iosSimulatorArm64Test/wasmJsBrowserTest
// the same way the real app gets them -- Kotlin's own resource merging reaches the compiled
// app bundle, but not karma's test server or a Kotlin/Native test binary's working directory.
awakeTestResources {
    roots.from(layout.projectDirectory.dir("src/appMain/resources"))
    roots.from(layout.projectDirectory.dir("src/commonMain/resources"))
}

