package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Easing
import io.github.ronjunevaldoz.awake.ui.api.EaseOut
import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.toPx

enum class PanelEdge { Top, Right, Bottom, Left }

data class SlidePanelProperties(
    val dismissOnClickOutside: Boolean = true,
    val scrimColor: Color? = null,
    val surface: SurfaceStyle = SurfaceStyle(),
    val durationMs: Float = 250f,
    val easing: Easing = EaseOut,
)

fun UiScope.slidePanel(
    id: String,
    expanded: Boolean,
    edge: PanelEdge,
    size: Dp,
    properties: SlidePanelProperties = SlidePanelProperties(),
    content: ColumnScope.(UiBounds) -> Unit,
): UiPopupResult {
    if (!expanded) return UiPopupResult(null, false)
    val progress = animateFloatTween("$id.slide", 1f, 0f, properties.durationMs, properties.easing)
    properties.scrimColor?.let { overlayScrim(frameBounds(), it) }
    val result = popup(
        id = id,
        anchorSlot = UiBounds(0f, 0f, 0f, 0f),
        expanded = true,
        positionProvider = panelPosition(edge, size.toPx(), progress),
        properties = UiPopupProperties(dismissOnClickOutside = properties.dismissOnClickOutside, clippingEnabled = false),
    ) { slot ->
        surface(
            id = id,
            modifier = if (edge == PanelEdge.Left || edge == PanelEdge.Right) Modifier.fillMaxHeight() else Modifier.height(size),
            style = properties.surface,
            clipContent = true,
            content = content,
        )
    }
    return result
}

private fun panelPosition(edge: PanelEdge, size: Float, progress: Float) = UiPopupPositionProvider { _, frame, _ ->
    val offset = (1f - progress) * size
    when (edge) {
        PanelEdge.Left -> UiBounds(-offset, 0f, size, frame.height)
        PanelEdge.Right -> UiBounds(frame.width - size + offset, 0f, size, frame.height)
        PanelEdge.Top -> UiBounds(0f, -offset, frame.width, size)
        PanelEdge.Bottom -> UiBounds(0f, frame.height - size + offset, frame.width, size)
    }
}
