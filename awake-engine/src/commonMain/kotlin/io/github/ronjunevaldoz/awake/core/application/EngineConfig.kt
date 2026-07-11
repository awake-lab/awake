// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.application

import kotlin.concurrent.Volatile
import kotlin.native.concurrent.ThreadLocal

data class EngineConfig(
    var fps: Int = 60, // frame per second
    var ups: Double = 1.0 / 30.0, // update per second
)

/** Backend-agnostic frame-rate config the [GameLoop] actuals read -- deliberately decoupled
 * from any particular rendering backend's context/config type (see docs/MVP_PLAN.md's
 * Decision Log, D11 follow-up) so a headless consumer never needs to depend on a rendering
 * module just to configure its own tick rate. Rendering backends (e.g. awake-opengl's
 * `AwakeContext.init`) mirror their own fps/ups into this holder for existing call sites
 * to keep working unchanged. */
@ThreadLocal
object EngineConfigHolder {
    @Volatile
    var config: EngineConfig = EngineConfig()
}
