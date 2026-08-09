// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.app

import io.github.ronjunevaldoz.awake.engine.game.GameWindowBackend

internal actual fun platformBackendPreference(): GameWindowBackend = GameWindowBackend.WEBGPU
