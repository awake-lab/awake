import java.util.Base64

plugins {
    id("awake.kmp-library-convention")
    id("awake.dokka-convention")
    id("awake.detekt-convention")
    id("awake.spotless-convention")
    id("awake.ui-ownership-convention")
    id("awake.ui-authored-units-convention")
}

kotlin {
    android {
        namespace = "io.github.ronjunevaldoz.awake.ui.headless"
    }

    sourceSets {
        commonMain.dependencies {
            // Public headless contracts may use API values without exposing the runtime.
            api(project(":awake:ui:graphics"))
            api(project(":awake:ui:animation"))
            api(project(":awake:ui:text"))
            implementation(project(":awake:ui:ui-core"))
            implementation(project(":awake:ui:heroicons"))
            implementation(project(":awake:core"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":awake:ui:testing"))
            implementation(project(":awake:ui:ui-core"))
        }
    }
}

tasks.named<Test>("desktopTest") {
    doFirst {
        delete(layout.buildDirectory.dir("ui-snapshots"))
        delete(layout.buildDirectory.dir("reports/ui-snapshots"))
        delete(layout.buildDirectory.dir("reports/ui-tutorials"))
    }
    finalizedBy("uiSnapshotReport", "uiTutorialDocsReport")
}

tasks.register("uiSnapshotReport") {
    group = "verification"
    description = "Generate an HTML gallery of every UI snapshot PNG from the last desktopTest run."
    val snapshotsDir = layout.buildDirectory.dir("ui-snapshots")
    val reportFile = layout.buildDirectory.file("reports/ui-snapshots/index.html")
    doLast {
        val root = snapshotsDir.get().asFile
        val snapshots = root.listFiles { file -> file.isFile && file.extension == "png" }
            ?.sortedBy { it.name } ?: emptyList()

        val cards = snapshots.joinToString("\n") { file ->
            val base64 = Base64.getEncoder().encodeToString(file.readBytes())
            """
            <div style="margin:0 1rem 1rem 0">
                <h3>${file.nameWithoutExtension}</h3>
                <img src="data:image/png;base64,$base64" style="display:block;border:1px solid #444;image-rendering:auto;max-width:100%;height:auto" />
            </div>
            """.trimIndent()
        }

        val body = if (snapshots.isEmpty()) "<p>No UI snapshots recorded.</p>" else
            """<div style="display:flex;flex-wrap:wrap">$cards</div>"""

        val html = """
            <!DOCTYPE html>
            <html><head><meta charset="utf-8"><title>UI snapshot report</title></head>
            <body style="font-family:sans-serif;background:#1e1e1e;color:#eee;padding:2rem">
                <h1>UI snapshot report</h1>
                $body
            </body></html>
        """.trimIndent()

        val out = reportFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(html)
        println("UI snapshot report: file://${out.absolutePath}")
    }
}

tasks.register("uiTutorialDocsReport") {
    group = "documentation"
    description = "Generate an HTML tutorial guide from curated UI tutorial snapshots."
    val snapshotsDir = layout.buildDirectory.dir("ui-snapshots")
    val manifestFile = layout.buildDirectory.file("ui-snapshots/tutorials.tsv")
    val reportFile = layout.buildDirectory.file("reports/ui-tutorials/index.html")
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
                """<img src="data:image/png;base64,$base64" alt="$title" style="display:block;border:1px solid #444;image-rendering:auto;max-width:100%;height:auto" />"""
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
            <p>No tutorial snapshots recorded.</p>
            <p>Run <code>./gradlew :awake:ui:ui-headless:desktopTest</code> to regenerate them.</p>
            """.trimIndent()
        } else {
            """
            <p style="color:#cfcfcf">Curated UI tutorials generated from desktop snapshot tests. Add new entries with <code>saveUiTutorialSnapshot(...)</code>.</p>
            $cards
            """.trimIndent()
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>UI tutorial docs</title>
            </head>
            <body style="font-family:sans-serif;background:#1e1e1e;color:#eee;padding:2rem;max-width:1100px;margin:0 auto">
                <h1>UI tutorial docs</h1>
                $body
            </body>
            </html>
        """.trimIndent()

        val out = reportFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(html)
        println("UI tutorial docs: file://${out.absolutePath}")
    }
}

tasks.register("uiCrossPlatformSnapshotCheck") {
    group = "verification"
    description = "Run shared UI snapshot regression tests on desktop and web targets."
    dependsOn("desktopTest", "wasmJsBrowserTest")
}

tasks.register("auditUiHeadlessThemeIndependence") {
    group = "verification"
    description = "Verifies production Headless sources do not depend on Core theme APIs."
    val sourceRoot = layout.projectDirectory.dir("src/commonMain/kotlin")
    val forbiddenTokens = listOf(
        "UiThemeValues",
        "UiTheme",
        "LocalTheme",
        "ProvideTheme",
        "CoreUiComponentStyles",
        "UiComponentStyles",
    )

    doLast {
        val violations = sourceRoot.asFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    val token = forbiddenTokens.firstOrNull { candidate ->
                        Regex("\\b${Regex.escape(candidate)}\\b").containsMatchIn(line)
                    }
                    token?.let {
                        "${file.relativeTo(projectDir)}:${index + 1} must not reference $it"
                    }
                }
            }
            .toList()

        val message = buildString {
            append("ui-headless theme-independence violations:\n")
            append(violations.joinToString("\n"))
        }
        check(violations.isEmpty()) { message }
        println("ui-headless theme independence: no Core theme API references")
    }
}
