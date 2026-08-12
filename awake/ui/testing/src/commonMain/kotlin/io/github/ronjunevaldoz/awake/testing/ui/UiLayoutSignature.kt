// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/** Golden-layout signature for a full screen/page composition -- a structural fingerprint of
 * every recorded [UiSemanticNode]'s role, id, and rounded bounds, independent of color/style.
 * Catches unintended layout drift (a widget moving, resizing, or disappearing) at sample level
 * without the flakiness or maintenance cost of a full pixel screenshot: a style-only change
 * (recoloring a theme) leaves the signature untouched, while a layout regression (row math
 * shifting a widget by a few pixels) changes it. Bounds are rounded to whole pixels so
 * sub-pixel float noise across platforms/compilers doesn't produce spurious drift. */
fun layoutSignature(nodes: List<UiSemanticNode>): ULong {
    var hash = 0xcbf29ce484222325uL
    fun mix(value: Long) {
        hash = hash xor value.toULong()
        hash *= 0x100000001b3uL
    }
    fun mixSlot(slot: UiBounds) {
        mix(slot.x.toInt().toLong())
        mix(slot.y.toInt().toLong())
        mix(slot.width.toInt().toLong())
        mix(slot.height.toInt().toLong())
    }
    nodes.sortedWith(compareBy({ it.bounds.y }, { it.bounds.x }, { it.id ?: "" })).forEach { node ->
        mix(node.role.ordinal.toLong())
        (node.id ?: "").forEach { mix(it.code.toLong()) }
        mixSlot(node.bounds)
    }
    return hash
}

/** Renders a human-readable description of [nodes] for a failing golden-layout assertion --
 * one line per node, sorted the same way [layoutSignature] sorts, so a diff between two runs'
 * printed descriptions localizes exactly which widget moved. */
fun describeLayout(nodes: List<UiSemanticNode>): String = nodes
    .sortedWith(compareBy({ it.bounds.y }, { it.bounds.x }, { it.id ?: "" }))
    .joinToString(separator = "\n") { node ->
        val bounds = node.bounds
        "${node.role}[${node.id ?: "?"}] @ (${bounds.x.toInt()}, ${bounds.y.toInt()}) ${bounds.width.toInt()}x${bounds.height.toInt()}"
    }
