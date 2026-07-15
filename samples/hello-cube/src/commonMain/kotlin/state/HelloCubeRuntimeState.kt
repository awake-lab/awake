// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.engine.application.FrameStats

internal enum class HelloCubeCameraMode {
    ORBIT,
    FREE_FLY
}

internal class HelloCubeRuntimeState(
    var mode: HelloCubeCameraMode = HelloCubeCameraMode.ORBIT,
    val frameStats: FrameStats = FrameStats()
) {
    var minimapEnabled = false
}
