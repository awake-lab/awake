// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

private val previewManifestLock = Any()

fun renderAnnotatedUiPreview(entry: AwakeUiPreviewEntry): AwakeUiPreviewScene {
    val scenes = renderAnnotatedUiPreviews(entry)
    require(scenes.size == 1) {
        "Preview entry ${entry::class.qualifiedName} produced ${scenes.size} samples. Use renderAnnotatedUiPreviews(...) instead."
    }
    return scenes.single()
}

fun renderAnnotatedUiPreviews(entry: AwakeUiPreviewEntry): List<AwakeUiPreviewScene> {
    val annotation = entry::class.java.getAnnotation(AwakeUiPreview::class.java)
        ?: error("Preview entry ${entry::class.qualifiedName} is missing @AwakeUiPreview")
    val metadata = AwakeUiPreviewMetadata(
        id = annotation.id,
        title = annotation.title,
        group = annotation.group,
        summary = annotation.summary,
        width = annotation.width,
        height = annotation.height,
        reportScale = annotation.reportScale
    )
    return entry.renderSamples(metadata).map { sample ->
        AwakeUiPreviewScene(
            metadata = AwakeUiPreviewMetadata(
                id = sample.id,
                title = sample.title,
                group = sample.group,
                summary = sample.summary,
                width = sample.width,
                height = sample.height,
                reportScale = sample.reportScale
            ),
            primitives = sample.frame.primitives,
            background = sample.frame.background,
            font = sample.frame.font,
            semantics = sample.frame.semantics
        )
    }
}

fun saveAwakeUiPreview(scene: AwakeUiPreviewScene) {
    val rasterWidth = scene.metadata.rasterWidth
    val rasterHeight = scene.metadata.rasterHeight
    val pixels = scene.primitives.rasterize(
        width = rasterWidth,
        height = rasterHeight,
        background = scene.background,
        font = scene.font
    )
    val image = BufferedImage(rasterWidth, rasterHeight, BufferedImage.TYPE_INT_ARGB)
    var offset = 0
    for (y in 0 until rasterHeight) {
        for (x in 0 until rasterWidth) {
            val r = pixels[offset].toInt() and 0xFF
            val g = pixels[offset + 1].toInt() and 0xFF
            val b = pixels[offset + 2].toInt() and 0xFF
            val a = pixels[offset + 3].toInt() and 0xFF
            image.setRGB(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
            offset += 4
        }
    }

    val outDir = File("build/ui-previews").apply { mkdirs() }
    ImageIO.write(image, "png", File(outDir, "${scene.metadata.id}.png"))

    synchronized(previewManifestLock) {
        val manifest = File(outDir, "previews.tsv")
        val escapedTitle = scene.metadata.title.escapePreviewField()
        val escapedGroup = scene.metadata.group.escapePreviewField()
        val escapedSummary = scene.metadata.summary.escapePreviewField()
        val line = listOf(
            scene.metadata.id,
            escapedTitle,
            escapedGroup,
            escapedSummary,
            scene.metadata.width.toString(),
            scene.metadata.height.toString(),
            scene.metadata.reportScale.toString()
        ).joinToString("\t")
        val entries = linkedMapOf<String, String>()
        if (manifest.exists()) {
            manifest.readLines()
                .filter { it.isNotBlank() }
                .forEach { existing -> entries[existing.substringBefore('\t')] = existing }
        }
        entries[scene.metadata.id] = line
        manifest.writeText(entries.values.joinToString(separator = "\n", postfix = "\n"))
    }
}

private fun String.escapePreviewField(): String = replace('\t', ' ').replace('\n', ' ')
