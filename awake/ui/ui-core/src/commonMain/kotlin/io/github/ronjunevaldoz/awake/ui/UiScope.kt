// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Temporary source-compatibility name for Core's raw primitive receiver.
 *
 * Ordinary widget code must migrate to Headless' public `UiScope` facade. Core primitives and
 * deliberate advanced authoring use [UiPrimitiveScope].
 */
@Deprecated(
    message = "Core's raw UI receiver was renamed to UiPrimitiveScope. Ordinary widgets should use Headless UiScope.",
    replaceWith = ReplaceWith("UiPrimitiveScope"),
)
typealias UiScope = UiPrimitiveScope
