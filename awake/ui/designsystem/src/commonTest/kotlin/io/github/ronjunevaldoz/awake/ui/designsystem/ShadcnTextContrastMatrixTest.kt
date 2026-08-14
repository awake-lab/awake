// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.surface
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Every variant that pairs a fill with a label, checked for readable contrast.
 *
 * `ShadcnTooltipContrastTest` exists because the tooltip once shipped dark-on-dark: `surface()`
 * pushed its text style but not its `foreground`, so the label fell back to the theme foreground
 * and matched the pill it sat on. That produced one test, for one component, asserting one exact
 * colour -- so when the Primary badge did the same thing it went unnoticed.
 *
 * This asserts a RATIO instead of an expected colour, which means it needs no per-variant
 * bookkeeping and covers variants nobody thought to enumerate. Ratio is WCAG relative luminance,
 * measured between each drawn glyph and the fill actually behind it, in both themes.
 */
class ShadcnTextContrastMatrixTest {

    /** WCAG relative luminance: sRGB channels linearised, then weighted. */
    private fun relativeLuminance(color: Color): Float {
        fun linearise(raw: Float): Float {
            val c = raw.coerceIn(0f, 1f)
            return if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
        }
        return 0.2126f * linearise(color.r) + 0.7152f * linearise(color.g) + 0.0722f * linearise(color.b)
    }

    private fun contrastRatio(a: Color, b: Color): Float {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        val lighter = maxOf(la, lb)
        val darker = minOf(la, lb)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private data class Case(val label: String, val render: (UiContext) -> Unit)

    /**
     * Renders [body] inside a surface that has already pushed a text colour.
     *
     * This is the dimension that matters. surface() only adopts its own `foreground` as the
     * content colour when nothing was inherited -- `resolved.textStyle.color == null` -- so a
     * badge nested in any coloured surface keeps the ANCESTOR's text colour and ignores its own.
     * Standalone it measures 17:1; inside the showcase's preview card it renders dark-on-dark.
     */
    private fun UiContext.insideColouredSurface(body: () -> Unit) {
        headlessRoot().surface(
            id = "contrast-host",
            style = SurfaceStyle(
                background = currentTheme.colors.card,
                foreground = currentTheme.colors.foreground,
            ),
        ) { body() }
    }

    private fun cases(): List<Case> = buildList {
        ShadcnBadgeVariant.entries.forEach { variant ->
            add(
                Case("badge/${variant.name}") { ui ->
                    ui.headlessRoot().shadcnBadge(
                        id = "contrast-badge",
                        label = "Label",
                        variant = variant,
                    )
                },
            )
            add(
                Case("badge/${variant.name}@nested") { ui ->
                    ui.insideColouredSurface {
                        ui.headlessRoot().shadcnBadge(
                            id = "contrast-badge-nested",
                            label = "Label",
                            variant = variant,
                        )
                    }
                },
            )
        }
        ShadcnButtonVariant.entries.forEach { variant ->
            add(
                Case("button/${variant.name}") { ui ->
                    ui.headlessRoot().shadcnButton(
                        id = "contrast-button",
                        label = "Label",
                        variant = variant,
                    )
                },
            )
        }
    }

    /** Ratio between each glyph and the nearest fill drawn under it, or null when nothing is drawn. */
    private fun worstRatio(dark: Boolean, case: Case): Pair<Float, String>? {
        val theme = shadcnTheme(dark = dark)
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(theme)
        ui.beginFrame(300f, 150f, testSnapshot(x = -100f, y = -100f, down = false))
        case.render(ui)
        val primitives = ui.endFrame()

        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        if (glyphs.isEmpty()) return null

        // Opaque fills, in paint order; the last one covering a glyph is what it sits on. A
        // transparent variant paints none, so the page background stands in.
        val fills = primitives.mapNotNull { p ->
            when (p) {
                is UiDrawPrimitive.Quad -> if (p.color.a > 0.9f) Triple(p.x, p.y, Triple(p.w, p.h, p.color)) else null
                is UiDrawPrimitive.RoundedQuad ->
                    if (p.color.a > 0.9f) Triple(p.x, p.y, Triple(p.w, p.h, p.color)) else null
                else -> null
            }
        }

        var worst = Float.MAX_VALUE
        var detail = ""
        glyphs.forEach { glyph ->
            val cx = glyph.x + glyph.w / 2f
            val cy = glyph.y + glyph.h / 2f
            val behind = fills.lastOrNull { (fx, fy, rest) ->
                val (fw, fh, _) = rest
                cx in fx..(fx + fw) && cy in fy..(fy + fh)
            }?.third?.third ?: theme.colors.background
            val ratio = contrastRatio(glyph.color, behind)
            if (ratio < worst) {
                worst = ratio
                detail = "glyph=${glyph.color} behind=$behind"
            }
        }
        return worst to detail
    }

    @Test
    fun everyVariantsLabelIsReadableOnItsOwnFill() {
        val failures = mutableListOf<String>()
        val report = mutableListOf<String>()

        listOf(false, true).forEach { dark ->
            cases().forEach { case ->
                val result = worstRatio(dark, case) ?: return@forEach
                val (ratio, detail) = result
                val mode = if (dark) "dark" else "light"
                val ok = ratio >= MIN_RATIO || "$mode ${case.label}" in KNOWN_BELOW_FLOOR
                report += "  ${if (ok) "ok  " else "FAIL"} $mode ${case.label}: ratio=$ratio"
                if (!ok) failures += "$mode ${case.label} ratio=$ratio ($detail)"
            }
        }

        assertTrue(
            failures.isEmpty(),
            "${failures.size} variant(s) render their label below a $MIN_RATIO:1 contrast ratio " +
                "-- dark-on-dark or light-on-light:\n" + report.joinToString("\n"),
        )
    }

    private companion object {
        /**
         * Accepted, upstream-matching shortfalls -- NOT a way to silence new ones.
         *
         * Danger is `bg-destructive text-white`, and upstream additionally applies
         * `dark:bg-destructive/60` so the fill composites darker in dark mode and white stays
         * readable. We paint destructive at full opacity, so white measures 2.89:1 -- just under
         * the floor. Fixing it properly needs the theme to expose its own darkness (there is no
         * `UiThemeValues.isDark` today) or the dark palette to carry the alpha, so it is recorded
         * here with the reason rather than hidden by lowering MIN_RATIO for everything.
         */
        val KNOWN_BELOW_FLOOR = setOf(
            "dark badge/Danger",
            "dark badge/Danger@nested",
            "dark button/Danger",
        )

        /**
         * WCAG AA for large text is 3:1. Deliberately not 4.5 -- muted captions on a card are
         * legitimately close to that, and a gate nobody can satisfy gets deleted. 3:1 still
         * catches every same-colour-on-same-colour case, which is the actual defect.
         */
        const val MIN_RATIO = 3f
    }
}
