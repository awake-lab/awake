// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, Awake-Core ${platform.name}!"
    }
}