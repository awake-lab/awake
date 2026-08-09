// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.utils

import kotlin.native.concurrent.ThreadLocal

// Fps/Delta/FpsString are capitalized against Kotlin convention, but they are public API
// with call sites across the samples -- renaming them is an API change, not formatting.
@Suppress("ktlint:standard:property-naming")
@ThreadLocal
object Time {
    var Fps: Double = 0.0
    var Delta: Double = 0.0
    var FpsString: String = ""
}

@ThreadLocal
object Frame {
    var width: Int = 0
    var height: Int = 0
}
