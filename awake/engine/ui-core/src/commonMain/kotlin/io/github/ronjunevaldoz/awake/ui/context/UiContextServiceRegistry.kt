// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import kotlin.reflect.KClass

internal class UiContextServiceRegistry {
    private var resolver: ((KClass<*>) -> Any?)? = null

    fun bind(resolver: ((KClass<*>) -> Any?)?) {
        this.resolver = resolver
    }

    fun clear() {
        resolver = null
    }

    fun resolve(type: KClass<*>): Any? = resolver?.invoke(type)
}
