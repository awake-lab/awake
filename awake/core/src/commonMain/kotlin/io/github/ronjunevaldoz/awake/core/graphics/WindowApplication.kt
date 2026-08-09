// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.graphics

import io.github.ronjunevaldoz.awake.core.input.Input

interface WindowApplication {
    val input: Input
    fun create(surface: Any? = null)
    fun update(delta: Float)
    fun pause()
    fun resume()
    fun resize(x: Int, y: Int, width: Int, height: Int)
    fun dispose()
}
