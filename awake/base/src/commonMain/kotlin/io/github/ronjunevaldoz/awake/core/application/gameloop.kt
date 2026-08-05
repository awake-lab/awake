// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.application


interface GameLoop {
    fun startLoop(onUpdate: (deltaTime: Double) -> Unit)
}
