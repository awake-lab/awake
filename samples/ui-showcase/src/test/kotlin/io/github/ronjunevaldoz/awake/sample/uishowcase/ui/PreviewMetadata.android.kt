// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata

internal actual fun previewMetadataFor(
    entry: AwakeUiPreviewEntry,
    reportScale: Int
): AwakeUiPreviewMetadata {
    val annotation = requireNotNull(entry.javaClass.getAnnotation(AwakeUiPreview::class.java)) {
        "missing @AwakeUiPreview on ${entry.javaClass.name}"
    }
    return AwakeUiPreviewMetadata(
        id = annotation.id,
        title = annotation.title,
        group = annotation.group,
        summary = annotation.summary,
        width = annotation.width,
        height = annotation.height,
        reportScale = reportScale
    )
}
