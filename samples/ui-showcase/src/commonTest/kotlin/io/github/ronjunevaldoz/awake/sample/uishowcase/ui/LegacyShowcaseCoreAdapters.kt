// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope as CoreColumnScope

/** Test-only receiver adapters for legacy preview harnesses while their roots migrate. */
internal fun CoreColumnScope.drawUiShowcaseSidebar(compact: Boolean) {
    context.createUiScope(context.frameBounds()).column(modifier = Modifier.fillMaxSize()) {
        drawUiShowcaseSidebar(compact)
    }
}

internal fun CoreColumnScope.drawUiShowcasePageContent(
    state: UiShowcaseRuntimeState,
    showInlineMenu: Boolean,
) {
    context.createUiScope(context.frameBounds()).column(modifier = Modifier.fillMaxSize()) {
        drawUiShowcasePageContent(state, showInlineMenu)
    }
}

internal fun CoreColumnScope.drawUiShowcaseCanvasPreview() {
    context.createUiScope(context.frameBounds()).column(modifier = Modifier.fillMaxSize()) {
        drawUiShowcaseCanvasPreview()
    }
}

internal fun CoreColumnScope.renderUiShowcasePagePreview(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    context.createUiScope(context.frameBounds()).column(modifier = Modifier.fillMaxSize()) {
        renderUiShowcasePagePreview(page, state)
    }
}
