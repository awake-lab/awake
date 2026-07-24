// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiSemanticNode

internal class UiSemanticCollector {
    private val nodes = ArrayList<UiSemanticNode>()

    fun beginFrame() {
        nodes.clear()
    }

    fun record(node: UiSemanticNode, measuring: Boolean) {
        if (!measuring) nodes += node
    }

    fun snapshot(): List<UiSemanticNode> = nodes.toList()
}
