// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.ProvideTheme
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.theme.asRuntimeTheme

/**
 * Applies a shadcn theme to everything drawn inside the block.
 *
 * ```kotlin
 * shadcnTheme(dark = true) {
 *     drawApp()
 * }
 * ```
 *
 * The scoped reading of the same theme the factory already produced. `pushTheme`/`popTheme` still
 * work and this is a thin wrapper over them -- the value is that the pair cannot come apart: the
 * pop runs in a `finally`, so content that throws does not leave its theme applied to every widget
 * drawn after it in the frame.
 *
 * This is Awake's answer to `CompositionLocalProvider(LocalTheme provides x) { }`, and the
 * resemblance is real -- a value scoped to a subtree, read by descendants, restored on the way out.
 * The machinery underneath is not Compose's: there is no composition and no recomposition, just a
 * stack pushed and popped as the immediate-mode frame is built. So a block reads the same, while
 * `setContent { App() }` does not transfer -- the runtime drives the frame and calls the UI, rather
 * than a composition owning it.
 */
fun UiScope.shadcnTheme(
    preset: ShadcnStylePreset = ShadcnStylePreset.Vega,
    baseColor: ShadcnBaseColor = ShadcnBaseColor.Neutral,
    accent: ShadcnAccent = ShadcnAccent.Base,
    dark: Boolean = true,
    content: UiScope.() -> Unit,
) = shadcnTheme(
    values = shadcnTheme(preset = preset, baseColor = baseColor, accent = accent, dark = dark),
    content = content,
)

/** Overload for a theme already built -- app state usually holds one rather than four enum knobs. */
fun UiScope.shadcnTheme(values: UiThemeValues, content: UiScope.() -> Unit) {
    primitive.ProvideTheme(values.asRuntimeTheme()) { content() }
}
