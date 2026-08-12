// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal

import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.ui.heroicons.icon.HeroIcons

/** Small built-in vector icons shared by widgets that need an affordance glyph (e.g.
 * [io.github.ronjunevaldoz.awake.ui.headless.input.dropdown]'s expand indicator) without
 * depending on an external icon font/asset pipeline. Delegates to [io.github.ronjunevaldoz.ui.heroicons.icon.HeroIcons] rather than
 * duplicating path data -- this object exists as the plain, unbranded name headless widgets
 * reach for, distinct from [io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnIcons]
 * (the shadcn-facing, *overridable* registry one layer up). */
object UiIcons {
    val chevronDown: UiImageVector = HeroIcons.Solid20Mini.chevronDown
    val chevronUp: UiImageVector = HeroIcons.Solid20Mini.chevronUp
}
