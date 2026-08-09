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

/**
 * MVP1a debug readout (see docs/MMORPG_ROADMAP.md): mirrors [Time]'s polled-singleton shape
 * -- [demo.SceneRuntimeHost] writes [PlayerPositionText] once per `fixedUpdate`, the Compose
 * HUD (`App.kt`) polls it the same way it already polls [Time.FpsString].
 */
// PlayerPositionText mirrors Time's Capitalized property naming for the same reason:
// it's public API polled by App.kt, so renaming it is an API change, not formatting.
@Suppress("ktlint:standard:property-naming")
@ThreadLocal
object DebugHud {
    var PlayerPositionText: String = ""
}
