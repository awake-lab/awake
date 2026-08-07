// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata

internal actual fun previewMetadataFor(
    entry: AwakeUiPreviewEntry,
    reportScale: Int,
): AwakeUiPreviewMetadata {
    // No reflection on wasmJs, return dummy metadata.
    return AwakeUiPreviewMetadata(
        id = "wasm-dummy",
        title = "Wasm Dummy",
        group = "Dummy",
        summary = "Dummy",
        width = 1,
        height = 1,
        reportScale = reportScale,
    )
}
