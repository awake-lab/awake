// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Corner-radius scale derived from one tunable base -- shadcn/ui's `--radius` convention:
 * sm/md are offset down from base, lg IS base, xl is offset up. A consumer retuning the whole
 * app's roundness changes one number, not four independent constants to keep in sync.
 *
 * Fate (decided in the wave-2a value-fix pass, see docs/reference/shadcn-reference-pipeline.md):
 * this stays as `ui-core`'s own neutral, non-shadcn-aware radius fallback -- genuinely needed by
 * several `ui-core`/`ui-headless` call sites that have no theme-preset concept to read from
 * (`ScrollContainers.kt`, `layouts/Surface.kt`, `theme/UiComponentStyles.kt`'s
 * `CoreUiComponentStyles`, and multiple `ui-headless` generic-widget defaults/tests). It is NOT
 * shadcn's radius system and must never be read from `ui-designsystem` component code -- a
 * `shadcn*` component always has a real theme in scope and should read
 * `theme.asShadcnTheme().radii.*` (or, generically, `theme.shapes.*`) instead, so switching
 * `ShadcnStylePreset` actually changes its corners. Four `ui-designsystem` call sites
 * (`DropdownMenu.kt`, `Dialog.kt`, `ShadcnToast.kt`, `ShadcnToggleGroup.kt`) used to read this
 * global directly, silently ignoring preset switches -- rewired to theme radii in that same pass.
 * `UiShape.none` (the zero-`Dp` sentinel) is unaffected by any of this and stays the shared
 * "no radius/no border/no offset" default used everywhere, including inside `ui-designsystem`.
 */
object UiShape {
    var base: Dp = 8f.dp
    val sm: Dp get() = (base.value - 4f).coerceAtLeast(0f).dp
    val md: Dp get() = (base.value - 2f).coerceAtLeast(0f).dp
    val lg: Dp get() = base
    val xl: Dp get() = (base.value + 4f).dp
    val pill: Dp = 9999f.dp
    val none: Dp = 0f.dp
}
