// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.tw
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnInputOtpSlotStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnLegacyAmbientSurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.box
import io.github.ronjunevaldoz.awake.ui.headless.clickable
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.otpInput
import io.github.ronjunevaldoz.awake.ui.headless.requestFocus
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.width

fun UiScope.shadcnInputOTP(
    id: String,
    value: String,
    length: Int = 6,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    groupSize: Int = 0,
    onValueChange: (String) -> Unit = {},
): String {
    var resolved = value
    // Explicit style, not a bare surface() -- this wrapper used to inherit a full card look
    // (background/border/padding) from ui-core's now-deleted ambient `theme.components.surface`
    // fallback purely as a side effect of `surface()`'s Panel-role auto-detection, not because
    // this wrapper asked for one. Reproduces that exact same look explicitly rather than
    // silently changing it -- see the parent task write-up for the "is this actually correct"
    // follow-up (real shadcn's InputOTP has no such wrapper card at all).
    surface(id = "$id.slots", modifier = modifier, style = shadcnLegacyAmbientSurfaceStyle(themeValues)) {
        resolved = otpInput(
            id = id,
            value = value,
            length = length,
            fieldModifier = Modifier.height(9.tw).width(1f.dp),
            enabled = enabled,
            groupSize = groupSize,
            horizontalArrangement = Arrangement.spacedBy(1.5.tw),
            onValueChange = onValueChange,
            separator = { text("-") },
        ) { index, char ->
            surface(
                id = "$id.slot.$index",
                modifier = Modifier.height(9.tw).width(9.tw).clickable { requestFocus(id) },
                style = shadcnInputOtpSlotStyle(themeValues, enabled, isError),
            ) {
                box(modifier = Modifier.fillMaxSize(), contentAlignment = UiAlignment.Center) {
                    text(char, centered = true)
                }
            }
        }
    }
    return resolved
}
