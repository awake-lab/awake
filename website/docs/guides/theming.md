# Theming

Awake uses a custom design system based on shadcn/ui principles, adapted for high-performance game HUDs.

## UI Tokens

The theme is driven by `UiColorTokens`. You can customize the look of your UI by providing a custom `UiTheme`.

```kotlin
val CustomTheme = UiTheme(
    tokens = UiColorTokens(
        primary = Color.fromHex("#3b82f6"),
        background = Color.fromHex("#020617"),
        // ...
    )
)
```

## Applying a Theme

Apply the theme in your `gameUi` definition:

```kotlin
gameUi {
    theme(CustomTheme)
    overlay {
        // Your UI components
    }
}
```
