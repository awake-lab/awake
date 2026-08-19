package io.github.ronjunevaldoz.awake.engine.app.lifecycle

import io.github.ronjunevaldoz.awake.engine.app.dsl.AppSpecBuilder

interface AppInstaller {
    fun install(into: AppSpecBuilder)
}