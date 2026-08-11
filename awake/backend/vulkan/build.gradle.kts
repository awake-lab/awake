/*
 * Awake
 * Awake.awake-backend-vulkan
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.security.MessageDigest
import java.util.Base64

plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.vulkan"
    }

    // iosX64 (Intel simulator) dropped: Compose Multiplatform stopped publishing it
    // after 1.11.0-alpha01 (Apple Silicon only going forward)

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            // Renderer/DrawCall/TextureLoader (moved in from awake-core) need Mat4/Camera
            // and Bitmap/readResourceBytes -- see docs/MVP_PLAN.md's Decision Log, D11, for
            // the awake-core split this module boundary comes from.
            implementation(project(":awake:core"))
            // The backend consumes raw UI draw primitives and mesh utilities to submit the
            // frame to Vulkan. Keep this direct rather than relying on render-contract's
            // transitive ui-core dependency; authored value contracts come from ui-api.
            implementation(project(":awake:engine:ui:ui-core"))
            implementation(project(":awake:engine:ui:ui-api"))
            // Module restructuring slice 1 (see docs/MVP_PLAN.md): Mesh/Material/Renderer's
            // expect declarations now implement the narrow backend-neutral interfaces this
            // module owns, so RenderSystem (awake-scene) can depend on just that module
            // instead of all of awake-backend-vulkan's concrete Vulkan bindings. `api`, not
            // `implementation`, since consumers reaching these types through awake-backend-vulkan
            // (e.g. VulkanApplication.kt) need them visible too.
            api(project(":awake:engine:render:contract"))
            // Raw generated Vulkan API (see docs/tasks/2026-08-09-application-seam-and-module-
            // naming-plan.md, Part 3) -- gen/handles/models/enums/Vulkan.kt/VulkanSurface.kt.
            // `api`, not `implementation`: Renderer/GraphicsDevice/etc.'s own public signatures
            // (e.g. RenderPipeline constructor params) surface these raw types to consumers.
            api(project(":awake:backend:vulkan:bindings"))
            // Reusable-Application gap fix (see docs/MVP_PLAN.md's Decision Log):
            // VulkanGameApplication implements the Application interface (awake-engine) and
            // owns generic scene loading/TransformSystem/RenderSystem wiring (awake-scene) so
            // a new game doesn't have to hand-roll the same ~200 lines of GraphicsDevice/
            // SwapchainManager/RenderPipeline/Mesh/Material bootstrap awake-demo used to.
            implementation(libs.kotlinx.coroutines.core)
            // VulkanGameApplication now extends GameApplication (see
            // docs/MVP_PLAN.md's decision log for the duplication this replaces). `api`,
            // not `implementation`: it's a supertype of VulkanGameApplication, so consumers
            // (sample-hello-cube, awake-demo) need it resolvable on their own classpath too.
            api(project(":awake:engine:game"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            // Headless-renderer pixel-baseline regression test (desktopTest only, see
            // RendererHeadlessPixelBaselineTest) needs comparePixels().
            implementation(project(":awake:engine:ui:testing"))
            // Real-widget real-render investigations (see UiAnimationFrameCapture /
            // ShadcnCollapsibleRealRenderCollapseFrameCaptureTest, desktopTest only) need
            // actual shadcn widgets (shadcnSidebar/shadcnCollapsible), not just raw
            // UiDrawPrimitives -- test-only, no cycle (ui-designsystem doesn't depend on this
            // module).
            implementation(project(":awake:engine:ui:designsystem"))
        }
        androidMain.dependencies {
            implementation(libs.leakcanary.android)
        }
    }
}

// On macOS, the Vulkan loader needs VK_ICD_FILENAMES to find MoltenVK's ICD manifest at run
// time -- same rationale as :awake:backend:vulkan:bindings' own desktopTest env (this module's
// desktopTest exercises the real renderer, so it needs the same environment).
val moltenVkIcdPath =
    fileTree("/opt/homebrew/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") }
        .plus(fileTree("/usr/local/Cellar/molten-vk") { include("*/etc/vulkan/icd.d/MoltenVK_icd.json") })
        .files.firstOrNull()?.absolutePath
val desktopVulkanEnv = buildMap {
    if (moltenVkIcdPath != null) put("VK_ICD_FILENAMES", moltenVkIcdPath)
    put(
        "DYLD_FALLBACK_LIBRARY_PATH",
        "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib"
    )
}
val desktopNativeLibDir =
    project(":awake:backend:vulkan:bindings").layout.buildDirectory.dir("desktop-native-libs")

// Always points java.library.path at bindings' desktop-native-libs for desktop tests -- a
// no-op if :awake:backend:vulkan:bindings:buildDesktopNative hasn't been run (System.loadLibrary
// just fails with its usual UnsatisfiedLinkError in that case, same as if this weren't set).
tasks.named<Test>("desktopTest") {
    jvmArgs("-Djava.library.path=${desktopNativeLibDir.get().asFile.absolutePath}")
    environment(desktopVulkanEnv)
    // `-DAWAKE_RECORD_SNAPSHOTS=true` on the Gradle CLI only sets the property on Gradle's own
    // JVM -- desktopTest runs in a forked test JVM, so forward it explicitly. Same fix
    // awake.ui-preview-report-convention applies for the ui-preview modules.
    System.getProperty("AWAKE_RECORD_SNAPSHOTS")
        ?.let { systemProperty("AWAKE_RECORD_SNAPSHOTS", it) }
    // Headless Vulkan tests exercise native loader / instance lifecycle paths that have
    // proven sensitive to process-shared state when multiple renderer baseline classes run
    // in one worker. Fork per test class keeps those baselines isolated and reproducible.
    forkEvery = 1
    finalizedBy("pixelBaselineReport")
}

