@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import java.io.File
import java.util.Base64

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library.kmp)
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-authored-units-convention")
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "io.github.ronjunevaldoz.awake.engine.application.dsl"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "awake-engine-game-dsl"
        }
    }

    jvm("desktop")

    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":awake:engine:game"))
        }
        commonTest.dependencies {
            implementation(project(":awake:base"))
            implementation(project(":awake:engine:render-api"))
            implementation(project(":awake:engine:ui:ui-core"))
            implementation(project(":awake:engine:ui:ui-unstyled"))
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        named("desktopTest") {
            dependencies {
                implementation(project(":awake:base"))
                implementation(project(":awake:engine:render-api"))
                implementation(project(":awake:engine:ui:ui-core"))
                implementation(project(":awake:engine:ui:ui-unstyled"))
                implementation(project(":awake:engine:ui:ui-testing"))
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

tasks.named<Test>("desktopTest") {
    doFirst {
        delete(layout.buildDirectory.dir("game-dsl-tutorials"))
        delete(layout.buildDirectory.dir("reports/game-dsl-tutorials"))
    }
    finalizedBy("gameDslTutorialDocsReport")
}

tasks.register("gameDslTutorialDocsReport") {
    group = "documentation"
    description = "Generate an HTML tutorial guide for the game DSL proof examples."
    val manifestFile = layout.buildDirectory.file("game-dsl-tutorials/tutorials.tsv")
    val reportFile = layout.buildDirectory.file("reports/game-dsl-tutorials/index.html")
    doLast {
        val manifest = manifestFile.get().asFile
        val tutorials = if (manifest.exists()) {
            manifest.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val columns = line.split('\t')
                    if (columns.size < 4) null else columns
                }
                .sortedBy { it[0] }
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

        val cards = tutorials.joinToString("\n") { columns ->
            val title = columns[1]
            val summary = columns[2]
            val snippet = columns[3].replace('\u000B', '\n')
            """
            <article style="margin:0 0 2rem 0;padding:1rem;border:1px solid #333;border-radius:8px;background:#252525">
                <h2 style="margin-top:0">${escapeHtml(title)}</h2>
                <p style="color:#cfcfcf">${escapeHtml(summary)}</p>
                <pre style="overflow:auto;padding:1rem;background:#171717;border-radius:6px;border:1px solid #2f2f2f"><code>${escapeHtml(snippet)}</code></pre>
            </article>
            """.trimIndent()
        }

        val body = if (cards.isBlank()) {
            """
            <p>No game DSL tutorials recorded.</p>
            <p>Run <code>./gradlew :awake:engine:game-dsl:desktopTest</code> to regenerate them.</p>
            """.trimIndent()
        } else {
            """
            <p style="color:#cfcfcf">Curated game DSL examples generated from executable desktop tests. The snippets below are backed by assertions over real GameSpec and AwakeGame composition.</p>
            $cards
            """.trimIndent()
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Game DSL tutorial docs</title>
            </head>
            <body style="font-family:sans-serif;background:#1e1e1e;color:#eee;padding:2rem;max-width:1100px;margin:0 auto">
                <h1>Game DSL tutorial docs</h1>
                $body
            </body>
            </html>
        """.trimIndent()

        val out = reportFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(html)
        println("Game DSL tutorial docs: file://${out.absolutePath}")
    }
}

// Moved from the deleted awake:engine:ui-dsl module (2026-07-24) -- that module had no
// production code left, only these tests. Kept as a separate report/snapshot dir since it's a
// different format (PNG snapshots) from gameDslTutorialDocsReport above (text code snippets).
tasks.named<Test>("desktopTest") {
    doFirst {
        delete(layout.buildDirectory.dir("ui-dsl-snapshots"))
        delete(layout.buildDirectory.dir("reports/ui-dsl-tutorials"))
    }
    finalizedBy("uiDslTutorialDocsReport")
}

tasks.register("uiDslTutorialDocsReport") {
    group = "documentation"
    description = "Generate an HTML tutorial guide for the UI facade DSL snapshots."
    val snapshotsDir = layout.buildDirectory.dir("ui-dsl-snapshots")
    val manifestFile = layout.buildDirectory.file("ui-dsl-snapshots/tutorials.tsv")
    val reportFile = layout.buildDirectory.file("reports/ui-dsl-tutorials/index.html")
    doLast {
        val root = snapshotsDir.get().asFile
        val manifest = manifestFile.get().asFile
        val tutorials = if (manifest.exists()) {
            manifest.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val columns = line.split('\t')
                    if (columns.size < 5) null else columns
                }
                .sortedBy { it[0] }
        } else {
            emptyList()
        }

        val cards = tutorials.joinToString("\n") { columns ->
            val name = columns[0]
            val title = columns[1]
            val summary = columns[2]
            val width = columns[3]
            val height = columns[4]
            val png = File(root, "$name.png")
            val image = if (png.exists()) {
                val base64 = Base64.getEncoder().encodeToString(png.readBytes())
                """<img src="data:image/png;base64,$base64" alt="$title" style="image-rendering:pixelated;border:1px solid #444;max-width:100%" />"""
            } else {
                """<p style="color:#f88">Missing snapshot: $name.png</p>"""
            }
            """
            <article style="margin:0 0 2rem 0;padding:1rem;border:1px solid #333;border-radius:8px;background:#252525">
                <h2 style="margin-top:0">$title</h2>
                <p style="color:#cfcfcf">$summary</p>
                <p style="color:#8f8f8f;font-size:0.9rem">Snapshot: ${width}x$height</p>
                $image
            </article>
            """.trimIndent()
        }

        val body = if (cards.isBlank()) {
            """
            <p>No UI DSL tutorial snapshots recorded.</p>
            <p>Run <code>./gradlew :awake:engine:game-dsl:desktopTest</code> to regenerate them.</p>
            """.trimIndent()
        } else {
            """
            <p style="color:#cfcfcf">Curated UI facade tutorials generated from desktop snapshot tests. These examples exercise the declarative DSL rather than the raw widget primitives.</p>
            $cards
            """.trimIndent()
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>UI DSL tutorial docs</title>
            </head>
            <body style="font-family:sans-serif;background:#1e1e1e;color:#eee;padding:2rem;max-width:1100px;margin:0 auto">
                <h1>UI DSL tutorial docs</h1>
                $body
            </body>
            </html>
        """.trimIndent()

        val out = reportFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(html)
        println("UI DSL tutorial docs: file://${out.absolutePath}")
    }
}
