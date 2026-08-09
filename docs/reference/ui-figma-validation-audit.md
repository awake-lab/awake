# UI Figma Validation Audit — Findings & Gaps

> **Status:** COMPLETED · **Date:** 2026-08-06
> This document audits the current UI testing, semantics, and rasterization infrastructure to identify blockers for automated validation against the **Shadcn Figma Design System**.

## References

*   **Figma Design System (January 2026)**: [Shadcn UI Components with Variables](https://www.figma.com/design/vGpl4nFKKAMpmjAjnpM3AC/shadcn-ui-components-with-variables---Tailwind-classes---Updated-January-2026--Community-?node-id=0-1&p=f&t=Q5vlJZMagPqVJHnJ-0)
*   **Targeted Specifications**: Sizing, Padding, Spacing, Color, Tokens, **Radii Scale, Shadow/Elevation, and Typography Metrics (Line-height/Tracking).**

## Prerequisites

Before starting Phase A, the following must be in place:
1.  **Figma API Token**: Access to the [Figma REST API](https://www.figma.com/developers/api) to pull variable/mode data.
2.  **JSON Design Schema**: A defined JSON contract that maps Figma Variable exports to Awake's `tokenId` expectations.
3.  **Headless Runtime**: The `ui-testing` module must be capable of running the CPU rasterizer in CI (already verified on Desktop JVM).

---

## 1. Style & Token Metadata Gap

Currently, the `UiContext` emits "dead" primitives. Once a component like a `shadcnButton` is rendered, its high-level design intent is lost and reduced to raw geometry and hex colors.

### Gaps in Primitives (`UiDrawPrimitive`)
*   **Missing Token IDs**: `Quad`, `RoundedQuad`, and `Glyph` lack a `tokenId` property. To validate against Figma, we must know that a specific `Quad` represents the `primary` background or the `input` border, rather than just its resulting `Color` value.
*   **Missing Shadow Support**: There is no primitive for drop-shadows or inner-shadows. Figma specifies shadows via offset, blur, and spread; we cannot currently represent or validate these.
*   **Loss of Context**: We cannot currently distinguish between a background fill and a border-stroke primitive once they reach the renderer/rasterizer.

### Gaps in Semantics (`UiSemanticNode`)
*   **Missing Visual Properties**: `UiSemanticNode` captures boundaries and basic roles (Button, Text), but lacks visual metadata such as `backgroundColor`, `borderRadius`, `strokeWidth`, or `textStyleToken`.
*   **Validation Limitation**: We can validate *where* a widget is, but not *what it looks like* without falling back to fragile and slow pixel-diffing.

## 2. Validation Logic Gaps

The `validateAwakeUiPreview` function and its sibling inspectors in `ui-testing` are built for **Safety** (collision detection, text truncation) rather than **Design Fidelity**.

### Gap in Inspector Logic
*   **Minimum vs. Exact Padding**: Current inspectors (`inspectPadding`) only check for **minimum** values. Figma validation requires **exact** matches (e.g., padding must be exactly `16px`, not just `> 8px`).
*   **Absolute Sizing**: There is no current validator for absolute dimension constraints (e.g., "Standard Button must be exactly 40px tall").
*   **Composition Integrity**: We lack a way to validate the "internal anatomy" of a component—for example, asserting that a `shadcnButton` always contains a `Label` that is perfectly centered and uses the `primary-foreground` color token.

## 3. Figma Variable & Mode Integration

Figma's modern variables support different **Modes** (e.g., Compact vs. Expanded, Light vs. Dark, Default vs. High-Density).

### Gaps in Multi-Mode Reporting
*   **Variable Mode Matrix**: While we have `componentStateMatrix` for interaction states (Hover, Active), we don't have a **Variable Mode Matrix** that automatically runs the same test suite across different density or radius scales defined in Figma.
*   **Token Mapping Layer**: There is no mapping bridge between Figma Variable Names (e.g., `--primary`) and our internal `ShadcnPalette` properties.

---

## Proposed Action Plan

To enable automated Figma validation, the following architectural changes are required:

### Phase A: Metadata Enrichment (Core) — **CRITICAL**
1.  **Enrich Primitives**: Add `val tokenId: String?` to `UiDrawPrimitive` and all its subtypes.
2.  **Shadow Primitives**: Add `ShadowQuad` or `DropShadow` primitives to support Figma's elevation specs.
3.  **Enrich Semantics**: Add `backgroundColor`, `borderRadius`, `shadowToken`, and `textStyleToken` to `UiSemanticNode`.
4.  **Thread Tokens**: Update `Style.kt` and `UiContext` to propagate the `tokenId` from the design tokens down to the emitted primitives.

### Phase B: Fidelity Inspectors (Testing) — **HIGH**
1.  **Exact Matchers**: Implement `inspectExactPadding` and `inspectExactSpacing` in `UiSemanticInspection.kt`.
2.  **Dimension Assertions**: Add `minWidth`/`maxWidth`/`exactHeight` constraints to `AwakeUiPreviewValidationConfig`.
3.  **Token Assertions**: Create a `tokenAssertions` rule set to verify that components are using the correct semantic variables (e.g., "Button text must use the `primary-foreground` token").
4.  **a11y Compliance**: Automate contrast and touch-target checks.

### Phase C: Structured Reporting (Tooling) — **MEDIUM**
1.  **Design Data Export**: Update `UiPreviewReportTask` to output a structured JSON "Design Report" (per component) alongside the HTML gallery.
2.  **Figma Comparison**: Create a script to compare the generated "Design Report" against a JSON export from Figma (e.g., via the Figma REST API or a plugin).

---

## Architectural Layering

To prevent tight coupling between the engine core and any specific design system, the following layering must be strictly enforced:

### 1. Foundation (`ui-core`)
**Ownership:** Engine Core.
*   **Role**: Provides the "pipes" for metadata.
*   **Agnosticism**: Stays 100% design-system agnostic. It carries `tokenId` as an opaque `String?`.
*   **Coordinate System**: Exclusively uses **Dp (Density-independent Pixels)** for all public APIs (`Style`, `UiBounds`). Physical `px` are used only for internal computation and final rasterization.
*   **Constraint**: It never defines tokens like "primary" or "muted". It only knows that a primitive *can* have an origin ID.

### 2. Tooling (`ui-testing`)
**Ownership:** Engine Tooling.
*   **Role**: Provides the "measuring tape".
*   **Agnosticism**: Stays design-system agnostic. It implements math-heavy inspectors like `inspectExactPadding` or `inspectSemanticOverlaps`.
*   **Constraint**: It validates numerical relationships and string matches but doesn't know the business logic behind a specific token choice.

### 3. Design System (`ui-designsystem` / Shadcn)
**Ownership:** Branded Layer.
*   **Role**: Defines the "design intent".
*   **Knowledge**: Owns the mapping between Figma variable names (e.g., `--radius`) and engine values.
*   **Constraint**: This is where validation tests are authored. It uses the foundation's pipes and the tooling's measuring tape to assert its own branded rules.

---

## Pros & Cons of Automated Figma Validation

### Pros
*   **Absolute Fidelity**: Guarantees components match design specs to the pixel and token, eliminating "eye-balling" errors during development.
*   **Semantic Correctness**: Validates *why* a color or size is used (Token ID). Prevents using a `secondary` color that happens to match `primary`'s hex code today but might diverge tomorrow.
*   **Stable CI Baseline**: Reduces reliance on fragile pixel-diffing (snapshots), which are prone to false positives caused by sub-pixel anti-aliasing or font-rendering differences across OSes.
*   **Automated Regression**: Automatically flags when a code change inadvertently alters a component's "anatomy" (e.g., a margin that was accidentally hardcoded instead of using a token).

### Cons
*   **Implementation Overhead**: Requires deep changes to the core UI stack (`Style.kt`, `UiContext.kt`) to thread metadata from high-level DSLs down to raw draw primitives.
*   **Test Fragility**: High-fidelity tests are "brittle" by design. A 1px shift in a Figma spec will break CI, requiring high coordination between design and engineering.
*   **Metadata Bloat**: Increases the memory footprint of a `UiFrameOutput` as every primitive now carries additional strings/enums for validation.
*   **Dependency on Figma API**: Automation requires a reliable export pipeline from Figma (REST API or local plugin), introducing an external failure point in the build process.

---

## Sample Code Recommendation

Below is a conceptual example of how these changes would be integrated into the existing testing DSL.

### 1. Metadata Enrichment (`ui-core`)
Update primitives to carry their semantic origin.

```kotlin
// In UiDrawPrimitive.kt
data class Quad(
    val x: Float, val y: Float, val w: Float, val h: Float,
    val color: Color,
    val tokenId: String? = null, // New: Track design system origin
    val transform: UiPrimitiveTransform? = null
) : UiDrawPrimitive()
```

### 2. Fidelity Configuration (`ui-testing`)
Extend the validation config to support exact matches and token verification.

```kotlin
// In AwakeUiPreviewValidation.kt
data class AwakeUiPreviewValidationConfig(
    // ... existing safety rules ...
    
    /** Assert exact pixel dimensions from Figma */
    val dimensionRules: List<DimensionRule> = emptyList(),
    
    /** Assert that specific semantic nodes use the correct design tokens */
    val tokenRules: List<TokenRule> = emptyList()
)

data class TokenRule(val nodeId: String, val expectedToken: String)
data class DimensionRule(val nodeId: String, val exactHeight: Float? = null, val exactWidth: Float? = null)
```

### 3. High-Fidelity Test Case
A sample test ensuring a Shadcn button exactly matches its Figma specification.

```kotlin
@Test
fun `validate shadcn button against figma spec`() {
    val scene = renderAnnotatedUiPreview(ShadcnButtonPreview)
    
    val figmaSpec = AwakeUiPreviewValidationConfig(
        minContentPaddingPx = 16f, // Safety check
        tokenRules = listOf(
            TokenRule("button.background", expectedToken = "primary"),
            TokenRule("button.label", expectedToken = "primary-foreground")
        ),
        dimensionRules = listOf(
            DimensionRule("button.container", exactHeight = 40f)
        )
    )
    
    // This now fails if the height is 39px OR if the background color 
    // hex matches but was assigned via 'secondary' token instead of 'primary'.
    verifyAwakeUiPreview(scene, config = figmaSpec)
}
```

---

## 4. Holistic Testing Scenarios

Beyond Figma fidelity, the following holistic scenarios should be integrated into the validation suite to ensure overall UI quality.

### 1. Performance & Overdraw Validation
*   **Primitive Count Gating**: Assert that a component doesn't exceed a specific number of `UiDrawPrimitive`s (e.g., "A standard Button should not emit more than 8 primitives"). This catches nested layout bloat.
*   **Overdraw Calculation**: Implement an inspector that calculates the overdraw ratio (total area of all quads vs. viewport area) to identify invisible layers wasting GPU cycles.

### 2. Accessibility (a11y) Scenarios
*   **Contrast Ratio Enforcement**: Automate contrast checks between the `foreground` and `background` tokens assigned to a node against WCAG AA/AAA standards.
*   **Touch Target Minimums**: Assert that all interactive roles (`Button`, `Toggle`, `Switch`) have a minimum semantic bounding box of at least 40x40dp (DPI-stable size).
*   **Focus Order Verification**: Validate that the semantic ID list corresponds to a logical "Tab" order (Top-to-Bottom, Left-to-Right).

### 3. Cross-Platform Consistency
*   **Backend Parity Matrix**: Run a specific suite of "Core Graphics" previews across both the Desktop Vulkan and Headless Wasm/WebGPU backends. Assert that the resulting `UiFrameOutput` (primitive list) and pixel snapshots are identical to ensure math parity.

### 4. Interactive Flow Sequencing
*   **Logical Flow Assertions**: Move beyond "matrix-of-states" tests to "sequenced-flow" tests.
    *   *Example:* `Simulate Hover(id) -> Assert SemanticState(Hovered) -> Simulate Click(id) -> Assert SemanticNode(id=Dialog) Exists`.
    *   This ensures the logic connecting interaction to layout is sound across engine updates.

### 5. Animation & Dynamic Style Validation
*   **Interpolation Fidelity**: Current tests capture static frames. We lack a way to assert that a `Style` transition (e.g., background color lerp on hover) follows the expected curve or responsiveness defined in the design system.
*   **Jitter & Snap Detection**: Automated detection of "snaps" (discontinuous jumps in layout or color) during an animation. Currently, this requires manual inspection of frame dumps.
*   **Style State Coverage**: While `componentStateMatrix` exists, it doesn't easily validate complex state combinations (e.g., "Pressed + Focused + Disabled") and how their style rules resolve against Figma's logic.

---

## 6. Testing Priority & Strategy

To scale validation without overwhelming CI or slowing down development, the following prioritization strategy is recommended:

### 1. Atomic Fidelity First (Design System)
The primary gate for design fidelity must be the **atomic components** in `ui-designsystem`.
*   **Component-Level Assertions**: Every base component (Button, Input, Checkbox) must have a dedicated fidelity test using `AwakeUiPreviewValidationConfig` with exact dimensions and token matches.
*   **Token Integrity**: A component test should fail if a hex color matches but the `tokenId` is missing or incorrect. This prevents "lucky" matches where a hardcoded color happens to be correct today but drifts tomorrow.

### 2. Layout Integrity Second (Showcase)
The `samples:ui-showcase` should focus on **composition and integration**.
*   **Composition Safety**: Validation here should use relaxed "Safety" rules (min-padding, no truncation, no overlap) to ensure that layout combinations remain usable across themes.
*   **Regression Discovery**: Use the showcase to discover where atomic changes affect high-level page composition.

### 3. Automated Gating Strategy
*   **Fidelity Baseline**: Lock in a "Design Baseline" for all atomic components. Any 1px shift or token change requires an explicit review.
*   **Continuous Loop**: Integrate the `ui-preview-watch` loop into the design workflow so developers see fidelity violations in real-time during component authoring, rather than discovering them at the PR stage.

### Prioritization Hierarchy (The "Golden Rule")

| Priority | Level | Surface | Goal | Validation Method |
| :--- | :--- | :--- | :--- | :--- |
| **1** | **Atomic Fidelity** | `ui-designsystem` | Match Figma Specs 1:1 | `exactPadding`, `tokenAssertions`, `exactHeight` |
| **2** | **Layout Safety** | `ui-showcase` | Prevent broken layouts | `minPadding`, `noOverlap`, `noTruncation` |
| **3** | **Interactive Integrity** | Both | Correct state response | `InteractionSequence`, `componentStateMatrix` |
| **4** | **Visual Stability** | Both | Catch unintended drift | `pixelSnapshots`, `UiSnapshotSignatureTest` |

---

## 7. Implementation Roadmap & Proof of Success

To move this from audit to implementation, the following tasks are recommended.

### Phase A: Foundation (Metadata)
| Task | Description | Proof of Success | Complexity |
| :--- | :--- | :--- | :--- |
| **A.1** | Add `tokenId: String?` to `UiDrawPrimitive` and update all subtypes. | `Quad` and `Glyph` instances carry `primary` or `muted` strings in unit tests. | **S** |
| **A.2** | Implement `ShadowQuad` and `Style.shadow()`. | A Card emits a shadow primitive with correct offset/blur metadata. | **M** |
| **A.3** | Add visual metadata (color, radius, token) to `UiSemanticNode`. | `UiContext` captures `backgroundColor` in the semantic dump of a frame. | **S** |
| **A.4** | Update `Style.kt` to accept an optional `tokenId` in background/foreground rules. | A `shadcnButton` emits a `Quad` where `tokenId == "primary"`. | **M** |

### Phase B: Verification (Inspectors)
| Task | Description | Proof of Success | Complexity |
| :--- | :--- | :--- | :--- |
| **B.1** | Implement `inspectExactPadding` in `UiSemanticInspection.kt`. | A test fails if a Button has 15px padding instead of the required 16px. | **S** |
| **B.2** | Add `DimensionRule` support to `validateAwakeUiPreview`. | A test fails if a standard Button height is 38px instead of 40px. | **S** |
| **B.3** | Implement `tokenAssertions` logic. | A test fails if a Label uses the `muted` token instead of `primary-foreground`. | **M** |

### Phase C: Integration (Tooling)
| Task | Description | Proof of Success | Complexity |
| :--- | :--- | :--- | :--- |
| **C.1** | Export `design-report.json` in `UiPreviewReportTask`. | A JSON file exists in `build/reports` containing all semantic/token metadata per component. | **M** |
| **C.2** | Create a `FigmaModeMatrix` helper for tests. | A single test function renders a component in Light, Dark, and Compact modes in one pass. | **M** |
| **C.3** | Verify `ui-designsystem` atomic components. | 100% of base Shadcn components (Button, Input, etc.) have green "Fidelity" status. | **L** |

---

## 8. Future Proofing & Deep Fidelity

To move beyond a 1:1 "snapshot match" and achieve production-grade design fidelity, the following long-term strategies must be considered.

### 1. Pixel Snapping Policy
Figma is mathematically exact (sub-pixel), but physical screens are discrete. Our validation engine must decide on a **Deterministic Snapping Policy**:
*   **The Problem**: If a Button is 40.5px tall in Figma but the engine snaps it to 40px, we get a permanent "false positive" mismatch.
*   **The Fix**: Phase A.1 should include a common snapping utility used by both the layout engine and the validator to ensure they agree on rounding (e.g., `floor` vs `round`).

### 2. Geometric Fidelity (Corner Smoothing)
Modern design systems (including high-end Shadcn/Tailwind implementations) often use **Corner Smoothing** (continuous curvature/squircles) rather than simple circular arcs.
*   **The Gap**: `RoundedQuad` currently only supports a single `radius` (circular arc).
*   **Future Proof**: We should eventually extend `RoundedQuad` or add a `SmoothRoundedQuad` that accepts a `smoothing` parameter (0.0 to 1.0) to match Figma's "Corner Smoothing" variable.

### 3. Color Space Parity (sRGB vs. Linear)
Figma operates in sRGB space. Our renderers (Vulkan/WebGPU) often perform lighting math in Linear space.
*   **The Problem**: A simple hex copy from Figma (`#F87171`) will look "washed out" or "too dark" if the color transformation isn't mathematically identical to Figma's preview.
*   **The Fix**: Validation must include a **Color Transformation Assertion** that verifies the resulting pixel values (after Gamma/Tonemapping) match the target sRGB value within a specific Delta-E tolerance.

### 4. Font Baseline & Metric Fidelity
Figma aligns text to a baseline; engines often align to a bounding box or cap-height.
*   **The Gap**: "Vertical Centering" of text in Figma often looks slightly different than in-engine due to how leading and descent are handled.
*   **Deep Fidelity**: Implement an inspector that reads **Font Metric Metadata** (Ascent, Descent, Baseline) from the `Glyph` primitive to ensure the *optical* center of the text matches Figma, not just the bounding box center.

### 5. Prototyping Interaction Fidelity
Figma prototypes define specific easing curves (e.g., `Cubic Bezier (0.4, 0, 0.2, 1)`) and durations.
*   **Deep Fidelity**: Extend `UiAnimationFrameCapture` to not just record frames, but to **Plot the Curve**. Assert that the sampled values (alpha, position) follow the specific Bezier path defined in the design system, rather than a generic linear transition.

### 6. Design System Pluggability
While the current focus is Shadcn, the **Architectural Layering** ensures that `ui-core` and `ui-testing` remain unaware of "primary" or "secondary" logic.
*   **Scalability**: This ensures we can onboard **Material 3** or a custom **Internal Brand** system by simply updating the `Design System` layer, reusing the same "pipes" and "measuring tape".

---

## 9. Figma Integration & Validation Execution

To bridge Figma variables with the engine's validation suite, the following execution pattern is recommended. This avoids hardcoding Figma values in tests and instead drives assertions from the JSON Design Schema.

### 1. Design Token Schema (`design-tokens.json`)
The expected output from a Figma Variable sync script.

```json
{
  "modes": ["light", "dark"],
  "tokens": {
    "primary": { "light": "#1d4ed8", "dark": "#3b82f6" },
    "radius-lg": { "default": "8px" },
    "button-height": { "default": "40px" },
    "button-padding-x": { "default": "16px" }
  }
}
```

### 2. Figma Variable Provider
A utility to load and resolve tokens within a test context.

```kotlin
interface FigmaVariableProvider {
    fun getPx(tokenId: String, mode: String = "default"): Float
    fun getColor(tokenId: String, mode: String = "default"): Color
}
```

### 3. Automated Validation Execution
Executing a test that pulls the latest specifications directly from the design schema.

```kotlin
@Test
fun `validate shadcn button against live figma variables`() {
    // Load variables from synced JSON
    val figma = loadFigmaVariables("path/to/design-tokens.json")
    val mode = "light"
    
    val scene = renderAnnotatedUiPreview(ShadcnButtonPreview)
    
    val dynamicSpec = AwakeUiPreviewValidationConfig(
        // Pull exact requirements from Figma tokens
        minContentPaddingPx = figma.getPx("button-padding-x", mode),
        
        tokenRules = listOf(
            TokenRule("button.background", expectedToken = "primary"),
            TokenRule("button.label", expectedToken = "primary-foreground")
        ),
        
        dimensionRules = listOf(
            DimensionRule(
                nodeId = "button.container", 
                exactHeight = figma.getPx("button-height", mode)
            )
        )
    )
    
    // Executes logic in Phase B (Fidelity Inspectors)
    verifyAwakeUiPreview(scene, config = dynamicSpec)
}
```

### 4. CI Execution Flow
1.  **Sync**: `scripts/sync-figma-variables.py` pulls from Figma REST API and writes `design-tokens.json`.
2.  **Test**: `./gradlew :awake:engine:ui:designsystem:desktopTest` runs.
3.  **Validate**: `verifyAwakeUiPreview` compares the emitted `UiFrameOutput` against the loaded JSON specs.
4.  **Report**: `UiPreviewReportTask` generates a JSON diff if tokens or dimensions mismatched.
