# UI status report

Standing answer to "what's supported, what's tested, what's stable, what's known-broken" for
`ui-core` / `ui-headless` / `ui-designsystem`. Update this in the same commit as any change that
moves a row — it is meant to remove the need to re-audit (or re-ask) each time.

Last verified: **2026-08-10**, against pinned reference `third_party/shadcn-ui-ref` @ `6261bd89…`.

Companion docs: `ui-component-coverage.md` (per-component inventory + missing list),
`skills/awake-shadcn-styling/SKILL.md` (how to read Tailwind, source-of-truth rule).

---

## Legend

| Mark | Meaning |
|---|---|
| ✅ | Verified against the pinned shadcn source, with a test that would catch a regression |
| 🟡 | Implemented and believed correct, but **nothing tests the specific values** |
| ⚠️ | Known-wrong or unverified against source |
| ❌ | Not implemented |

---

## Subsystem status

| Area | Supported | Stable | Verified vs source | Notes |
|---|---|---|---|---|
| Spacing scale (`Tw.Spacing`) | ✅ | ✅ | ✅ | Generated from a vendored Tailwind scale by `:awake:ui:tailwind-generator`. |
| Radius ladder (`fromBase`) | ✅ | ✅ | ✅ | Multiplicative, locked by `ShadcnRadiusScaleTest`. Was additive and silently wrong for every non-Vega preset until 2026-08-10. |
| Color tokens | ✅ | ✅ | ✅ | `ShadcnReferenceTokenTest` vs published OKLCH values. |
| Component sizes/padding | 🟡 | 🟡 | 🟡 | 10 source-verified bugs fixed 2026-08-10; the rest are unaudited. No mechanism prevents new drift — see Open Risks. |
| Text layout / vertical centering | ⚠️ | 🟡 | ❌ | Fidelity tests run `BitmapFont`; the app runs `PackedUiFont`. Different `visibleTopEm`/`lineHeightEm` → the tests cannot prove app behavior. |
| Cursor (hover/resize) | ✅ | ✅ | n/a | Fixed 2026-08-10. Was inert: `runVulkanDesktopGame(cursor=)` defaults to `null` and no sample passed it. **Only `ui-showcase` is wired** — studio/scene3d still pass nothing. |
| Resizable drag | ✅ | ✅ | ✅ | Hit strip 2dp → 4dp (real `after:w-1`). |
| Theme presets | 🟡 | ✅ | 🟡 | Only `Vega` maps to real shadcn; the other 7 are Awake-original density variants with no upstream spec. |

## Test gates — what each actually proves

| Gate | Proves | Does **not** prove |
|---|---|---|
| `ShadcnRadiusScaleTest` | Radius ladder matches Tailwind's formula | Any component uses it correctly |
| `ShadcnReferenceTokenTest` | Color values match published OKLCH | Anything about layout |
| `*FidelityTest` | Exact per-component dimensions | Correctness vs shadcn — the numbers are ours, and use `BitmapFont` |
| `UiShowcaseLayoutSignatureTest` | Same page renders identically twice; no two pages collide | That the current layout is right (see `ShadcnGeometryParityTest`) |
| `ShadcnParityScreenshotTest` | Pixels didn't change unnoticed | Same |
| `ShadcnReferenceComparisonTest` | Colour/radius/border/shadow mismatch % didn't get **worse** | Layout fidelity — demoted 2026-08-15, see `docs/reference/ui-validation.md`'s coverage matrix |
| `ShadcnGeometryParityTest` | Exact size/position vs shadcn's own `getBoundingClientRect`, sub-pixel | Colour, border, shadow — no dimension it doesn't literally have a number for |

**The pattern above is now half-fixed.** Layout has an external-truth oracle
(`ShadcnGeometryParityTest`, 2026-08-15) covering 7 of ~14 components, both themes for badge
only. Colour/radius/border/shadow still has none — pixel diff is a regression gate for that
dimension, not a correctness one, same structural gap this section originally described.
`UiShowcaseLayoutSignatureTest`'s 54 recorded hex constants were dropped the same day: a
baseline nobody reads on regeneration isn't a gate, and it was bulk-regenerated four times in
one session.

