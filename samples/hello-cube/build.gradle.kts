import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    // DebugSnapshot/DebugCommand (DebugControlServer.kt, desktop-only) -- see that file's
    // doc comment. Applied module-wide (cheap, same as awake:scene's own usage) rather than
    // scoped to just desktopMain, since Gradle KMP plugins apply per-module, not per-source-set.
    alias(libs.plugins.kotlin.serialization)
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
    // Same linker-opts-repetition pattern as MoltenVK above, for JoltC (see
    // awake/backend/jolt/build.gradle.kts's own comment for why): this module's own static
    // libs don't propagate through the project() dependency to this app's final framework
    // link step, so the flags need to be repeated here too.
    val joltCNativeBuildDir = mapOf(
        "iosArm64" to project(":awake:backend:jolt").layout.buildDirectory.dir("joltc-native/iosArm64").get().asFile,
        "iosSimulatorArm64" to project(":awake:backend:jolt").layout.buildDirectory.dir("joltc-native/iosSimulatorArm64").get().asFile,
    )
    fun joltCLinkerOpts(targetName: String): List<String> {
        val nativeBuildDir = joltCNativeBuildDir.getValue(targetName)
        return listOf(
            "-L${nativeBuildDir.path}", "-ljoltc",
            "-L${nativeBuildDir.resolve("JoltPhysics/Build").path}", "-lJolt",
            "-lc++",
        )
    }
    iosArm64 {
        binaries.framework {
            baseName = "Sample"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosArm64") + joltCLinkerOpts("iosArm64"))
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Sample"
            isStatic = true
            linkerOpts(moltenVkLinkerOpts("iosSimulatorArm64") + joltCLinkerOpts("iosSimulatorArm64"))
            xcf.add(this)
        }
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        // commonMain: helloCubeGame()/SampleMesh.kt -- the shared game builder consumed by
        // both the appMain (Vulkan) and wasmJsMain (WebGPU) entry points below.
        commonMain.dependencies {
            // AwakeGame contract + authored game DSL.
            implementation(project(":awake:engine:game-dsl"))
            // Reusable scene runtime plus authored scene/game DSL.
            implementation(project(":awake:scene-dsl"))
            // Shared coroutine primitives for tests and any future async sample helpers.
            implementation(libs.kotlinx.coroutines.core)
            // DebugSnapshot is @Serializable so the desktop debug-control server can
            // JSON-encode it.
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        // appMain: shared by desktop/Android/iOS -- these three drive VulkanGameApplication
        // (awake-backend-vulkan), which doesn't publish a wasmJs variant. Same pattern
        // awake-demo/shared/build.gradle.kts already uses, for the same reason.
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
                // Physics backend kept available for future scene variants on desktop.
                implementation(project(":awake:backend:jolt"))
                // DebugControlServer -- desktop-only Ktor WebSocket debug-control channel,
                // pulled out into its own reusable module (see docs/MVP_PLAN.md's decision
                // log). Generic over command/response types; this module supplies the actual
                // DebugCommand/DebugSnapshot parsing/encoding in Main.kt.
                implementation(project(":samples:server"))
            }
        }
        named("androidMain") {
            dependsOn(appMain)
            dependencies {
                // api, not implementation: sample-hello-cube:androidApp needs VulkanView
                // (awake-engine) and Application visible transitively -- see that module's
                // build.gradle.kts comment. Scoped to androidMain only (not appMain, which
                // iosMain also depends on) -- promoting this to api project-wide caused
                // awake-ecs's World (and its ambiguous ObjC-exported `family()` overloads)
                // to leak into the iOS XCFramework's generated header, which doesn't happen
                // when these stay `implementation` there, matching awake-demo/shared.
                api(project(":awake:engine"))
                api(project(":awake:backend:vulkan"))
                implementation(project(":awake:backend:jolt"))
            }
        }
        named("iosMain") {
            dependsOn(appMain)
            dependencies {
                // Physics backend kept available for future scene variants on iOS.
                implementation(project(":awake:backend:jolt"))
            }
        }

        named("wasmJsMain") {
            dependencies {
                implementation(project(":awake:engine"))
                implementation(project(":awake:backend:webgpu"))
                // Scene runtime + DSL for the shared helloCubeGame() builder.
                implementation(project(":awake:scene-dsl"))
                implementation(libs.wgpu4k)
                implementation(libs.wgpu4k.toolkit)
                implementation(libs.kotlinx.browser)
                // Physics backend kept available for future scene variants on wasm.
                implementation(project(":awake:backend:jolt"))
            }
            // wasmJs library resources aren't merged into a consuming app's own
            // processedResources/webpack-dev-server static output by default -- only a
            // module's OWN commonMain/wasmJsMain resources get served at their relative
            // path (see Resource.wasmJs.kt's doc comment for that assumption). This was
            // silently broken since D13 moved the UI/glyph/debug-line WGSL shaders into
            // awake-backend-webgpu's own resources -- flagged in D19/D20/D21 as a known gap,
            // fixed here by pointing this source set's resources directly at that module's
            // resource directory so Gradle's own wasmJsProcessResources task picks them up
            // and copies them into this app's served output, no manual copy task needed.
            resources.srcDir(project(":awake:backend:webgpu").file("src/wasmJsMain/resources"))
        }
    }
}

// Same MoltenVK ICD lookup awake-demo:desktopApp's runVulkanDesktop task uses -- see that
// module's build.gradle.kts for the full rationale.
val desktopNativeLibDir = project(":awake:backend:vulkan").layout.buildDirectory.dir("desktop-native-libs")
val moltenVkIcdPath = fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
    .plus(fileTree("/usr/local/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") })
    .files.firstOrNull()?.absolutePath
val dyldFallbackLibraryPath = "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib"

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the minimal hello-cube sample (a single static Vulkan cube, no texture)."
    dependsOn("compileKotlinDesktop", "desktopProcessResources")
    mainClass.set("io.github.ronjunevaldoz.awake.sample.hellocube.app.MainKt")
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
