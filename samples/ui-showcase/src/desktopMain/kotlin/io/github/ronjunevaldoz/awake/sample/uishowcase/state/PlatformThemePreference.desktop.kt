// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.state

internal actual fun platformPrefersDarkTheme(): Boolean {
    val macAppearance = System.getProperty("apple.awt.application.appearance")
        ?.lowercase()
        ?.contains("dark") == true
    val envOverride = System.getenv("AWAKE_DARK_MODE")
    return when (envOverride?.lowercase()) {
        "1", "true", "dark" -> true
        "0", "false", "light" -> false
        else -> macAppearance
    }
}