// Gradle's own HTML test report only shows escaped stdout/stacktrace text -- no <img>
// rendering, no attachment support -- so RendererHeadlessPixelBaselineTest's actual.png/
// baseline.png dumps (see that test's failure branch) are otherwise just files a developer
// has to know to go dig up. This task turns build/test-failures/*/{actual,baseline}.png
// into one self-contained HTML page (images inlined as base64, so it's a single file you
// can open or send without broken relative links) -- a lightweight stand-in for a real
// test-reporting tool (Allure, etc) until there are enough visual tests to justify one.
tasks.register("pixelBaselineReport") {
    group = "verification"
    description =
        "Generate an HTML gallery of actual-vs-baseline PNGs for any failed pixel-baseline test."
    val failuresDir = layout.buildDirectory.dir("test-failures")
    val reportFile = layout.buildDirectory.file("reports/pixel-baseline/index.html")
    // No inputs.dir/outputs.file declared -- this always reruns (a plain dev convenience
    // task, not something that needs Gradle's up-to-date caching), and failuresDir may
    // legitimately not exist yet (no failures ever recorded).
    doLast {
        val root = failuresDir.get().asFile
        val testDirs =
            root.listFiles { file -> file.isDirectory }?.sortedBy { it.name } ?: emptyList()

        fun imgTag(file: File): String {
            if (!file.exists()) return "<p><em>missing: ${file.name}</em></p>"
            val base64 = Base64.getEncoder().encodeToString(file.readBytes())
            return """<img src="data:image/png;base64,$base64" style="image-rendering:pixelated;width:256px;height:256px;border:1px solid #444" />"""
        }

        val sections = testDirs.joinToString("\n") { testDir ->
            """
            <section style="margin-bottom:2rem">
                <h2>${testDir.name}</h2>
                <div style="display:flex;gap:1rem">
                    <div><h3>Baseline (expected)</h3>${imgTag(File(testDir, "baseline.png"))}</div>
                    <div><h3>Actual (rendered)</h3>${imgTag(File(testDir, "actual.png"))}</div>
                </div>
            </section>
            """.trimIndent()
        }

        val body = if (testDirs.isEmpty()) {
            "<p>No pixel-baseline test failures in the last run.</p>"
        } else {
            sections
        }

        val html = """
            <!DOCTYPE html>
            <html><head><meta charset="utf-8"><title>Pixel baseline report</title></head>
            <body style="font-family:sans-serif;background:#1e1e1e;color:#eee;padding:2rem">
                <h1>Pixel baseline report</h1>
                $body
            </body></html>
        """.trimIndent()

        val out = reportFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(html)
        println("Pixel baseline report: file://${out.absolutePath}")
    }
}

// Vulkan loads the checked-in .spv binaries at runtime, NOT the .frag/.vert sources next to
// them -- so editing GLSL without recompiling silently ships the old shader. That really
// happened: 7 of 10 binaries had drifted, hiding both an unimplemented MSDF branch and a
// descriptor-stage mismatch until the SPIR-V was regenerated.
//
// Gate on a hash of each GLSL source recorded when its .spv was last built, rather than
// recompiling and byte-comparing: glslangValidator is unpinned (the glsl-validator convention
// fetches main-tot), so different compiler versions legitimately emit different bytes and a
// byte gate would fail for the wrong reason. A source hash needs no compiler and no network.
val shaderSourceDir = layout.projectDirectory.dir("src/commonMain/resources/assets/shader/vulkan")
val shaderManifest = shaderSourceDir.file("shader-sources.sha256")

fun hashShaderSources(): String =
    shaderSourceDir.asFile.listFiles()
        .orEmpty()
        .filter { it.extension == "frag" || it.extension == "vert" }
        .sortedBy { it.name }
        .joinToString("\n") { file ->
            val digest = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
            "${file.name}  " + digest.joinToString("") { byte -> "%02x".format(byte) }
        }

tasks.register("updateShaderManifest") {
    group = "shader"
    description = "Record the current GLSL source hashes. Run after recompiling .spv binaries."
    doLast {
        shaderManifest.asFile.writeText(hashShaderSources() + "\n")
        logger.lifecycle("Wrote ${shaderManifest.asFile.name}")
    }
}

val verifyShaderBinaries = tasks.register("verifyShaderBinaries") {
    group = "verification"
    description = "Fail if a .frag/.vert changed without its .spv being regenerated."
    doLast {
        val manifestFile = shaderManifest.asFile
        require(manifestFile.exists()) {
            "Missing ${manifestFile.name}. Run :awake:backend:vulkan:updateShaderManifest."
        }
        val expected = manifestFile.readText().trim()
        val actual = hashShaderSources().trim()
        if (expected != actual) {
            val changed =
                actual.lines().filter { it !in expected.lines() }.map { it.substringBefore("  ") }
            error(
                "Vulkan GLSL changed but the checked-in .spv was not regenerated: " +
                        "${changed.joinToString()}. Recompile with " +
                        "`glslangValidator -V <file> -o <file>.spv`, then run " +
                        ":awake:backend:vulkan:updateShaderManifest."
            )
        }
    }
}

tasks.named("check") { dependsOn(verifyShaderBinaries) }

// Also gate RESOURCE PACKAGING, not just `check`. `check` alone only fires for someone who runs
// this module's own verification; it is silent for the far more common path of building or
// running a sample, which is exactly how a stale ui_glyph.frag.spv shipped a gamma fix that
// never reached the GPU. Hanging the gate off processResources means any build that packages
// the .spv has to prove the .spv matches its GLSL first.
tasks.matching { it.name.endsWith("rocessResources") }.configureEach {
    dependsOn(verifyShaderBinaries)
}
