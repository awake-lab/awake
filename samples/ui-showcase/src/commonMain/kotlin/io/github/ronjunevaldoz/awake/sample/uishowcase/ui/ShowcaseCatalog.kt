// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.blocks.BlockPlaceholderPages
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.gettingstarted.IntroductionPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.gettingstarted.ThemingPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.BadgePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.ButtonGroupPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.ButtonPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.CheckboxPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.ComboboxPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.FieldPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.InputGroupPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.InputOtpPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.RadioGroupPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.RangeSliderPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.SelectPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.SliderPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.SwitchPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.TextFieldPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.TextareaPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.ToggleGroupPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs.TogglePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.AccordionPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.BreadcrumbPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.CanvasPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.CardPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.CollapsibleCardPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.CollapsiblePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.ResizablePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.ScrollAreaPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.SeparatorPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.SidebarPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.SurfacePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.TablePage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout.TabsPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays.AlertDialogPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays.ContextMenuPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays.DialogPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays.DrawerPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays.DropdownMenuPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays.PopoverPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays.SheetPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.overlays.TooltipPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status.AlertPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status.AvatarPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status.EmptyPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status.KbdPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status.ProgressPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status.SkeletonPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status.SpinnerPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.status.ToastPage
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.typography.TypographyPage

/**
 * The single showcase catalog. The app renders it, the preview/layout-signature tests derive
 * their fixtures from it, and the parity manifest keys off [ShowcasePage.referenceExample].
 * Adding a page here is the only way to publish one -- there is no second, test-only list.
 */
internal val ShowcasePages: List<ShowcasePage> = listOf(
    IntroductionPage,
    ThemingPage,

    ButtonPage,
    ButtonGroupPage,
    BadgePage,
    TextFieldPage,
    TextareaPage,
    InputOtpPage,
    InputGroupPage,
    CheckboxPage,
    RadioGroupPage,
    SwitchPage,
    TogglePage,
    ToggleGroupPage,
    SliderPage,
    RangeSliderPage,
    SelectPage,
    ComboboxPage,
    FieldPage,

    CardPage,
    CollapsibleCardPage,
    TabsPage,
    AccordionPage,
    CollapsiblePage,
    BreadcrumbPage,
    SidebarPage,
    ResizablePage,
    TablePage,
    ScrollAreaPage,
    SeparatorPage,
    SurfacePage,
    CanvasPage,

    DialogPage,
    AlertDialogPage,
    DrawerPage,
    SheetPage,
    PopoverPage,
    DropdownMenuPage,
    ContextMenuPage,
    TooltipPage,

    AlertPage,
    AvatarPage,
    ProgressPage,
    SkeletonPage,
    SpinnerPage,
    ToastPage,
    KbdPage,
    EmptyPage,

    TypographyPage,
) + BlockPlaceholderPages

internal val ShowcasePagesByCategory: Map<ShowcaseCategory, List<ShowcasePage>> =
    ShowcasePages.groupBy { it.category }

/**
 * Returns null for an unknown id on purpose. The previous catalog silently substituted the
 * first page, which let five preview fixtures fingerprint the Introduction page while claiming
 * to cover Range Slider, State, Shimmer, and Field Demo.
 */
internal fun showcasePageOrNull(pageId: String): ShowcasePage? =
    ShowcasePages.firstOrNull { it.id == pageId }

internal fun showcasePageById(pageId: String): ShowcasePage =
    requireNotNull(showcasePageOrNull(pageId)) { "Unknown showcase page id: $pageId" }
