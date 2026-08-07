// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.engine.application.GameWindowBackend

internal actual fun platformBackendPreference(): GameWindowBackend = GameWindowBackend.WEBGPU
