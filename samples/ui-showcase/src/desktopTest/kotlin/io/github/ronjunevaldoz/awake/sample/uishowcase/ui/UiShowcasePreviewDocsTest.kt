// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import kotlin.test.Test

class UiShowcasePreviewDocsTest {

    @Test
    fun writeShowcasePreviews() {
        UiShowcasePreviewEntries.forEach { entry ->
            saveAwakeUiPreview(renderAnnotatedUiPreview(entry))
        }
    }
}
