// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.drawDropdownTriggerContent
import io.github.ronjunevaldoz.awake.ui.unstyled.input.progressBar
import io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.checkbox
import io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.switch
import io.github.ronjunevaldoz.awake.ui.unstyled.input.slider
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.textField
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.textarea
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggleGroup
import io.github.ronjunevaldoz.awake.ui.unstyled.skeleton
import io.github.ronjunevaldoz.awake.ui.unstyled.spinner
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private fun shadcnFieldStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.field(theme.asShadcnTheme()) then style

private fun shadcnFieldStyle(
    theme: UiTheme,
    variant: ShadcnTextFieldVariant,
    style: Style
): Style =
    ShadcnStyles.field(theme.asShadcnTheme(), variant) then style

private fun shadcnCheckboxStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.checkbox(theme.asShadcnTheme()) then style

// Real shadcn's RadioGroup item is a circular checkbox.checkbox() -- same box/inset-dot
// mechanics, just a Circle shapeSpec instead of a rounded square. No separate ui-unstyled
// primitive needed for that alone.
private fun shadcnRadioStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.checkbox(theme.asShadcnTheme()) then Style { shape(UiShapeSpec.Circle) } then style

private fun shadcnSliderStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.slider(theme.asShadcnTheme()) then style

fun UiScope.shadcnSwitch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Boolean = switch(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = shadcnFieldStyle(theme, style)
)

fun UiScope.shadcnToggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {}
): Boolean = toggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = style,
    enabled = enabled,
    onCheckedChange = onCheckedChange
)

fun UiScope.shadcnCheckbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Boolean = checkbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = shadcnCheckboxStyle(theme, style)
)

// Single-select among [options] -- clicking an unselected item selects it; clicking the
// already-selected item is a no-op (checkbox()'s own toggle-off return is discarded), since
// a real radio group has no way to end up with nothing selected once one item is chosen.
fun ColumnScope.shadcnRadioGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    gap: Dp = 8f.dp,
    style: Style = Style.Empty
): Int {
    var resolved = selectedIndex
    val radioStyle = shadcnRadioStyle(theme, style)
    options.forEachIndexed { index, label ->
        val clicked = checkbox(
            id = "$id.$index",
            checked = index == selectedIndex,
            label = label,
            modifier = modifier.height(24f.dp),
            style = radioStyle,
            boxSize = 16f.dp
        )
        if (clicked) resolved = index
        if (index != options.lastIndex) spacer(Modifier.height(gap))
    }
    return resolved
}

