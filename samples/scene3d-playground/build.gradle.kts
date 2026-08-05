import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.gradle.api.tasks.JavaExec

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    id("awake.shader-pipeline-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-authored-units-convention")
    id("awake.ui-preview-report-convention")
}

kotlin {
    jvmToolchain(17)
    applyDefaultHierarchyTemplate()

    android {
        namespace = "io.github.ronjunevaldoz.awake.sample.scene3d"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
        withHostTest {}
    }

    jvm("desktop")

    val xcf = XCFramework("Scene3DPlayground")
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
            baseName = "Scene3DPlayground"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosArm64"))
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Scene3DPlayground"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosSimulatorArm64"))
            xcf.add(this)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            // Fixed dev-server ports, freed up by hello-cube/starter-game's retirement -- keep
            // in sync with .claude/launch.json and docs/reference/developer-docs.md's port table.
            commonWebpackConfig {
                val port = if (mode == org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.PRODUCTION) 8085 else 8081
                devServer = devServer?.copy(port = port)
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":awake:engine:game-dsl"))
            implementation(project(":awake:engine:render-api"))
            implementation(project(":awake:base"))
            implementation(project(":awake:ecs"))
            implementation(project(":awake:scene"))
            implementation(project(":awake:scene-dsl"))
            implementation(project(":awake:engine:ui:ui-core"))
            implementation(project(":awake:engine:ui:ui-designsystem"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":awake:engine:ui:ui-testing"))
            implementation(libs.kotlinx.coroutines.test)
        }

        val appMain = create("appMain") {
            dependsOn(commonMain.get())
        }
        appMain.dependencies {
            implementation(project(":awake:base"))
            implementation(project(":awake:backend:vulkan"))
        }

        named("desktopMain") {
            dependsOn(appMain)
            dependencies {
                implementation(libs.recast4j.recast)
                implementation(libs.recast4j.detour)
            }
        }

        named("androidMain") {
            dependsOn(appMain)
            dependencies {
                api(project(":awake:base"))
                api(project(":awake:backend:vulkan"))
                implementation(libs.recast4j.recast)
                implementation(libs.recast4j.detour)
            }
        }

        named("iosMain") {
            dependsOn(appMain)
        }

        // Pixel-baseline regression test for RotatingCubeDemo (see
        // RotatingCubePixelBaselineTest) needs a real headless Vulkan Renderer -- only
        // appMain/desktopMain/androidMain/iosMain pull in awake:backend:vulkan today (a real
        // GPU backend has no business on commonTest's classpath, which every target -- wasmJs
        // included -- compiles against), so desktopTest gets its own explicit dependency
        // instead of going through appMain. comparePixels (awake:engine:ui:ui-testing) is
        // already a commonTest dependency above, inherited here. appMain's own compiled
        // triangle.vert/frag.spv (PositionNormalColor, this sample's real shader -- not
        // awake-backend-vulkan's own generic ui shaders) is mirrored into
        // src/desktopTest/resources rather than added as a source-set dependency here, since
        // appMain also carries GltfViewerDemo's model assets this test has no use for.
        named("desktopTest") {
            dependencies {
                implementation(project(":awake:backend:vulkan"))
            }
        }

        named("wasmJsMain") {
            dependencies {
                implementation(project(":awake:base"))
                implementation(project(":awake:backend:webgpu"))
                implementation(libs.kotlinx.browser)
            }
            resources.srcDir(project(":awake:backend:webgpu").file("src/wasmJsMain/resources"))
        }
    }
}

tasks.named<UiPreviewReportTask>("uiPreviewReport") {
    reportTitle.set("Scene3D Playground Previews")
}

val desktopNativeLibDir = project(":awake:backend:vulkan").layout.buildDirectory.dir("desktop-native-libs")
val moltenVkIcdPath = fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
    .plus(fileTree("/usr/local/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") })
    .files.firstOrNull()?.absolutePath
val dyldFallbackLibraryPath = "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib"

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the Awake 3D playground sample."
    dependsOn("desktopMainClasses")
    mainClass.set("io.github.ronjunevaldoz.awake.sample.scene3d.app.MainKt")
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

// Same wiring awake-backend-vulkan's own desktopTest task uses for its headless pixel-baseline
// test (see that module's build.gradle.kts) -- RotatingCubePixelBaselineTest needs the same
// native Vulkan loader / MoltenVK ICD env to construct a real (headless) GraphicsDevice. A
// no-op if buildDesktopNative hasn't been run (System.loadLibrary just fails with its usual
// UnsatisfiedLinkError, same as if this weren't set at all).
tasks.named<Test>("desktopTest") {
    jvmArgs("-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
    if (moltenVkIcdPath != null) {
        environment("VK_ICD_FILENAMES", moltenVkIcdPath)
    }
    environment("DYLD_FALLBACK_LIBRARY_PATH", dyldFallbackLibraryPath)
    // A second headless GraphicsDevice.createHeadless() call in the same JVM process throws
    // VK_ERROR_EXTENSION_NOT_PRESENT (cross-instance native/global Vulkan loader state colliding
    // -- see RotatingCubePixelBaselineTest's own doc comment, which sidesteps this WITHIN one
    // test class by sharing a single device across cases). RotatingCubeContinuousSpinStabilityTest
    // is a separate class with its own headless device, so the same collision resurfaces ACROSS
    // classes once both run in this task's single default JVM -- forkEvery=1 gives every test
    // class a fresh JVM instead of chasing a fix in GraphicsDevice's own teardown.
    forkEvery = 1
}
