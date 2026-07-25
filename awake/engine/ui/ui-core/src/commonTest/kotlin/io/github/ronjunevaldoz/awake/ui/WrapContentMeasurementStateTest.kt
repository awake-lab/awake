package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*

/**
 * Regression test for the real bug report: a `verticalScroll` column wrapping a WrapContent
 * `surface` whose content branches on persisted (`rememberStateValue`) widget state -- e.g. a
 * "currently selected page" -- gets the wrong WrapContent height once state diverges from its
 * default/initial value. Root cause: [UiContext]'s internal WrapContent/scroll trial-measurement
 * pass builds a brand-new [io.github.ronjunevaldoz.awake.ui.context.UiContext] with its own
 * fresh state store instead of reading the real (source) context's persisted state, so any
 * stateful branch inside WrapContent content measures against its *default* value, not its
 * actual current value.
 */
class WrapContentMeasurementStateTest {
    @Test
    fun wrapContentSurfaceSizesAgainstRealPersistedStateNotDefault() {
        val ui = UiContext()
        ui.beginFrame(920f, 620f, testSnapshot())

        // content() below mirrors drawUiShowcasePageContent(): rememberStateValue() is read
        // *inside* the content passed to the WrapContent surface itself, not the outer scroll
        // column -- this is the exact nesting depth where the bug lives, since surface()'s own
        // internal unbounded trial re-executes this same content with a brand-new state store.
        val content: io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope.(UiSlot) -> Unit = {
            val mode by rememberStateValue("page-mode", "value") { "short" }
            if (mode == "short") {
                surface(id = "row-0", modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(36f.px))) { }
            } else {
                repeat(20) { index ->
                    surface(id = "row-$index", modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(36f.px))) { }
                }
            }
        }

        // First frame: establish persisted state as "short" (the default).
        var slotShort: UiSlot? = null
        ui.createBox(x = 0f, y = 0f, width = 920f, height = 620f).column(
            id = "content-viewport",
            modifier = (Modifier.verticalScroll(UiScrollState())).width(Dimension.FillMax).height(Dimension.FillMax)) {
            slotShort = surface(
                id = "content", modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent), content = content)
        }
        ui.endFrame()

        // Simulate a click handler committing new state on a *prior* frame (as real widgets
        // do), so this frame's composition only ever *reads* the persisted value -- exactly
        // like `drawUiShowcasePageContent`'s `var selectedPage by rememberStateValue(...)`
        // being flipped by a sidebar button's `onSelect` callback before the next frame draws.
        ui.rememberStateValue<String>("page-mode", "value") { "short" }.value = "long"

        // Second frame: real app-driven state change -- user navigates to the "long" page.
        ui.beginFrame(920f, 620f, testSnapshot())
        var slotLong: UiSlot? = null
        ui.createBox(x = 0f, y = 0f, width = 920f, height = 620f).column(
            id = "content-viewport",
            modifier = (Modifier.verticalScroll(UiScrollState())).width(Dimension.FillMax).height(Dimension.FillMax)) {
            slotLong = surface(
                id = "content", modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent), content = content)
        }
        ui.endFrame()

        assertTrue(
            (slotLong?.height ?: 0f) > (slotShort?.height ?: 0f) * 5f,
            "WrapContent surface must size against the real 'long' page content (20 rows) " +
                "once state changes, not the default 'short' page (1 row) the trial " +
                "measurement pass fell back to -- short=${slotShort?.height} long=${slotLong?.height}"
        )
    }
}
