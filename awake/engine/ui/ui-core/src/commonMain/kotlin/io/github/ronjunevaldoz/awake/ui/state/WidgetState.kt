// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Per-widget-id state a [io.github.ronjunevaldoz.awake.ui.context.UiContext] keeps across frames. Generic on purpose -- `dropdown`'s
 * expanded/collapsed flag is just the library's own first consumer of this, not a hardcoded
 * capability; a custom widget persists whatever typed value it needs the same way.
 */
class WidgetState {
    private val values = HashMap<String, Any?>()

    // Callers are expected to use a stable, unique key per value type (same contract as any
    // typed key-value store); getOrPut only ever inserts `default: T` on a miss, so an
    // existing entry under `key` was itself put there as a T by an earlier call.
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, default: T): T = values.getOrPut(key) { default } as T

    fun <T> set(key: String, value: T) {
        values[key] = value
    }

    fun remove(key: String) {
        values.remove(key)
    }
}
