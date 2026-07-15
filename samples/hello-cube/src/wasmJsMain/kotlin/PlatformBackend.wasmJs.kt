// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.engine.application.GameWindowBackend

internal actual fun platformBackendPreference(): GameWindowBackend = GameWindowBackend.WEBGPU
