// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.state

import platform.UIKit.UIUserInterfaceStyleDark
import platform.UIKit.UIScreen

internal actual fun platformPrefersDarkTheme(): Boolean =
    UIScreen.mainScreen.traitCollection.userInterfaceStyle == UIUserInterfaceStyleDark
