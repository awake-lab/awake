import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("awake.shader-pipeline-convention")
    id("awake.test-resources-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-ownership-convention")
}

// Shared shaders (skinned/instanced/shadow_depth/skinned_instanced/textured) live in
// awake:asset:shaders, not duplicated here -- see that module's own build.gradle.kts doc
// comment. A static directory reference, not a project dependency: that module produces no
// artifact this one consumes.
val sharedShaderDirectory =
    project(":awake:asset:shaders").layout.projectDirectory.dir("src/commonMain/resources/shaders")
tasks.named<SyncWgslShaderPipelineTask>("syncAwakeShaders") {
    additionalSourceDirectories.from(sharedShaderDirectory)
}
tasks.named<ValidateWgslShadersTask>("validateAwakeShaders") {
    additionalSourceDirectories.from(sharedShaderDirectory)
}

// verifyUiOwnership (awake.ui-ownership-convention) reads the whole src/ tree for its .kt
// source-pattern checks; syncAwakeShaders writes generated shader files under src/ too. Neither
// task actually depends on the other's output, but Gradle's parallel scheduler still needs an
// explicit order to avoid a same-directory read/write race.
tasks.named("verifyUiOwnership") {
    mustRunAfter("syncAwakeShaders")
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
            implementation(project(":awake:engine:bootstrap"))
            implementation(project(":awake:engine:render:contract"))
            implementation(project(":awake:core"))
            implementation(project(":awake:core:animation"))
            implementation(project(":awake:asset:gltf"))
            // TexturedUniformLayout/LitShadowUniformLayout -- code, not just the shared shader
            // text this module's syncAwakeShaders already references as a static file path.
            implementation(project(":awake:asset:shaders"))
            implementation(project(":awake:ecs"))
            implementation(project(":awake:scene"))
            implementation(project(":awake:scene:authoring"))
            implementation(project(":awake:ui:ui-core"))
            implementation(project(":awake:ui:designsystem"))
            // Outline glyphs directly, not through ShadcnIcons: that registry is pinned to the
            // 20px "mini" solid tier, and studio's chrome is outline at 24.
            implementation(project(":awake:ui:heroicons"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":awake:ui:testing"))
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

        named("desktopTest") {
            dependencies {
                implementation(project(":awake:backend:vulkan"))
            }
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

// The desktop test JVM needs the same native wiring the `run` task above sets up: these tests
// construct a real Vulkan device to render a frame and look at it, which is the only way to catch
// something the unit tests cannot see (a mirrored viewport, an unlit surface).
tasks.named<Test>("desktopTest") {
    jvmArgs("-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
    if (moltenVkIcdPath != null) {
        environment("VK_ICD_FILENAMES", moltenVkIcdPath)
    }
    environment("DYLD_FALLBACK_LIBRARY_PATH", dyldFallbackLibraryPath)
    // Each device session creates a Vulkan instance, and a second vkCreateInstance in the same
    // JVM fails -- same reason :awake:backend:vulkan forks per class.
    forkEvery = 1
}

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
