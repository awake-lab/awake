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

- [x] keep preview validation wired through `AwakeUiPreview`
- [x] keep showcase layout signatures green after structural cleanup
- [x] keep widget snapshot signatures green after adding the component-state matrix baseline

### Phase 3: Shadcn Component Completion

- [x] export showcase preview docs at 2x raster scale while preserving logical layout size
- [x] add initial component state-matrix previews for buttons and field controls
- [x] add open-state overlay previews for dropdown menus and alert dialogs
- [x] keep popup overlays out of wrap-content measurement so preview semantics stay truthful
- [x] add widget-level preview proofs for slider, tooltip, and scroll panel
- [ ] finish field-family parity: input, textarea, select, dropdown menu, popover
- [ ] finish overlay-family parity: dialog, tooltip, sheet, sidebar, context menu
- [ ] finish selection-family parity: tabs, radio group, switch, command/menu patterns
- [ ] add component state-matrix previews per shared widget instead of only page-level previews
- [ ] add sampled animation previews: rest, in-flight, settled

### Phase 4: Future Cleanup

- [ ] decide which sample-local compositions should be promoted into `ui-dsl`
- [ ] add web preview validation so desktop is not the only proof lane
- [ ] add a dedicated shadcn parity scorecard against `ui.shadcn.com`
