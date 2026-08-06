# UI Figma Validation Audit — Findings & Gaps

> **Status:** DRAFT · **Date:** 2026-08-06
> This document audits the current UI testing, semantics, and rasterization infrastructure to identify blockers for automated validation against the **Shadcn Figma Design System** (Variables, Sizing, Padding, Spacing, and Tokens).

## 1. Style & Token Metadata Gap

Currently, the `UiContext` emits "dead" primitives. Once a component like a `shadcnButton` is rendered, its high-level design intent is lost and reduced to raw geometry and hex colors.

### Gaps in Primitives (`UiDrawPrimitive`)
*   **Missing Token IDs**: `Quad`, `RoundedQuad`, and `Glyph` lack a `tokenId` property. To validate against Figma, we must know that a specific `Quad` represents the `primary` background or the `input` border, rather than just its resulting `Color` value.
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

### Phase A: Metadata Enrichment (Core)
1.  **Enrich Primitives**: Add `val tokenId: String?` to `UiDrawPrimitive` and all its subtypes.
2.  **Enrich Semantics**: Add `backgroundColor`, `borderRadius`, and `textStyleToken` to `UiSemanticNode`.
3.  **Thread Tokens**: Update `Style.kt` and `UiContext` to propagate the `tokenId` from the design tokens down to the emitted primitives.

### Phase B: Fidelity Inspectors (Testing)
1.  **Exact Matchers**: Implement `inspectExactPadding` and `inspectExactSpacing` in `UiSemanticInspection.kt`.
2.  **Dimension Assertions**: Add `minWidth`/`maxWidth`/`exactHeight` constraints to `AwakeUiPreviewValidationConfig`.
3.  **Token Assertions**: Create a `tokenAssertions` rule set to verify that components are using the correct semantic variables (e.g., "Button text must use the `primary-foreground` token").

### Phase C: Structured Reporting (Tooling)
1.  **Design Data Export**: Update `UiPreviewReportTask` to output a structured JSON "Design Report" (per component) alongside the HTML gallery.
2.  **Figma Comparison**: Create a script to compare the generated "Design Report" against a JSON export from Figma (e.g., via the Figma REST API or a plugin).
