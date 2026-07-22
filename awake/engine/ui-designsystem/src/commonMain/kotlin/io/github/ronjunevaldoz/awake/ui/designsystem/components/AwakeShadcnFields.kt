// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.dropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.theme
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

private fun awakeShadcnFieldStyle(theme: UiTheme, style: Style): Style =
    AwakeShadcnStyles.field(theme.asAwakeShadcnTheme()) then style

private fun awakeShadcnFieldStyle(theme: UiTheme, variant: AwakeShadcnTextFieldVariant, style: Style): Style =
    AwakeShadcnStyles.field(theme.asAwakeShadcnTheme(), variant) then style

private fun awakeShadcnCheckboxStyle(theme: UiTheme, style: Style): Style =
    AwakeShadcnStyles.checkbox(theme.asAwakeShadcnTheme()) then style

// Real shadcn's RadioGroup item is a circular checkbox.checkbox() -- same box/inset-dot
// mechanics, just a Circle shapeSpec instead of a rounded square. No separate ui-unstyled
// primitive needed for that alone.
private fun awakeShadcnRadioStyle(theme: UiTheme, style: Style): Style =
    AwakeShadcnStyles.checkbox(theme.asAwakeShadcnTheme()) then Style { shape(UiShapeSpec.Circle) } then style

private fun awakeShadcnSliderStyle(theme: UiTheme, style: Style): Style =
    AwakeShadcnStyles.slider(theme.asAwakeShadcnTheme()) then style

fun UiScope.awakeShadcnSwitch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = switch(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = awakeShadcnFieldStyle(theme, style)
)

fun UiScope.awakeShadcnToggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
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

fun UiScope.awakeShadcnCheckbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = checkbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = awakeShadcnCheckboxStyle(theme, style)
)

// Single-select among [options] -- clicking an unselected item selects it; clicking the
// already-selected item is a no-op (checkbox()'s own toggle-off return is discarded), since
// a real radio group has no way to end up with nothing selected once one item is chosen.
fun ColumnScope.awakeShadcnRadioGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    gap: Dp = 8f.dp,
    style: Style = Style.Empty
): Int {
    var resolved = selectedIndex
    val radioStyle = awakeShadcnRadioStyle(theme, style)
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
        if (index != options.lastIndex) spacer(UiModifier().height(gap))
    }
    return resolved
}

fun UiScope.awakeShadcnProgress(
    id: String,
    value: Float,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Unit = progressBar(
    id = id,
    value = value,
    modifier = modifier,
    style = awakeShadcnSliderStyle(theme, style)
)

private fun awakeShadcnSkeletonStyle(theme: UiTheme, style: Style): Style {
    val shadcnTheme = theme.asAwakeShadcnTheme()
    return Style {
        background(shadcnTheme.palette.muted)
        shape(shadcnTheme.radii.md)
    } then style
}

fun UiScope.awakeShadcnSkeleton(
    id: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Unit = skeleton(id = id, modifier = modifier, style = awakeShadcnSkeletonStyle(theme, style))

private fun awakeShadcnSpinnerStyle(theme: UiTheme, style: Style): Style {
    val shadcnTheme = theme.asAwakeShadcnTheme()
    return Style { foreground(shadcnTheme.tokens.mutedForeground) } then style
}

fun UiScope.awakeShadcnSpinner(
    id: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Unit = spinner(id = id, modifier = modifier, style = awakeShadcnSpinnerStyle(theme, style))

// Real shadcn's TabsList is a muted rounded track; the active TabsTrigger gets a raised
// card-colored background, inactive ones are chromeless labels. Composed from
// awakeShadcnButton the same way awakeShadcnRadioGroup composes from checkbox(): reuse the
// existing variant/style system rather than a new low-level widget.
fun ColumnScope.awakeShadcnTabs(
    id: String,
    tabs: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    tabWidth: Dp = 96f.dp,
    height: Dp = 32f.dp
): Int {
    var resolved = selectedIndex
    val shadcnTheme = theme.asAwakeShadcnTheme()
    surface(
        id = "$id.track",
        width = modifier.width ?: Dimension.WrapContent,
        height = Dimension.Fixed(height),
        modifier = modifier,
        radius = shadcnTheme.radii.md,
        style = Style { background(shadcnTheme.palette.muted) }
    ) {
        row(height = height, horizontalArrangement = Arrangement.spacedBy(2f.dp)) {
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
                val tabModifier = UiModifier(width = Dimension.Fixed(tabWidth), height = Dimension.Fixed(tabHeight))
                // UiButtonVariant.Ghost's resolveFill hardcodes fill to transparent unless
                // hovered/active, ignoring any style override -- so the active tab (which must
                // show its card-colored background at rest, not just on hover) uses Primary
                // (-> UiButtonVariant.Filled, which always honors the resolved background)
                // with that background/foreground overridden by tabStyle; inactive tabs stay
                // Ghost for the real chromeless-until-hover look.
                val clicked = awakeShadcnButton(
                    id = "$id.$index",
                    label = label,
                    modifier = tabModifier,
                    variant = if (active) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Ghost,
                    style = tabStyle
                )
                if (clicked) resolved = index
            }
        }
    }
    return resolved
}

fun UiScope.awakeShadcnTextField(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnTextFieldVariant = AwakeShadcnTextFieldVariant.Default,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false
): String = textField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    style = awakeShadcnFieldStyle(theme, variant, style),
    enabled = enabled,
    isError = isError
)

fun UiScope.awakeShadcnTextarea(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnTextFieldVariant = AwakeShadcnTextFieldVariant.Default,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false,
    minLines: Int = 3
): String = textarea(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    style = awakeShadcnFieldStyle(theme, variant, style),
    enabled = enabled,
    isError = isError,
    minLines = minLines
)

fun UiScope.awakeShadcnDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Int? {
    val popupState = rememberPopupState(id, key = "expanded")
    val triggerStyle = awakeShadcnFieldStyle(theme, style)
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

    val result = dropdownMenu(
        id = "$id.dropdown",
        anchorSlot = trigger.slot,
        expanded = popupState.expanded,
        items = options.map { UiDropdownMenuItem(label = it) },
        selectedIndex = selectedIndex,
        width = Dimension.Fixed(trigger.slot.width.px),
        itemHeight = 32f,
        positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
        style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), AwakeShadcnSurfaceVariant.Popover) then Style {
            contentPadding(4f.dp)
        }
    )
    if (result.dismissed || result.selectedIndex != null) {
        popupState.close()
    }
    return result.selectedIndex
}

fun UiScope.awakeShadcnSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Float = slider(
    id = id,
    min = min,
    max = max,
    value = value,
    label = label,
    modifier = modifier,
    style = awakeShadcnSliderStyle(theme, style)
)

fun UiScope.awakeShadcnToggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    onIndexChange: (Int) -> Unit = {}
) = toggleGroup(id, options, selectedIndex, modifier, onIndexChange)
