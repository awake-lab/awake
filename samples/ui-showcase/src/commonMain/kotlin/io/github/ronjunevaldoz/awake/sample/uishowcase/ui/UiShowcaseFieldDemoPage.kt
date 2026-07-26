// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.ShadcnFieldOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldDescription
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLegend
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldSet
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.toPx

// First entry of each list doubles as the unselected placeholder ("MM"/"YYYY"), matching the
// reference screenshot without needing a dedicated placeholder concept on shadcnSelect.
private val CheckoutMonthOptions = listOf("MM") + (1..12).map { it.toString().padStart(2, '0') }
private val CheckoutYearOptions = listOf("YYYY") + (2026..2035).map { it.toString() }

/**
 * A real-world composed pattern: shadcn/ui's web "Payment Method" checkout form, rebuilt 1:1 from
 * shadcnFieldSet/shadcnFieldLegend/shadcnFieldGroup/shadcnField and friends. Demonstrates the new
 * FieldSet/FieldLegend pair -- everything else in this page already existed.
 */
internal fun ColumnScope.drawUiShowcaseFieldDemoPreview(state: UiShowcaseRuntimeState) {
    var nameOnCard by context.rememberStateValue("ui-showcase-field-demo", "nameOnCard") { "" }
    var cardNumber by context.rememberStateValue("ui-showcase-field-demo", "cardNumber") { "" }
    var monthIndex by context.rememberStateValue("ui-showcase-field-demo", "month") { 0 }
    var yearIndex by context.rememberStateValue("ui-showcase-field-demo", "year") { 0 }
    var cvv by context.rememberStateValue("ui-showcase-field-demo", "cvv") { "" }
    var sameAsShipping by context.rememberStateValue("ui-showcase-field-demo", "sameAsShipping") { true }
    var comments by context.rememberStateValue("ui-showcase-field-demo", "comments") { "" }

    shadcnFieldGroup {
        shadcnFieldSet(id = "checkout-payment") {
            shadcnFieldLegend("Payment Method")
            shadcnFieldDescription("All transactions are secure and encrypted")
            shadcnFieldGroup {
                shadcnField(id = "checkout-name-on-card") {
                    shadcnFieldLabel("Name on Card")
                    nameOnCard = shadcnInput(
                        id = "checkout-name-on-card-input",
                        value = nameOnCard,
                        placeholder = "Evil Rabbit"
                    )
                }
                shadcnField(id = "checkout-card-number") {
                    shadcnFieldLabel("Card Number")
                    cardNumber = shadcnInput(
                        id = "checkout-card-number-input",
                        value = cardNumber,
                        placeholder = "1234 5678 9012 3456"
                    )
                    shadcnFieldDescription("Enter your 16-digit card number")
                }
                // CSS grid-cols-3 gap-4 equivalent. row()'s own weight() modifier only settles
                // through a WrapContent trial-measure pass that this framework's weighted-row
                // resolution doesn't yet handle cleanly for nested shadcnField columns (see
                // docs/reference/ui-ownership.md -- row/column measurement internals are
                // ui-core, not ui-designsystem, territory) -- so this splits the row's own
                // already-known [width] into three fixed-width columns directly instead.
                val gridGapPx = 16f.dp.toPx()
                val gridColumnWidthPx = (width - gridGapPx * 2) / 3f
                row(horizontalArrangement = Arrangement.spacedBy(16f.dp), modifier = Modifier.height(76f.dp)) {
                    column(modifier = Modifier.width(gridColumnWidthPx.px)) {
                        shadcnField(id = "checkout-month") {
                            shadcnFieldLabel("Month")
                            monthIndex = shadcnSelect(
                                id = "checkout-month-select",
                                options = CheckoutMonthOptions,
                                selectedIndex = monthIndex
                            ) ?: monthIndex
                        }
                    }
                    column(modifier = Modifier.width(gridColumnWidthPx.px)) {
                        shadcnField(id = "checkout-year") {
                            shadcnFieldLabel("Year")
                            yearIndex = shadcnSelect(
                                id = "checkout-year-select",
                                options = CheckoutYearOptions,
                                selectedIndex = yearIndex
                            ) ?: yearIndex
                        }
                    }
                    column(modifier = Modifier.width(gridColumnWidthPx.px)) {
                        shadcnField(id = "checkout-cvv") {
                            shadcnFieldLabel("CVV")
                            cvv = shadcnInput(
                                id = "checkout-cvv-input",
                                value = cvv,
                                placeholder = "123"
                            )
                        }
                    }
                }
            }
        }
        shadcnFieldSeparator()
        shadcnFieldSet(id = "checkout-billing") {
            shadcnFieldLegend("Billing Address")
            shadcnFieldDescription("The billing address associated with your payment method")
            shadcnFieldGroup {
                shadcnField(id = "checkout-same-as-shipping", orientation = ShadcnFieldOrientation.Horizontal) {
                    sameAsShipping = shadcnCheckbox(
                        id = "checkout-same-as-shipping-checkbox",
                        checked = sameAsShipping,
                        modifier = Modifier.width(20f.dp).height(20f.dp)
                    )
                    shadcnFieldLabel("Same as shipping address")
                }
            }
        }
        shadcnFieldSet(id = "checkout-comments") {
            shadcnFieldGroup {
                shadcnField(id = "checkout-comments-field") {
                    shadcnFieldLabel("Comments")
                    comments = shadcnTextarea(
                        id = "checkout-comments-textarea",
                        value = comments,
                        placeholder = "Add any additional comments",
                        minLines = 4
                    )
                }
            }
        }
        shadcnField(id = "checkout-actions", orientation = ShadcnFieldOrientation.Horizontal) {
            shadcnButton(
                id = "checkout-submit",
                label = "Submit",
                variant = ShadcnButtonVariant.Primary,
                modifier = Modifier.width(100f.dp).height(36f.dp)
            )
            shadcnButton(
                id = "checkout-cancel",
                label = "Cancel",
                variant = ShadcnButtonVariant.Outline,
                modifier = Modifier.width(100f.dp).height(36f.dp)
            )
        }
    }
}
