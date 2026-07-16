// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.state

import android.content.res.Configuration
import android.content.res.Resources

internal actual fun platformPrefersDarkTheme(): Boolean {
    val mode = Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mode == Configuration.UI_MODE_NIGHT_YES
}
