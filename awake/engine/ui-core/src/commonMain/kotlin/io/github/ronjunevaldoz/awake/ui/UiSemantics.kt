// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Flat semantic record for one UI element emitted during a frame.
 *
 * This is intentionally lightweight and renderer-agnostic: it exists so tests and debug
 * tooling can reason about intent ("button", "text", "panel") without reverse-engineering
 * primitive lists.
 */
enum class UiSemanticRole {
    Text,
    Button,
    Toggle,
    Switch,
    Checkbox,
    Slider,
    Dropdown,
    Panel,
    ScrollPanel,
    Skeleton,
    Spinner
}

data class UiSemanticNode(
    val role: UiSemanticRole,
    val bounds: UiSlot,
    val id: String? = null,
    val label: String? = null,
    val contentBounds: UiSlot? = null,
    val clippedBounds: UiSlot? = null,
    val truncated: Boolean = false,
    val lineCount: Int = 0,
    val selected: Boolean? = null
)
