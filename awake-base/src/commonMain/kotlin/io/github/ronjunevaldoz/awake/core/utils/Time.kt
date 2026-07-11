package io.github.ronjunevaldoz.awake.core.utils

import kotlin.native.concurrent.ThreadLocal

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
@ThreadLocal
object DebugHud {
    var PlayerPositionText: String = ""
}