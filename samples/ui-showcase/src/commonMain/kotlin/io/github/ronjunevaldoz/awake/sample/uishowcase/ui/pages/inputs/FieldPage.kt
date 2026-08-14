// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldLegend
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSet
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldTextField
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val FieldPage = ShowcasePage(
    id = "field",
    title = "Field",
    category = ShowcaseCategory.Inputs,
    description = "Label, description, validation, and control composed as one form field primitive.",
    usageCode = """shadcnFieldSet { shadcnFieldLegend("Billing"); shadcnFieldGroup { ... } }""",
    referenceExample = "registry/new-york-v4/examples/field-demo.tsx",
    previewHeight = 620,
    notes = listOf("FieldSet/FieldLegend/FieldGroup own the spacing rhythm; controls stay unaware of it."),
    hero = {
        var name by rememberStateValue("ui-showcase-field", "name") { "" }
        var email by rememberStateValue("ui-showcase-field", "email") { "" }
        var plan by rememberStateValue("ui-showcase-field", "plan") { 0 }
        var receipts by rememberStateValue("ui-showcase-field", "receipts") { true }

        shadcnFieldSet(id = "showcase-fieldset") {
            shadcnFieldLegend("Billing details")
            shadcnFieldGroup(id = "showcase-fieldgroup-contact") {
                name = shadcnFieldTextField(
                    id = "showcase-field-name",
                    label = "Name on card",
                    value = name,
                    placeholder = "Jane Doe",
                )
                email = shadcnFieldTextField(
                    id = "showcase-field-email",
                    label = "Billing email",
                    value = email,
                    placeholder = "jane@example.com",
                )
            }
            shadcnFieldSeparator(label = "Plan")
            shadcnFieldGroup(id = "showcase-fieldgroup-plan") {
                plan = shadcnFieldDropdown(
                    id = "showcase-field-plan",
                    label = "Subscription",
                    options = listOf("Starter", "Pro", "Enterprise"),
                    selectedIndex = plan,
                ) ?: plan
                receipts = shadcnFieldSwitch(
                    id = "showcase-field-receipts",
                    label = "Email me receipts",
                    checked = receipts,
                )
            }
        }
    },
)
