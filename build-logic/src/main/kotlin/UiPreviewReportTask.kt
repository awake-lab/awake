import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.Base64

/**
 * Generates an HTML gallery from a module's `AwakeUiPreviewWriter`-recorded previews
 * (`ui-previews/previews.tsv` + sibling PNGs, both under `build/`). Manifest columns:
 * id, title, group, summary, width, height, reportScale -- this format is a shared contract
 * defined by `awake:engine:ui:ui-testing`'s `AwakeUiPreviewWriter.kt`, not specific to any one
 * module, which is why this task lives here as a reusable convention rather than copy-pasted
 * per sample (extracted from `samples/ui-showcase`'s original inline `uiShowcasePreviewReport`
 * task after a second module needed the same report).
 */
abstract class UiPreviewReportTask : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val reportTitle: Property<String>

    // @Internal, not @InputFile/@InputDirectory: a fresh checkout (or a desktopTest run that
    // recorded zero previews) legitimately has neither of these paths on disk yet, and
    // @InputFile/@InputDirectory validate the path exists BEFORE the task runs even when the
    // property is @Optional (that annotation only lets the property itself be unset, it doesn't
    // waive the on-disk existence check for a path that IS set). generate() already handles a
    // missing manifest/dir gracefully (empty-state HTML), so skip Gradle's input tracking here
    // entirely -- this also means the task always reruns rather than being up-to-date-skipped,
    // which is fine since it's already unconditionally finalizedBy'd after every desktopTest.
    @get:Internal
    abstract val previewsDir: DirectoryProperty

    @get:Internal
    abstract val manifestFile: RegularFileProperty

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @get:OutputFile
    @get:Optional
    abstract val designReportFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val root = previewsDir.get().asFile
        val manifest = manifestFile.get().asFile
        val title = reportTitle.getOrElse("Awake UI Previews")
        val previewColumns = if (manifest.exists()) {
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

        // --- Aggregate Design Report (JSON) ---
        if (designReportFile.isPresent) {
            val designOut = designReportFile.get().asFile
            val designJson = buildString {
                append("[\n")
                previewColumns.forEachIndexed { index, columns ->
                    val id = columns[0]
                    val jsonFile = root.resolve("$id.json")
                    if (jsonFile.exists()) {
                        append(jsonFile.readText().prependIndent("  "))
                        if (index < previewColumns.lastIndex) append(",")
                        append("\n")
                    }
                }
                append("]")
            }
            designOut.parentFile.mkdirs()
            designOut.writeText(designJson)
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

        val cards = previewColumns.joinToString("\n") { columns ->
            val id = columns[0]
            val cardTitle = columns[1]
            val group = columns[2].ifBlank { "General" }
            val summary = columns[3]
            val width = columns[4]
            val height = columns[5]
            val reportScale = columns[6].toIntOrNull()?.coerceAtLeast(1) ?: 1
            val png = root.resolve("$id.png")
            val image = if (png.exists()) {
                val base64 = Base64.getEncoder().encodeToString(png.readBytes())
                """<img src="data:image/png;base64,$base64" alt="${escapeHtml(cardTitle)}" width="${escapeHtml(width)}" height="${escapeHtml(height)}" style="display:block;border:1px solid #2f2f2f;border-radius:12px;max-width:100%;width:min(100%, ${escapeHtml(width)}px);height:auto" />"""
            } else {
                """<p style="color:#f88">Missing preview image: ${escapeHtml(id)}.png</p>"""
            }
            """
            <article style="display:grid;gap:1rem;margin:0 0 1.5rem 0;padding:1.25rem;border:1px solid #262626;border-radius:16px;background:#09090b">
                <div>
                    <p style="margin:0 0 0.35rem 0;color:#a1a1aa;font-size:0.85rem;text-transform:uppercase;letter-spacing:0.08em">${escapeHtml(group)}</p>
                    <h2 style="margin:0 0 0.5rem 0">${escapeHtml(cardTitle)}</h2>
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
            <p>Run <code>./gradlew ${project.path}:desktopTest</code> to regenerate them.</p>
            """.trimIndent()
        } else {
            """
            <p style="color:#d4d4d8">Curated Awake preview entries. Each card is generated from executable desktop tests, so docs and rendering stay in sync.</p>
            $cards
            """.trimIndent()
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>${escapeHtml(title)}</title>
            </head>
            <body style="font-family:Inter,system-ui,sans-serif;background:#020617;color:#fafafa;padding:2rem;max-width:1080px;margin:0 auto">
                <h1 style="margin-top:0">${escapeHtml(title)}</h1>
                $body
            </body>
            </html>
        """.trimIndent()

        val out = reportFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(html)
        println("UI preview report: file://${out.absolutePath}")
    }
}
