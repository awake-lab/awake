# Button

The `shadcnButton` component provides a versatile slot-based button.

## Usage

```kotlin
shadcnButton(
    onClick = { println("Clicked!") },
    variant = ShadcnButtonVariant.Primary
) {
    text("Click Me")
}
```

## Variants

Available variants include:
- `Primary`
- `Secondary`
- `Outline`
- `Ghost`
- `Destructive`
- `Link`
