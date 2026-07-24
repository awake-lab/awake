import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.gradle.api.tasks.JavaExec
import java.util.Base64

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    alias(libs.plugins.kotlin.serialization)
    id("awake.shader-pipeline-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-authored-units-convention")
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
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":awake:engine:game-dsl"))
            implementation(project(":awake:scene-dsl"))
            implementation(project(":awake:engine:ui-core"))
            implementation(project(":awake:engine:ui-designsystem"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":awake:engine:testing"))
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
        }

        named("androidMain") {
            dependsOn(appMain)
            dependencies {
                api(project(":awake:base"))
                api(project(":awake:backend:vulkan"))
            }
        }

        named("iosMain") {
            dependsOn(appMain)
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

tasks.named<Test>("desktopTest") {
    doFirst {
        delete(layout.buildDirectory.dir("ui-previews"))
        delete(layout.buildDirectory.dir("reports/ui-previews"))
    }
    systemProperty("AWAKE_SNAPSHOT_ROOT", project.rootDir.absolutePath)
    finalizedBy("uiShowcasePreviewReport")
}

val desktopNativeLibDir = project(":awake:backend:vulkan").layout.buildDirectory.dir("desktop-native-libs")
val moltenVkIcdPath = fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
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
    val jvmArgsList = mutableListOf("-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        jvmArgsList += "-XstartOnFirstThread"
    }
    jvmArgs(jvmArgsList)
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

tasks.register("uiShowcasePreviewReport") {
    group = "documentation"
    description = "Generate an HTML gallery for the Awake UI showcase previews."
    val previewsDir = layout.buildDirectory.dir("ui-previews")
    val manifestFile = layout.buildDirectory.file("ui-previews/previews.tsv")
    val reportFile = layout.buildDirectory.file("reports/ui-previews/index.html")
    doLast {
        val root = previewsDir.get().asFile
        val manifest = manifestFile.get().asFile
        val previews = if (manifest.exists()) {
            manifest.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val columns = line.split('\t')
                    if (columns.size < 7) null else columns
                }
                .sortedWith(compareBy({ it[2] }, { it[0] }))
        } else {
            emptyList()
        }

        fun escapeHtml(value: String): String = buildString(value.length) {
            value.forEach { char ->
                append(
                    when (char) {
                        '&' -> "&amp;"
                        '<' -> "&lt;"
                        '>' -> "&gt;"
                        '"' -> "&quot;"
                        else -> char
                    }
                )
            }
        }

        val cards = previews.joinToString("\n") { columns ->
            val id = columns[0]
            val title = columns[1]
            val group = columns[2].ifBlank { "General" }
            val summary = columns[3]
            val width = columns[4]
            val height = columns[5]
            val reportScale = columns[6].toIntOrNull()?.coerceAtLeast(1) ?: 1
            val png = root.resolve("$id.png")
            val image = if (png.exists()) {
                val base64 = Base64.getEncoder().encodeToString(png.readBytes())
                """<img src="data:image/png;base64,$base64" alt="${escapeHtml(title)}" width="${escapeHtml(width)}" height="${escapeHtml(height)}" style="display:block;border:1px solid #2f2f2f;border-radius:12px;max-width:100%;width:min(100%, ${escapeHtml(width)}px);height:auto" />"""
            } else {
                """<p style="color:#f88">Missing preview image: ${escapeHtml(id)}.png</p>"""
            }
            """
            <article style="display:grid;gap:1rem;margin:0 0 1.5rem 0;padding:1.25rem;border:1px solid #262626;border-radius:16px;background:#09090b">
                <div>
                    <p style="margin:0 0 0.35rem 0;color:#a1a1aa;font-size:0.85rem;text-transform:uppercase;letter-spacing:0.08em">${escapeHtml(group)}</p>
                    <h2 style="margin:0 0 0.5rem 0">${escapeHtml(title)}</h2>
                    <p style="margin:0 0 0.5rem 0;color:#d4d4d8">${escapeHtml(summary)}</p>
                    <p style="margin:0;color:#71717a;font-size:0.9rem">${escapeHtml(width)}x${escapeHtml(height)}${if (reportScale > 1) " @ ${reportScale}x export" else ""}</p>
                </div>
                $image
            </article>
            """.trimIndent()
        }

        val body = if (cards.isBlank()) {
            """
            <p>No previews recorded yet.</p>
            <p>Run <code>./gradlew :samples:ui-showcase:desktopTest</code> to regenerate them.</p>
            """.trimIndent()
        } else {
            """
            <p style="color:#d4d4d8">Curated Awake preview entries for the shadcn-style showcase. Each card is generated from executable desktop tests, so docs and rendering stay in sync.</p>
            $cards
            """.trimIndent()
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Awake UI Showcase Previews</title>
            </head>
            <body style="font-family:Inter,system-ui,sans-serif;background:#020617;color:#fafafa;padding:2rem;max-width:1080px;margin:0 auto">
                <h1 style="margin-top:0">Awake UI Showcase Previews</h1>
                $body
            </body>
            </html>
        """.trimIndent()

        val out = reportFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(html)
        println("UI showcase preview report: file://${out.absolutePath}")
    }
}
