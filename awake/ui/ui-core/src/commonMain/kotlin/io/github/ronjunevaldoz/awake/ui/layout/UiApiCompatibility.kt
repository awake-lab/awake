// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layout

import io.github.ronjunevaldoz.awake.ui.api.layout.contains as apiContains
import io.github.ronjunevaldoz.awake.ui.api.layout.intersect as apiIntersect

/** Temporary source bridge while core implementation files migrate to ui-api contracts. */
@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds instead.")
typealias UiBounds = io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.layout.intersect instead.")
fun UiBounds.intersect(other: UiBounds): UiBounds = this.apiIntersect(other)

@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.layout.contains instead.")
fun UiBounds.contains(other: UiBounds): Boolean = this.apiContains(other)