## Open risks

| # | Risk | Impact | Status |
|---|---|---|---|
| 1 | Button missing `px-4` | Real `h-9 px-4 py-2`; `ShadcnStyles.button()` sets no `contentPadding` at all. Affects every button's width. | **Confirmed vs source, unfixed** — needs its own rebaseline cycle |
| 2 | Text width budget vs density scale | Labels truncate at widths that should fit; ~3% strip-level drift | **Narrowed 2026-08-10.** Repro: re-enable `shadcnButtonSizeStyle` (one line) and run `ShadcnButtonFidelityTest` — 'Secondary' truncates in `width=112.0, height=72.0`. That height is 2x a 36dp button, i.e. **physical px at density 2**, while the label's own advances sum to ~67px at 1x (~134px at 2x). Text measures correctly; the available-width budget appears to mix dp- and px-space. Fits the rest: the 1.0x tabs matrix passed while the 2x one failed, and tests use BitmapFont at 1x so they never see it. Three hypotheses **disproven** — framing misalignment (3.5 of 38 pts), the advance clamp (0.2% on real strings, do NOT change it), and `resolveGlyphPx` (returns exactly 14px for 14.sp at density 1) |
| 3 | Test font ≠ app font | Alignment bugs pass tests and ship | Documented, unfixed |
| 4 | Parity baselines accept 28–44% | Can't certify fidelity | Needs component-aligned comparison + risk 1/2 fixed, then rebaseline |
| 5 | Three spacing vocabularies | `UiSpacing` / `ShadcnSpacing` / `Tw` | **Done 2026-08-18.** `ShadcnSpacing` deleted (its only reference was the unused `ShadcnResolvedTheme.spacing` property, zero real call sites). `UiSpacing` deleted too — `ui-core`/`ui-headless` own no named spacing scale at all, matching real Compose Foundation module boundaries (Foundation ships zero built-in spacing tokens; only a design-system layer like `Tw` owns a named scale). Every `UiSpacing.sm` call site (`UiContext.kt`, `UiMeasurementRuntime.kt`, `UiScopeRuntimeAccessors.kt`, `layouts/Surface.kt`, `layouts/Arrangement.kt`, plus `LayoutTest.kt`/`PanelTest.kt`) inlined to the literal `8f.dp`. `ShadcnSurfaceStyles.kt`'s `shadcnLegacyAmbientSurfaceStyle` also inlined to `8f.dp` with a comment recording why (reproduces ui-core's former ambient default, not a shadcn-branded value). `Tw` remains the only named spacing scale in the codebase, correctly confined to designsystem. The "4 known-wrong values" cited at `ShadcnInputOtpRecipes.kt`/`ShadcnVariants.kt` no longer exist at those sites; both now use `Tw`-based `.tw` units or plain literal `Dp` (E6, parked). |
| 6 | Cursor wired in one sample only | studio/scene3d have no hover cursors | Known |
| 7 | `popup()` can't take min/max bounds | `max-w-*`/`max-h-*` classes are unportable for any popup-based component (AlertDialog, Dialog, Sheet, Drawer, Popover). `UiModifier.widthIn(max=)` exists, but `popup()` sizes from a `Dimension`, so the constraint can't reach it. AlertDialog is parked at 320dp because of this. | Open — plumbing change in `popup()` |

## How to stop this recurring

Ranked by leverage. The first is the only one that scales:

1. **Assert against the pinned reference, not against ourselves.** `third_party/shadcn-ui-ref`
   holds the real `.tsx`; `tools/extract_shadcn_tokens.py` already parses it. A test that extracts
   the governing Tailwind class per component and compares it to our constant turns every value
   into a machine-checked fact. Until that exists, correctness depends on someone re-reading
   source by hand — which is exactly how 10 bugs accumulated.
2. **Keep this table honest.** A row moving to ✅ requires a test that would fail if reverted.
3. Compile-time exceptions and console warnings do **not** work for this class of bug: the
   compiler cannot know `h-9` is 36dp, and a warning nobody reads is not a gate.
