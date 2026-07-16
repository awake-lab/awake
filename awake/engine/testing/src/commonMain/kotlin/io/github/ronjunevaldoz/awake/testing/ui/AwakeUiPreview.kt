// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AwakeUiPreview(
    val id: String,
    val title: String,
    val group: String = "",
    val summary: String = "",
    val width: Int = 960,
    val height: Int = 720
)

data class AwakeUiPreviewMetadata(
    val id: String,
    val title: String,
    val group: String,
    val summary: String,
    val width: Int,
    val height: Int
)

data class AwakeUiPreviewFrame(
    val primitives: List<UiDrawPrimitive>,
    val background: Color = Color(0.1f, 0.1f, 0.12f, 1f),
    val font: UiFont? = null
)

data class AwakeUiPreviewScene(
    val metadata: AwakeUiPreviewMetadata,
    val primitives: List<UiDrawPrimitive>,
    val background: Color,
    val font: UiFont?
)

interface AwakeUiPreviewEntry {
    fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame
}
