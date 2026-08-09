// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.core.components

import io.github.ronjunevaldoz.awake.ecs.Poolable

data class Name(
    var value: String = "",
) : Poolable {
    override fun reset() {
        value = ""
    }
}
