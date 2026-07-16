// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.state

import kotlinx.browser.window

internal actual fun platformPrefersDarkTheme(): Boolean =
    window.matchMedia("(prefers-color-scheme: dark)").matches
