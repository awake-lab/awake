// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.scope.frameBounds as primitiveFrameBounds

/** Current frame bounds available to ordinary Headless recipes without exposing [UiScope]'s runtime. */
fun UiScope.frameBounds(): UiBounds = primitive.primitiveFrameBounds()
