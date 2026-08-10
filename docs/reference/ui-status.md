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
| Spacing scale (`Tw.Spacing`) | ✅ | ✅ | ✅ | Generated from a vendored Tailwind scale by `:awake:engine:ui:tailwind-generator`. |
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
| `UiShowcaseLayoutSignatureTest` | Page layout didn't change unnoticed | That the current layout is right |
| `ShadcnParityScreenshotTest` | Pixels didn't change unnoticed | Same |
| `ShadcnReferenceComparisonTest` | Mismatch % didn't get **worse** | Fidelity — baselines were frozen *after* divergence existed (accepts 28–44%) |

**The pattern:** every gate is a *regression* gate. Only the radius and color tests encode an
external truth. That is the structural gap behind every bug found on 2026-08-10.

## Open risks

| # | Risk | Impact | Status |
|---|---|---|---|
| 1 | Button missing `px-4` | Real `h-9 px-4 py-2`; `ShadcnStyles.button()` sets no `contentPadding` at all. Affects every button's width. | **Confirmed vs source, unfixed** — needs its own rebaseline cycle |
| 2 | ~3% text width divergence vs reference | Cumulative horizontal drift | Cause unknown. Two hypotheses **disproven**: framing misalignment (3.5 of 38 pts) and the advance clamp (0.2% on real strings — do not "fix" it) |
| 3 | Test font ≠ app font | Alignment bugs pass tests and ship | Documented, unfixed |
| 4 | Parity baselines accept 28–44% | Can't certify fidelity | Needs component-aligned comparison + risk 1/2 fixed, then rebaseline |
| 5 | Three spacing vocabularies | `UiSpacing` / `ShadcnSpacing` / `Tw` | Decided: `Tw` in designsystem, `UiSpacing` in core/headless, delete `ShadcnSpacing`. Sweep not done — must be per-site (a mechanical rename would launder 4 known-wrong values) |
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
