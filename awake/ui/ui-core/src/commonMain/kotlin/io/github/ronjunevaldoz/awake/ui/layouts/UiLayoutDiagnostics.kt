// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

/**
 * Escape hatches for layout checks that turn a silent wrong answer into a thrown one.
 *
 * Every layout defect found this week was the same shape: a modifier accepted, dropped, and no
 * error -- `verticalScroll` on `row()`/`box()`, `weight()` under a column that never planned
 * slots. The check is the fix until the capability exists; the flag is here so a caller that has
 * measured the fallback and wants it can say so explicitly rather than by accident.
 */
object UiLayoutDiagnostics {

    /**
     * Lets a weighted column child fall through to the wrap-content fallback instead of throwing.
     *
     * Only set this with a measurement in hand. The fallback does not divide the parent's height
     * -- the child wraps -- so anything relying on it is reading a number that looks like a
     * layout and is not one.
     */
    var allowUnplannedWeight: Boolean = false
}
