/*
 * Awake
 * Awake
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

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.library.kmp) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.vanniktech.publish) apply false
}

allprojects {
    group = "io.github.ronjunevaldoz"
    version = "0.1.0-SNAPSHOT"
}

tasks.register("developerDocs") {
    group = "documentation"
    description = "Build developer-facing API references and tutorial artifacts."
    dependsOn(
        ":awake:base:dokkaGeneratePublicationHtml",
        ":awake:ecs:dokkaGeneratePublicationHtml",
        ":awake:engine:game:dokkaGeneratePublicationHtml",
        ":awake:engine:game-dsl:dokkaGeneratePublicationHtml",
        ":awake:engine:game-dsl:desktopTest",
        ":awake:engine:game-dsl:gameDslTutorialDocsReport",
        ":awake:engine:game-dsl:uiDslTutorialDocsReport",
        ":awake:engine:render-api:dokkaGeneratePublicationHtml",
        ":awake:engine:ui:ui-core:dokkaGeneratePublicationHtml",
        ":awake:engine:ui:ui-designsystem:dokkaGeneratePublicationHtml",
        ":awake:engine:ui:ui-unstyled:dokkaGeneratePublicationHtml",
        ":awake:physics:api:dokkaGeneratePublicationHtml",
        ":awake:scene:dokkaGeneratePublicationHtml",
        ":awake:engine:ui:ui-unstyled:desktopTest",
        ":awake:engine:ui:ui-unstyled:uiSnapshotReport",
        ":awake:engine:ui:ui-unstyled:uiTutorialDocsReport",
        ":samples:ui-showcase:desktopTest",
        ":samples:ui-showcase:uiShowcasePreviewReport",
        "uiComponentLookupReport"
    )
}

// A single, searchable component lookup that merges the ui-showcase page-level preview
// gallery (samples/ui-showcase's UiShowcasePreviewDocsTest -> previews.tsv + PNGs) with the
// ui-unstyled bare-widget snapshot gallery (ui-unstyled's UiSnapshotTest -> loose PNGs, no
// manifest). Lives at the root project, not inside either module: ui-unstyled cannot depend on
// samples/ui-showcase (module graph flows the other way -- see docs/architecture.md's Module
// Graph), so a cross-module Kotlin test dependency would be a layering violation. Reading each
// module's already-generated build output after the fact avoids that entirely and keeps both
// existing report tasks untouched.
tasks.register("uiComponentLookupReport") {
    group = "documentation"
    description = "Generate one searchable HTML component lookup across the ui-showcase preview gallery and the ui-unstyled snapshot gallery."
    mustRunAfter(
        ":samples:ui-showcase:uiShowcasePreviewReport",
        ":awake:engine:ui:ui-unstyled:uiSnapshotReport"
    )
    val previewManifestFile = project(":samples:ui-showcase").layout.buildDirectory.file("ui-previews/previews.tsv")
    val previewImagesDir = project(":samples:ui-showcase").layout.buildDirectory.dir("ui-previews")
    val snapshotImagesDir = project(":awake:engine:ui:ui-unstyled").layout.buildDirectory.dir("ui-snapshots")
    val reportFile = layout.buildDirectory.file("reports/ui-component-lookup/index.html")
    doLast {
        // Rows use the same plain List<String> shape ([id, title, group, summary, source,
        // width, height, imagePath]) as the TSV rows the sibling report tasks in
        // samples/ui-showcase/build.gradle.kts and ui-unstyled/build.gradle.kts already parse
        // -- not a data class, which trips a Kotlin JVM IR backend crash ("Exception while
        // generating code for") when declared locally inside a Gradle Kotlin DSL script's
        // doLast block here.
        val idIdx = 0
        val titleIdx = 1
        val groupIdx = 2
        val summaryIdx = 3
        val sourceIdx = 4
        val widthIdx = 5
        val heightIdx = 6
        val imagePathIdx = 7

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

        fun titleCase(id: String): String =
            id.split('-', '_').filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

        val entries = mutableListOf<List<String>>()

        // Source 1: ui-showcase's page-level previews -- already carry id/title/group/summary.
        val previewManifest = previewManifestFile.get().asFile
        val previewRoot = previewImagesDir.get().asFile
        if (previewManifest.exists()) {
            previewManifest.readLines()
                .filter { it.isNotBlank() }
                .forEach { line ->
                    val columns = line.split('\t')
                    if (columns.size < 6) return@forEach
                    val id = columns[0]
                    val png = File(previewRoot, "$id.png")
                    if (!png.exists()) return@forEach
                    entries += listOf(
                        id,
                        columns[1],
                        columns[2].ifBlank { "General" },
                        columns[3],
                        "ui-showcase",
                        columns[4].toIntOrNull()?.toString() ?: "0",
                        columns[5].toIntOrNull()?.toString() ?: "0",
                        png.absolutePath
                    )
                }
        }

        // Source 2: ui-unstyled's bare-widget snapshots -- filename only, no manifest, so
        // title/group/dimensions are derived here instead of assumed to exist.
        val snapshotRoot = snapshotImagesDir.get().asFile
        snapshotRoot.listFiles { file -> file.isFile && file.extension == "png" }
            ?.sortedBy { it.name }
            ?.forEach { png ->
                val id = png.nameWithoutExtension
                val dimensions = try {
                    javax.imageio.ImageIO.read(png)
                } catch (_: Exception) {
                    null
                }
                entries += listOf(
                    id,
                    titleCase(id),
                    titleCase(id.substringBefore('-')),
                    "",
                    "ui-unstyled",
                    (dimensions?.width ?: 0).toString(),
                    (dimensions?.height ?: 0).toString(),
                    png.absolutePath
                )
            }

        val grouped = entries
            .sortedWith(compareBy({ it[groupIdx] }, { it[titleIdx] }))
            .groupBy { it[groupIdx] }

        val sections = grouped.entries.joinToString("\n") { (group, groupEntries) ->
            val cards = groupEntries.joinToString("\n") { entry ->
                val image = File(entry[imagePathIdx])
                val base64 = java.util.Base64.getEncoder().encodeToString(image.readBytes())
                val search = "${entry[idIdx]} ${entry[titleIdx]} ${entry[groupIdx]} ${entry[sourceIdx]}".lowercase()
                val summary = entry[summaryIdx]
                """
                <article class="lookup-card" data-search="${escapeHtml(search)}" style="display:grid;gap:0.75rem;margin:0 0 1.25rem 0;padding:1.1rem;border:1px solid #262626;border-radius:14px;background:#09090b">
                    <div>
                        <p style="margin:0 0 0.3rem 0;color:#a1a1aa;font-size:0.78rem;text-transform:uppercase;letter-spacing:0.08em">${escapeHtml(entry[sourceIdx])}</p>
                        <h3 style="margin:0 0 0.4rem 0">${escapeHtml(entry[titleIdx])}</h3>
                        ${if (summary.isNotBlank()) """<p style="margin:0 0 0.4rem 0;color:#d4d4d8;font-size:0.9rem">${escapeHtml(summary)}</p>""" else ""}
                        <p style="margin:0;color:#71717a;font-size:0.82rem">${entry[widthIdx]}x${entry[heightIdx]} &middot; ${escapeHtml(entry[idIdx])}</p>
                    </div>
                    <img src="data:image/png;base64,$base64" alt="${escapeHtml(entry[titleIdx])}" style="display:block;border:1px solid #2f2f2f;border-radius:10px;max-width:100%;height:auto" />
                </article>
                """.trimIndent()
            }
            """
            <section class="lookup-group" data-group="${escapeHtml(group)}">
                <h2 style="margin:1.5rem 0 0.75rem 0;color:#e4e4e7">${escapeHtml(group)}</h2>
                $cards
            </section>
            """.trimIndent()
        }

        val body = if (entries.isEmpty()) {
            """
            <p>No components recorded yet.</p>
            <p>Run <code>./gradlew :samples:ui-showcase:desktopTest :awake:engine:ui:ui-unstyled:desktopTest uiComponentLookupReport</code> to regenerate.</p>
            """.trimIndent()
        } else {
            sections
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Awake UI Component Lookup</title>
            </head>
            <body style="font-family:sans-serif;background:#000;color:#e4e4e7;padding:2rem;max-width:1100px;margin:0 auto">
                <h1 style="margin-bottom:0.25rem">Awake UI Component Lookup</h1>
                <p style="color:#a1a1aa;margin-top:0">
                    ${entries.size} components across <code>ui-showcase</code> (page-level previews) and
                    <code>ui-unstyled</code> (bare-widget snapshots). Generated from tests, not curated by hand.
                </p>
                <input
                    id="lookup-search"
                    type="text"
                    placeholder="Filter by component name..."
                    autofocus
                    oninput="filterLookup()"
                    style="width:100%;box-sizing:border-box;padding:0.65rem 0.9rem;margin:0.75rem 0 1.25rem 0;border-radius:10px;border:1px solid #333;background:#0a0a0a;color:#e4e4e7;font-size:1rem"
                />
                <div id="lookup-results">
                    $body
                </div>
                <script>
                function filterLookup() {
                    var query = document.getElementById('lookup-search').value.toLowerCase().trim();
                    document.querySelectorAll('.lookup-group').forEach(function (section) {
                        var anyVisible = false;
                        section.querySelectorAll('.lookup-card').forEach(function (card) {
                            var match = query === '' || card.getAttribute('data-search').indexOf(query) !== -1;
                            card.style.display = match ? '' : 'none';
                            if (match) anyVisible = true;
                        });
                        section.style.display = anyVisible ? '' : 'none';
                    });
                }
                </script>
            </body>
            </html>
        """.trimIndent()

        val out = reportFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(html)
        println("UI component lookup: file://${out.absolutePath}")
    }
}
