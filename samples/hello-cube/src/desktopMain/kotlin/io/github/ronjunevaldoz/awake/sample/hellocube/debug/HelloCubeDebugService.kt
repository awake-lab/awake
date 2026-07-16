// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.debug

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.sample.server.DebugService

internal fun HelloCubeDebugController.asDebugService(): DebugService<DebugCommand, DebugSnapshot> =
    object : DebugService<DebugCommand, DebugSnapshot> {
        override fun handle(command: DebugCommand) {
            when (command) {
                is DebugCommand.SwitchDemo -> switchDemo(command.index)
                is DebugCommand.SetCameraEye -> setCameraEye(Vec3(command.x, command.y, command.z))
                is DebugCommand.SetCameraCenter -> setCameraCenter(Vec3(command.x, command.y, command.z))
                is DebugCommand.SetMinimap -> setMinimap(command.enabled)
                DebugCommand.GetState -> Unit
            }
        }

        override fun snapshot(): DebugSnapshot = this@asDebugService.snapshot()
    }
