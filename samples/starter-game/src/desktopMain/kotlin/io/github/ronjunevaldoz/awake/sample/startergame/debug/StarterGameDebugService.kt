// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.debug

import io.github.ronjunevaldoz.awake.sample.server.DebugService

internal fun StarterGameDebugController.asDebugService(): DebugService<StarterDebugCommand, StarterDebugSnapshot> =
    object : DebugService<StarterDebugCommand, StarterDebugSnapshot> {
        override fun handle(command: StarterDebugCommand) {
            when (command) {
                is StarterDebugCommand.SwitchScene -> switchScene(command.sceneId)
                is StarterDebugCommand.SetTipsVisible -> setTipsVisible(command.enabled)
                StarterDebugCommand.GetState -> Unit
            }
        }

        override fun snapshot(): StarterDebugSnapshot = this@asDebugService.snapshot()
    }
