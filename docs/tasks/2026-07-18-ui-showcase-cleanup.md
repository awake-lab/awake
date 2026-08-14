# UI Showcase Cleanup

## Goal

Turn `samples:ui-showcase` from a single large catalog file into a clearer sample structure that
can carry the full Awake shadcn component push without turning every follow-up into page-level
spaghetti.

## Non-Negotiables

- keep shared UI behavior in reusable engine modules
- keep branded showcase recipes in `ui-designsystem`
- keep sample-local wiring in `samples:ui-showcase`
- keep preview validation and layout/snapshot baselines green

## Checklist

### Phase 1: Catalog Structure

- [x] split showcase metadata from showcase chrome/layout composition
- [x] split page preview implementations out of the catalog registry file
- [x] localize page-specific helpers and constants near the page that uses them
- [x] replace page-id `when` sprawl with page-owned preview renderers

### Phase 2: Validation Surface

- [x] ~~keep preview validation wired through `AwakeUiPreview`~~ -- superseded 2026-08-14:
  fixtures are derived from `ShowcasePages` and carry their own metadata, because
  annotation-driven metadata skipped iOS and wasmJs entirely
- [x] keep showcase layout signatures green after structural cleanup
- [x] keep widget snapshot signatures green after adding the component-state matrix baseline

### Phase 3: Shadcn Component Completion

- [x] export showcase preview docs at 2x raster scale while preserving logical layout size
- [x] add initial component state-matrix previews for buttons and field controls
- [x] add open-state overlay previews for dropdown menus and alert dialogs
- [x] keep popup overlays out of wrap-content measurement so preview semantics stay truthful
- [x] add widget-level preview proofs for slider, tooltip, and scroll panel
- [x] every public component family has a catalog page (2026-08-14 restructure); visual parity
  per family is still tracked in `2026-08-12-ui-showcase-parity-tracker.md`
- [ ] finish field-family parity: input, textarea, select, dropdown menu, popover
- [ ] finish overlay-family parity: dialog, tooltip, sheet, sidebar, context menu
- [ ] finish selection-family parity: tabs, radio group, switch, command/menu patterns
- [ ] add component state-matrix previews per shared widget instead of only page-level previews
- [ ] add sampled animation previews: rest, in-flight, settled

### Phase 4: Future Cleanup

- [ ] decide which sample-local compositions should be promoted into `ui-dsl`
- [x] preview validation and layout signatures now run on desktop, iOS, wasmJs, and Android
  host -- the reflection-based metadata that forced desktop-only proof is gone
- [x] add a dedicated shadcn parity scorecard -- see [`docs/reference/shadcn-parity.md`](../reference/shadcn-parity.md), built from `ronjunevaldoz/shadcn-compose`'s published `component-metadata.json` (real variant lists + real rendered preview images) rather than scraping `ui.shadcn.com` directly
