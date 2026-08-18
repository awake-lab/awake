// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope

/**
 * Keeps Design System tests on the public Headless facade while retaining the Core-owned frame
 * lifecycle used by the test harness.
 */
internal fun UiContext.headlessRoot(bounds: UiBounds = frameBoundsInternal()): UiScope = createUiScope(bounds)
