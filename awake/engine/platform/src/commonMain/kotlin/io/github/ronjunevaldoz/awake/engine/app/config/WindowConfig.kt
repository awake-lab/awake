package io.github.ronjunevaldoz.awake.engine.app.config

import io.github.ronjunevaldoz.awake.engine.app.dsl.AppWindowBackend

data class WindowConfig(
    val title: String,
    val width: Int,
    val height: Int,
    val backend: AppWindowBackend,
)