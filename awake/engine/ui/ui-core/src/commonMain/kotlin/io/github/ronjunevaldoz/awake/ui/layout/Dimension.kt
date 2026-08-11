// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layout

import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension as ApiDimension
import io.github.ronjunevaldoz.awake.ui.api.layout.LayoutWeight as ApiLayoutWeight
import io.github.ronjunevaldoz.awake.ui.api.layout.toDimension as apiToDimension

/** @deprecated Import this contract from `io.github.ronjunevaldoz.awake.ui.api.layout`. */
@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension instead.")
typealias Dimension = ApiDimension

/** @deprecated Import this contract from `io.github.ronjunevaldoz.awake.ui.api.layout`. */
@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.layout.LayoutWeight instead.")
typealias LayoutWeight = ApiLayoutWeight

/** Preserves the historical "pass `0f` (or negative) and it fills the enclosing scope" call
 * pattern this codebase's own widgets (and sample app) used before [Dimension] existed --
 * public, not file-private to the built-in widget implementations, so a consumer's own custom widget (e.g. `Gauge.kt`)
 * gets the exact same convenience a built-in widget does, matching this module's "no
 * capability gap versus a built-in widget" guarantee. */
fun Float.toDimension(): Dimension = if (this > 0f) ApiDimension.Fixed(this.px) else ApiDimension.FillMax

@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.layout.toDimension instead.")
fun Dp.toDimension(): Dimension = this.apiToDimension()
