// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.state

internal enum class UiShowcaseThemeMode(val label: String) {
    Auto("Auto"),
    Light("Light"),
    Dark("Dark"),
}

internal expect fun platformPrefersDarkTheme(): Boolean
