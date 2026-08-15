// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.tailwind

import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement

/**
 * Direct Tailwind flex alignment mappings for Awake Row and Column containers.
 */
public object TwLayout {
    /** `items-center` in a Row (cross-axis centering). */
    public val itemsCenterRow: UiAlignment.Vertical = UiAlignment.Vertical.Center

    /** `items-center` in a Column / Surface (cross-axis centering). */
    public val itemsCenterColumn: UiAlignment.Horizontal = UiAlignment.Horizontal.Center

    /** `justify-center` in a Row (main-axis centering). */
    public val justifyCenterRow: Arrangement = Arrangement.Center

    /** `justify-center` in a Column / Surface (main-axis centering). */
    public val justifyCenterColumn: Arrangement = Arrangement.Center
}
