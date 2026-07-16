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
| [overview]*        | Active section body                                |
| [buttons ]         | buttons / controls / MVI / popup sample            |
| [controls]         | depending on selected menu item                    |
| [counter ]         |                                                    |
| [popups  ]         |                                                    |
| [notes   ]         |                                                    |
+--------------------+----------------------------------------------------+
Legend: * = active
```

## Compact variant

```text
+--------------------------------------------------------------+
| Showcase Sidebar                                             |
| [overview]* [buttons] [controls] [counter] [popups] [notes] |
+--------------------------------------------------------------+
| Active section body                                          |
| component proof / controls / popup sample                    |
+--------------------------------------------------------------+
```

---

## Interaction notes

- Section buttons update the detail content in place instead of expanding a long stacked inspector.
- Compact width keeps the same sidebar content, but stacks it above the active section body.
- Sidebar and content use different authored surface treatments so the page reads like a designed sample, not one flat neutral panel.
