// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl

import io.github.ronjunevaldoz.awake.engine.app.core.AppModule
import io.github.ronjunevaldoz.awake.engine.app.core.AppSpec
import io.github.ronjunevaldoz.awake.engine.app.dsl.AppSpecBuilder
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle

fun appModule(
    block: AppModuleDsl.() -> Unit,
): AppModule = object : AppModule {
    override fun install(into: AppSpecBuilder) {
        AppModuleDsl(into).block()
    }
}

fun AppDsl.module(module: AppModule) {
    install(module)
}

fun AppDsl.module(
    block: AppModuleDsl.() -> Unit,
) {
    install(appModule(block))
}

fun AppModule.createApp(
    window: WindowDsl.() -> Unit,
): AwakeAppLifecycle = createAppSpec(window).createLifecycle()

fun AppModule.createAppSpec(
    window: WindowDsl.() -> Unit,
): AppSpec = appSpec {
    window(window)
    module(this@createAppSpec)
}

@AwakeGameDsl
class AppModuleDsl internal constructor(
    builder: AppSpecBuilder,
) : AppSpecDsl(builder) {

    fun module(module: AppModule) {
        install(module)
    }

    fun module(block: AppModuleDsl.() -> Unit) {
        install(appModule(block))
    }
}
