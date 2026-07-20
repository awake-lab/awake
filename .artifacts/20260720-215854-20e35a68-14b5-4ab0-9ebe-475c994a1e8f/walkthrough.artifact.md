# Walkthrough: UI DSL Cleanup and Scope Flattening

Cleaned up the Awake UI DSL by removing redundant wrapper classes and moving methods to core scope extensions, resolving the "DSL trap" complexity.

## Key Changes

### `engine/ui-core`
- **[UiScope.kt](file:///Users/ronvaldoz/StudioProjects/awaken/awake/engine/ui-core/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/UiScope.kt)**: Applied `@AwakeUiDsl` to the core interface to ensure DSL marker propagation without wrapper overhead.
- **[Layout.kt](file:///Users/ronvaldoz/StudioProjects/awaken/awake/engine/ui-core/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/Layout.kt)**: Updated `AbsoluteScope` to fallback to a finite width (4096px) instead of 0px when `FillMax` is used, fixing text wrapping issues in unconstrained scopes.

### `engine/ui-dsl`
- **[UiDslLayout.kt](file:///Users/ronvaldoz/StudioProjects/awaken/awake/engine/ui-dsl/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/UiDslLayout.kt)**: Deleted `UiColumnDslScope`, `UiRowDslScope`, etc. Replaced them with extensions on `ColumnScope`, `RowScope`, etc.
- **[UiDslControls.kt](file:///Users/ronvaldoz/StudioProjects/awaken/awake/engine/ui-dsl/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/UiDslControls.kt)**: Deleted `UiDslScope`. Moved DSL widgets (text, button, etc.) to direct extensions on `UiScope`.
- **[AnimationDsl.kt](file:///Users/ronvaldoz/StudioProjects/awaken/awake/engine/ui-dsl/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/AnimationDsl.kt)**: Fixed `animatedHeight` to correctly measure and render its content using the core scopes.
- **[UiPropertyDsl.kt](file:///Users/ronvaldoz/StudioProjects/awaken/awake/engine/ui-dsl/src/commonMain/kotlin/io/github/ronjunevaldoz/awake/ui/UiPropertyDsl.kt)**: Updated `propertyRow` to use `BoxScope` for its children, providing correct `FillMax` resolution for property controls.

### `engine/ui-designsystem`
- Updated all Shadcn components (Badges, Buttons, Fields, etc.) to use the new flattened DSL extensions.
- Standardized `textScale` and `textSize` propagation in composite components.

## Verification Summary

### Automated Tests
- **UI DSL Tests**: All 28 tests in `:awake:engine:ui-dsl` passing on Desktop.
- **Cross-Platform**: 27 tests passing on iOS and Android host environments.
- **Starter Game**: 7 integration tests passing across Desktop, iOS, and WasmJs.
- **UI Showcase**: Signature fingerprints updated to match new layout properties.

### Manual Verification
- Verified that `ui { ... }` blocks now provide direct access to `ColumnScope` methods without redundant wrapping.
- Confirmed that `propertyRow` controls correctly fill their allotted space.
