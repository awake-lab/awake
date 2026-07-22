# Button

The `awakeShadcnButton` component provides a versatile slot-based button.

## Usage

```kotlin
awakeShadcnButton(
    onClick = { println("Clicked!") },
    variant = AwakeShadcnButtonVariant.Primary
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
