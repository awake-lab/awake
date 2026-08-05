// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.shader

interface AttributeBinder {
    fun bind(name: String, location: Int)
    fun getLocation(name: String): Int
}
