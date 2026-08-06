# Awake UI Design System - Component Registry

Shared component registry for Awake UI Design System, mapping components to token slots, default metrics, and layout constraints.

## Component Registry Table

| Component Name | Role / Function | Token Slots | Default Size / Inset | Layout Rules |
| :--- | :--- | :--- | :--- | :--- |
| `shadcnButton` | Interactive Trigger / Action | `primary`, `secondary`, `destructive`, `background`, `border`, `ring` | Height: 40dp (Md), Padding: 16dp | Centered text slot, 6-8dp radius, hover/pressed state transitions |
| `shadcnCard` | Content Surface Container | `card`, `card-foreground`, `border` | Padding: 16dp (panelPadding), Corner: 12dp (xl) | Vertical column layout with optional header & footer dividers |
| `shadcnTabs` | Segmented Navigation Track | `muted`, `muted-foreground`, `card`, `foreground` | Track Height: 32dp, Inset: 4dp | Horizontal track with raised active tab highlight |
| `shadcnSelect` | Dropdown Picker Trigger | `background`, `input`, `ring`, `popover`, `border` | Trigger Height: 40dp | Popup anchoring, caret icon indicator, option item hover accent |
| `shadcnDrawer` | Slide-Over Modal Surface | `card`, `border`, `background` | Bottom/Top Height: 320dp | Full-width or fixed panel anchored to viewport edge with backdrop scrim |
| `shadcnTooltip` | Contextual Hover Popover | `card`, `border`, `primary`, `primary-foreground` | Inset: 8dp, Corner: 4dp (sm) | Aligned popup positioning relative to anchor slot bounds |
| `shadcnInput` | Form Text Input Field | `background`, `foreground`, `input`, `ring`, `muted` | Height: 40dp | Focus ring border highlight, placeholder text truncation |
| `shadcnBadge` | Status Indicator Pill | `primary`, `secondary`, `destructive`, `outline` | Height: 20dp, Padding: 8dp | Rounded full-pill shape, compact label text |
