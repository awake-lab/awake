/*
 * Awake
 * Awake.awake-core.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.core.application

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
