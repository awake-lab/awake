# UI Showcase Component Pages - Layout Specification

Layout specs and ASCII wireframe diagrams for the UI Showcase component pages (Buttons, Cards, Inputs, Navigation, Overlays).

## Showcase Page Structure

Each component showcase page mirrors the Figma canvas layout:
1. **Header Section**: Title, description badge, and token mapping badge.
2. **Hero Preview Card**: Interactive live component display with real-time property controls.
3. **Variant Matrix Grid**: 2x2 grid displaying Default, Secondary, Outline, and Destructive variants across Light & Dark themes.
4. **Token & Metric Inspector**: Token IDs, content padding, corner radius, and squircle smoothing values.

---

## ASCII Wireframe

```
+-----------------------------------------------------------------------+
|  [Header]  Component Showcase Title                                   |
|            Description & Token Slot Badges                            |
+-----------------------------------------------------------------------+
|  [Hero Card]                                                          |
|  +-----------------------------------------------------------------+  |
|  |  [Live Interactive Component Display]                           |  |
|  |  e.g. Primary Button / Card / Input / Tabs / Select             |  |
|  +-----------------------------------------------------------------+  |
|  | Controls: [Variant: Primary]  [Size: Md]  [Smoothing: 0.6]         |  |
+-----------------------------------------------------------------------+
|  [Variant Matrix Grid]                                                |
|  +-------------------------------+  +-------------------------------+  |
|  | Primary Variant               |  | Secondary Variant             |  |
|  +-------------------------------+  +-------------------------------+  |
|  +-------------------------------+  +-------------------------------+  |
|  | Outline Variant               |  | Destructive Variant           |  |
|  +-------------------------------+  +-------------------------------+  |
+-----------------------------------------------------------------------+
|  [Token & Metric Inspector]                                           |
|  Background Token: "card" | Border Token: "border" | Squircle: 0.6      |
+-----------------------------------------------------------------------+
```

---

## Component Layout Table

| Page Section | Component / Slot | Token Mapping | Gap / Padding |
| :--- | :--- | :--- | :--- |
| Header | Title & Badge | `foreground`, `muted-foreground` | Gap: 8dp |
| Hero Card | Interactive Live Preview | `card`, `border` | Padding: 20dp |
| Variant Matrix | 2x2 Variant Cards | `primary`, `secondary`, `destructive` | Gap: 16dp |
| Inspector Panel | Token & Metric Row | `muted`, `muted-foreground` | Padding: 12dp |
