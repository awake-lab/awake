// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * A complete, swappable look for every widget kind in a [UiScope] -- assigned once at
 * `ui.column(..., theme = MyTheme)` (or `absolute`), not per widget call. Mirrors Compose's
 * `MaterialTheme.colorScheme` role: the single place a consumer overrides to restyle an
 * entire panel, while individual widget calls can still pass their own [UiStyle] to override
 * just one instance.
 */
interface UiTheme {
    val button: UiStyle
    val toggle: UiStyle
    val slider: UiStyle
    val dropdown: UiStyle

    /** Toggle's checkmark fill -- not state-driven (doesn't change with hovered/active), so
     * it's a plain color on the theme rather than a [UiStyle]. */
    val checkColor: FloatArray
    val labelColor: FloatArray
}

/**
 * The exact colors `UiContext` used to hardcode, extracted into a theme so they're swappable
 * instead of baked into a private `colorFor` method. Every existing widget call keeps
 * rendering identically by defaulting to this.
 */
object DefaultUiTheme : UiTheme {
    private val base = floatArrayOf(0.25f, 0.25f, 0.28f, 0.9f)
    private val hover = floatArrayOf(0.35f, 0.35f, 0.4f, 0.9f)
    private val active = floatArrayOf(0.5f, 0.5f, 0.6f, 0.9f)
    private val neutralStyle = UiStyle { state -> if (state.active) active else if (state.hovered) hover else base }

    override val button = neutralStyle
    override val toggle = neutralStyle
    override val slider = neutralStyle
    override val dropdown = neutralStyle
    override val checkColor = floatArrayOf(0.2f, 0.8f, 0.3f, 1f)
    override val labelColor = floatArrayOf(1f, 1f, 1f, 1f)
}

/**
 * Demonstrates the extensibility this buys -- overrides just [button] via Kotlin interface
 * delegation (`by DefaultUiTheme`), inheriting everything else unchanged. A consumer (or a
 * future "destructive action" button in this repo) builds its own theme the identical way;
 * this isn't wired to any real call site, just the reference shape.
 */
object DangerUiTheme : UiTheme by DefaultUiTheme {
    private val base = floatArrayOf(0.6f, 0.15f, 0.15f, 0.9f)
    private val hover = floatArrayOf(0.75f, 0.2f, 0.2f, 0.9f)
    private val active = floatArrayOf(0.9f, 0.25f, 0.25f, 0.9f)
    override val button = UiStyle { state -> if (state.active) active else if (state.hovered) hover else base }
}
