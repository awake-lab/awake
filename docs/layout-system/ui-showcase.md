---
screen: ui-showcase
pattern: A
slots: [sidebar, main]
grid: {compact: [main], medium: [sidebar, main], expanded: [sidebar, main]}
weights: {sidebar: fixed, main: 1f}
---

# ui-showcase

## Components

| Component | Width | Visible | Notes |
|---|---|---|---|
| Showcase sidebar | 264 dp | medium + expanded | Dedicated section navigation and module notes |
| Showcase content rail | flex 1 | all sizes | One active section at a time for component proofing |
| Compact showcase sidebar | full width | compact | Sidebar content stacks above the active section |

---

## Default

```text
+--------------------+----------------------------------------------------+
| Showcase Sidebar   | Showcase Content                                   |
| 264 dp             | flex 1                                             |
+--------------------+----------------------------------------------------+
| [badge] SHADCN     | Buttons And Badges                                 |
| Awake UI Showcase  | Supporting copy                                    |
|--------------------|----------------------------------------------------|
| v Getting Started  | Hero sample (interactive)                          |
|   Introduction*    |                                                    |
|   Theming          | Variants  (static matrix, when declared)           |
| v Inputs           |                                                    |
| v Layout           | States    (static matrix, when declared)           |
| v Overlays         |                                                    |
| v Status           | Notes                                              |
| v Typography       |                                                    |
| v Blocks           |                                                    |
+--------------------+----------------------------------------------------+
Legend: * = active
```

## Compact variant

```text
+--------------------------------------------------------------+
| Showcase Sidebar                                             |
| [Introduction]* [Theming] [Button] [Badge] [Text Field] ... |
+--------------------------------------------------------------+
| Active section body                                          |
| component proof / controls / popup sample                    |
+--------------------------------------------------------------+
```

---

## Page structure

Every page renders up to three sections, in this order:

| Section | Source | Interactive |
|---|---|---|
| Hero | port of the page's `referenceExample` under `third_party/shadcn-ui-ref/apps/v4/` | yes |
| Variants | generated from the component's own variant enum | no |
| States | size/enabled/value ladders | no |

A page declaring neither matrix renders the hero alone. Exhaustive interaction coverage lives
in `:awake:ui:designsystem` tests, not in extra pages.

Pages in the **Blocks** category are registered placeholders for shadcn components Awake has
not built yet. They render a "not implemented" empty state naming the missing primitive and its
reference path, so a gap stays visible in the catalog instead of being absent from it.

## Interaction notes

- Section buttons update the detail content in place instead of expanding a long stacked inspector.
- Compact width keeps the same sidebar content, but stacks it above the active section body.
- Sidebar and content use different authored surface treatments so the page reads like a designed sample, not one flat neutral panel.
