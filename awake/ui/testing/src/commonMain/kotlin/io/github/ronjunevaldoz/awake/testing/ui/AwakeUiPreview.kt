// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.font.UiFont

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AwakeUiPreview(
    val id: String,
    val title: String,
    val group: String = "",
    val summary: String = "",
    val width: Int = 960,
    val height: Int = 720,
    val reportScale: Int = 1,
)

data class AwakeUiPreviewMetadata(
    val id: String,
    val title: String,
    val group: String,
    val summary: String,
    val width: Int,
    val height: Int,
    val reportScale: Int = 1,
) {
    val rasterWidth: Int get() = width * reportScale.coerceAtLeast(1)
    val rasterHeight: Int get() = height * reportScale.coerceAtLeast(1)
}

data class AwakeUiPreviewSample(
    val id: String,
    val title: String,
    val group: String,
    val summary: String,
    val width: Int,
    val height: Int,
    val reportScale: Int,
    val frame: AwakeUiPreviewFrame,
)

data class AwakeUiPreviewFrame(
    val primitives: List<UiDrawPrimitive>,
    val background: Color = Color(0.1f, 0.1f, 0.12f, 1f),
    val font: UiFont? = null,
    val semantics: List<UiSemanticNode> = emptyList(),
)

data class AwakeUiPreviewScene(
    val metadata: AwakeUiPreviewMetadata,
    val primitives: List<UiDrawPrimitive>,
    val background: Color,
    val font: UiFont?,
    val semantics: List<UiSemanticNode> = emptyList(),
)

interface AwakeUiPreviewEntry {
    fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame

    fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> = listOf(
        metadata.sample(frame = render(metadata)),
    )
}

fun AwakeUiPreviewMetadata.sample(
    frame: AwakeUiPreviewFrame,
    idSuffix: String = "",
    titleSuffix: String = "",
    summarySuffix: String = "",
): AwakeUiPreviewSample = AwakeUiPreviewSample(
    id = if (idSuffix.isBlank()) id else "$id-$idSuffix",
    title = if (titleSuffix.isBlank()) title else "$title - $titleSuffix",
    group = group,
    summary = if (summarySuffix.isBlank()) summary else "$summary $summarySuffix".trim(),
    width = width,
    height = height,
    reportScale = reportScale,
    frame = frame,
)