fun UiScope.shadcnProgress(
    id: String,
    value: Float,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Unit = progressBar(
    id = id,
    value = value,
    modifier = modifier,
    style = shadcnSliderStyle(theme, style)
)

private fun shadcnSkeletonStyle(theme: UiTheme, style: Style): Style {
    val shadcnTheme = theme.asShadcnTheme()
    return Style {
        background(shadcnTheme.palette.muted)
        shape(shadcnTheme.radii.md)
    } then style
}

fun UiScope.shadcnSkeleton(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Unit = skeleton(id = id, modifier = modifier, style = shadcnSkeletonStyle(theme, style))

private fun shadcnSpinnerStyle(theme: UiTheme, style: Style): Style {
    val shadcnTheme = theme.asShadcnTheme()
    return Style { foreground(shadcnTheme.tokens.mutedForeground) } then style
}

fun UiScope.shadcnSpinner(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Unit = spinner(id = id, modifier = modifier, style = shadcnSpinnerStyle(theme, style))

// Real shadcn's TabsList is a muted rounded track; the active TabsTrigger gets a raised
// card-colored background, inactive ones are chromeless labels. Composed from
// shadcnButton the same way shadcnRadioGroup composes from checkbox(): reuse the
// existing variant/style system rather than a new low-level widget.
fun ColumnScope.shadcnTabs(
    id: String,
    tabs: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    tabWidth: Dp = 96f.dp,
    height: Dp = 32f.dp
): Int {
    var resolved = selectedIndex
    val shadcnTheme = theme.asShadcnTheme()
    surface(
        id = "$id.track",
        modifier = (modifier).copy(
            widthDimension = modifier.widthDimension ?: Dimension.WrapContent,
            heightDimension = Dimension.Fixed(height)
        ),
        style = Style {
            shape(shadcnTheme.radii.md)
            background(shadcnTheme.palette.muted)
        }
    ) {
        row(
            horizontalArrangement = Arrangement.spacedBy(2f.dp),
            modifier = Modifier.height(height)
        ) {
            tabs.forEachIndexed { index, label ->
                val active = index == selectedIndex
                val tabStyle: Style = if (active) {
                    Style {
                        background(shadcnTheme.card)
                        foreground(shadcnTheme.tokens.foreground)
                    }
                } else {
                    Style { foreground(shadcnTheme.tokens.mutedForeground) }
                }
                val tabHeight: Dp = (height.value - 4f).dp
                val tabModifier = UiModifier(
                    widthDimension = Dimension.Fixed(tabWidth),
                    heightDimension = Dimension.Fixed(tabHeight)
                )
                // UiButtonVariant.Ghost's resolveFill hardcodes fill to transparent unless
                // hovered/active, ignoring any style override -- so the active tab (which must
                // show its card-colored background at rest, not just on hover) uses Primary
                // (-> UiButtonVariant.Filled, which always honors the resolved background)
                // with that background/foreground overridden by tabStyle; inactive tabs stay
                // Ghost for the real chromeless-until-hover look.
                val clicked = shadcnButton(
                    id = "$id.$index",
                    label = label,
                    modifier = tabModifier,
                    variant = if (active) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
                    style = tabStyle
                )
                if (clicked) resolved = index
            }
        }
    }
    return resolved
}

fun UiScope.shadcnInput(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false
): String = textField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    style = shadcnFieldStyle(theme, variant, style),
    enabled = enabled,
    isError = isError
)

fun UiScope.shadcnTextarea(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false,
    minLines: Int = 3
): String = textarea(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    style = shadcnFieldStyle(theme, variant, style),
    enabled = enabled,
    isError = isError,
    minLines = minLines
)

fun UiScope.shadcnSelect(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Int? {
    val popupState = rememberPopupState(id, key = "expanded")
    val triggerStyle = shadcnFieldStyle(theme, style)
    val trigger = buttonSlot(
        id = "$id.trigger",
        modifier = modifier.height(40f.dp),
        style = triggerStyle
    ) { }
    if (trigger.clicked) {
        popupState.toggle()
    }
    val selectedLabel = options.getOrNull(selectedIndex) ?: ""
    drawDropdownTriggerContent(
        slot = trigger.slot,
        label = selectedLabel,
        expanded = popupState.expanded,
        style = triggerStyle,
        semanticId = "$id.label"
    )
    recordSemantic(
        role = UiSemanticRole.Dropdown,
        id = id,
        label = selectedLabel,
        bounds = trigger.slot,
        selected = popupState.expanded
    )

    val result = shadcnDropdownMenu(
        id = "$id.dropdown",
        anchorSlot = trigger.slot.toBounds(),
        expanded = popupState.expanded,
        items = options.map { UiDropdownMenuItem(label = it) },
        selectedIndex = selectedIndex,
        width = Dimension.Fixed(trigger.slot.width.px),
        itemHeight = 32f,
        positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
        style = popoverStyle(theme.asShadcnTheme()) then Style {
            contentPadding(4f.dp)
        }
    )
    if (result.dismissed || result.selectedIndex != null) {
        popupState.close()
    }
    return result.selectedIndex
}

fun UiScope.shadcnSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Float = slider(
    id = id,
    min = min,
    max = max,
    value = value,
    label = label,
    modifier = modifier,
    style = shadcnSliderStyle(theme, style)
)

fun UiScope.shadcnToggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    onIndexChange: (Int) -> Unit = {}
) = toggleGroup(id, options, selectedIndex, modifier, onIndexChange)
