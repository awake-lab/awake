// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.app.core

import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AppInstaller

/**
 * Reusable authored game content that can be installed into a root [io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle].
 *
 * A game module owns feature/runtime composition such as scene wiring, UI overlays, debug
 * services, and lifecycle callbacks. The root `game {}` shell still owns application-level
 * concerns like window configuration and platform bootstrap.
 */
interface AppModule : AppInstaller
