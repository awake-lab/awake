// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.interact
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.UiInteraction
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.withIntrinsicLabelWidth

/**
 * Substring, case-insensitive match against [query] -- the filtering behavior any searchable
 * option list (combobox, filterable select) needs, independent of how the result is drawn.
 * An empty (post-trim) query matches everything. Keeps each option's original index so a
 * caller can still key click handling / selection state off the unfiltered list.
 */
fun filterOptionsByQuery(options: List<String>, query: String): List<IndexedValue<String>> {
    val trimmed = query.trim()
    return options.withIndex().filter { (_, option) ->
        trimmed.isEmpty() || option.contains(trimmed, ignoreCase = true)
    }
}
