import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.kotlin.serialization)
    id("awake.shader-pipeline-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-authored-units-convention")
    id("awake.ui-preview-report-convention")
    id("awake.ui-ownership-convention")
}

// verifyUiOwnership reads the whole src/ tree for its .kt source-pattern checks;
// syncAwakeShaders writes generated shader files under src/ too. Neither task actually depends
// on the other's output, but Gradle's parallel scheduler still needs an explicit order to avoid
// a same-directory read/write race.
tasks.named("verifyUiOwnership") {
    mustRunAfter("syncAwakeShaders")
}

kotlin {
    jvmToolchain(17)
    applyDefaultHierarchyTemplate()

    android {
        namespace = "io.github.ronjunevaldoz.awake.sample.uishowcase"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
        withHostTest {}
    }

    jvm("desktop")

    val xcf = XCFramework("UiShowcase")
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
            baseName = "UiShowcase"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosArm64"))
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "UiShowcase"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosSimulatorArm64"))
            xcf.add(this)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            // Fixed dev-server ports so dev/prod each get their own, and don't collide with
            // the other samples' wasmJs dev servers (webpack defaults every sample to 8080
            // otherwise). Keep in sync with the "wasmjs-ui-showcase"/"wasmjs-ui-showcase-prod"
            // entries in .claude/launch.json and the port table in
            // docs/reference/developer-docs.md.
            commonWebpackConfig {
                val port =
                    if (mode == org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.PRODUCTION) 8083 else 8082
                devServer = devServer?.copy(port = port)
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":awake:engine:bootstrap"))
            implementation(project(":awake:scene:authoring"))
            implementation(project(":awake:ui:designsystem"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":awake:ui:testing"))
            implementation(project(":awake:ui:ui-core"))
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

        named("androidMain") {
            dependsOn(appMain)
            dependencies {
                api(project(":awake:core"))
                api(project(":awake:backend:vulkan"))
            }
        }

        named("iosMain") {
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

tasks.named<UiPreviewReportTask>("uiPreviewReport") {
    reportTitle.set("Awake UI Showcase Previews")
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
    description = "Run the Awake UI showcase sample."
    dependsOn("desktopMainClasses")
    mainClass.set("io.github.ronjunevaldoz.awake.sample.uishowcase.app.MainKt")
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

tasks.withType<Test>().configureEach {
    val record =
        System.getenv("AWAKE_RECORD_SNAPSHOTS") ?: System.getProperty("AWAKE_RECORD_SNAPSHOTS")
    if (record != null) {
        systemProperty("AWAKE_RECORD_SNAPSHOTS", record)
    }
}

tasks.register("validateUiShowcasePlatforms") {
    group = "verification"
    description = "Build and test the UI showcase sample across desktop, iOS simulator, and web."
    dependsOn(
        "desktopTest",
        "desktopJar",
        "iosSimulatorArm64Test",
        "wasmJsBrowserDistribution"
    )
}
