// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata

internal actual fun previewMetadataFor(
    entry: AwakeUiPreviewEntry,
    reportScale: Int
): AwakeUiPreviewMetadata {
    // No reflection on iOS, return dummy metadata.
    // This is only used for validation tests in commonTest which might be skipped on iOS.
    return AwakeUiPreviewMetadata(
        id = "ios-dummy",
        title = "iOS Dummy",
        group = "Dummy",
        summary = "Dummy",
        width = 1,
        height = 1,
        reportScale = reportScale
    )
}
