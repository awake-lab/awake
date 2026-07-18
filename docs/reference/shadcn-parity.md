# Shadcn parity reference

Real, machine-readable ground truth for how close Awake's `ui-designsystem` components are
to actual shadcn/ui, pulled from `ronjunevaldoz/shadcn-compose`'s published
`docs/component-metadata.json` (schema: `docs/component-metadata.md` in that repo) instead of
memory or a hand-written "vibes" comparison. See
[`AwakeShadcnReferenceTokenTest`](../../awake/engine/ui-designsystem/src/commonTest/kotlin/io/github/ronjunevaldoz/awake/ui/designsystem/AwakeShadcnReferenceTokenTest.kt)
for the equivalent check on color *values* -- this doc is about variant/size *coverage* and
actual rendered look, which that test doesn't cover.

**Caveat the source itself calls out:** every preview image below was captured under
shadcn-compose's `Vega` style preset (1 of 8) with `baseColor = Neutral`, `accent = Base`.
Don't treat these as *the* canonical shadcn look -- they're *a* verified one. Swapping preset/
base color/accent changes shape, spacing, and animation on the real thing too.

This is not "100% accuracy" tooling and doesn't claim to be -- visual fidelity has no such
bar. It's a real reference image and a real property list, not a bullet-list of impressions.

## How to refresh this

```bash
curl -sL "https://raw.githubusercontent.com/ronjunevaldoz/shadcn-compose/main/docs/component-metadata.json" -o /tmp/shadcn-metadata.json
# re-run the download step in this doc's own history (git log this file) to see the exact script
```

## Variant/size coverage gaps

| Component | Real shadcn properties | Awake has | Gap |
|---|---|---|---|
| `button` | `ButtonVariant`: Default, Outline, Secondary, Ghost, Destructive, **Link**; `ButtonSize`: Xs, Sm, Md, Lg, Icon | `AwakeShadcnButtonVariant`: Primary, Secondary, Outline, Ghost, Danger. No size axis -- every call site hardcodes its own px width/height. | Missing **Link** variant. No `ButtonSize` equivalent at all. |
| `badge` | `BadgeVariant`: Default, Secondary, Destructive, Outline, **Ghost** | `AwakeShadcnBadgeVariant`: Primary, Secondary, Outline, Danger | Missing **Ghost** variant. |
| `text-field` | `TextFieldVariant`: Default, **Filled**, **Ghost** | `textField()`/`awakeShadcnTextField()`: single fixed look, no variant axis | Missing all 3 variants as a concept -- no error/invalid state, no disabled state either (see below). |
| `select` | `SelectVariant`: Default | `awakeShadcnDropdown()`: no variant axis | Matches (both effectively single-variant). |
| `checkbox` / `switch` / `slider` / `tabs` / `tooltip` / `popover` / `dropdown-menu` / `dialog` / `alert-dialog` | No variant axis in the real component either | Matches | No gap -- these are correctly single-look on both sides. |
| `toggle` | `ToggleVariant`: Default, Outline | We have no separate "Toggle" component from "Switch" -- Awake's `toggle()` is the switch equivalent; a bordered-button-style toggle (shadcn's actual `Toggle`, a different component from `Switch`) doesn't exist here. | Not a gap in the switch we have -- a genuinely separate missing *component* (icon/text toggle-button, not a boolean switch). |

## Visual notes (actually looked at the reference image, not just the property list)

**`button_variants_light.png`** (![button](shadcn-previews/button_variants_light.png)): real shadcn's `Default` variant is solid near-black, `Secondary` is a pale gray fill, `Outline` is a bordered/transparent button, `Ghost` is no fill/no border (label only), `Destructive` is solid red, `Link` renders as plain underlined-style text with no button chrome at all. Confirms our `Primary`/`Secondary`/`Outline`/`Ghost`/`Danger` five already track this correctly by color role (checked against [`AwakeShadcnReferenceTokenTest`](../../awake/engine/ui-designsystem/src/commonTest/kotlin/io/github/ronjunevaldoz/awake/ui/designsystem/AwakeShadcnReferenceTokenTest.kt)'s primary/destructive values); `Link` is the one real look we don't have a variant for.

**`text-field_states_light.png`** (![text-field](shadcn-previews/text-field_states_light.png)): five states shown -- `Default` (bordered, transparent bg), `Filled` (gray fill, no border), `Ghost` (no border/no fill, just a label), an **invalid/error state** (red border + red helper text below the field), and `Disabled` (muted gray, reduced contrast). Awake's `textField()` (built this session) only has the `Default` look -- no `Filled`/`Ghost` variant, no error/invalid state with helper text, no disabled state. This is the most substantive real gap this doc found: error-state styling on a form field is a genuinely common need, not a nice-to-have.

The other 12 downloaded images (`badge`, `checkbox`, `switch`, `slider`, `tabs`, `tooltip`,
`popover`, `dropdown-menu`, `select`, `dialog`, `alert-dialog`) are checked into
[`shadcn-previews/`](shadcn-previews/) for reference but haven't been individually eyeballed
and written up yet -- do that the same way before building or revising each of those
components, not all at once up front.

## Recommended next steps, in priority order

1. Add `textField()`'s missing states first (error/invalid with helper text, disabled) --
   the single gap this doc found with the clearest real-world need.
2. Add a `Link` button variant.
3. Add a `Ghost` badge variant.
4. When building the still-missing components from
   [`docs/tasks/2026-07-18-ui-showcase-cleanup.md`](../tasks/2026-07-18-ui-showcase-cleanup.md)'s
   Phase 3 checklist (textarea, select variants, popover, tabs, radio group, etc), pull that
   component's real preview image from `component-metadata.json` first and look at it before
   writing the recipe -- not after, as a retrofit.
