// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.game.GameWindowBackend

internal actual fun platformBackendPreference(): GameWindowBackend = GameWindowBackend.VULKAN
