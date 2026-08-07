# Shadcn Reference Pipeline

This document is the canonical source for how Awake pins and extracts real shadcn/ui ground
truth, instead of relying on memory, screenshots, or a third party's reimplementation.

## Why

Awake's design-system claims to target shadcn/ui's visual language, but for a long time nothing
mechanically checked that claim -- comparisons were either eyeballed or copied from another
project's own doc comments (see the history note in `ShadcnReferenceTokenTest.kt`). The
`/tmp/shadcn-compose-ref` path referenced by older docs was never pinned or scripted: an
ephemeral manual clone, gone the moment the machine that made it was gone. This pipeline
replaces both problems with a real, reproducible, gitignored checkout of the actual
`shadcn-ui/ui` repository plus a deterministic extractor.

## Pieces

1. **`tools/fetch_shadcn_reference.sh`** -- clones/updates `shadcn-ui/ui` at a pinned commit SHA
   into `third_party/shadcn-ui-ref/` (gitignored -- see `.gitignore`'s `/third_party/` entry).
   Idempotent: safe to re-run any time, always resets the checkout to `PINNED_SHA`.
2. **`tools/extract_shadcn_tokens.py`** (python3, stdlib only) -- parses
   `third_party/shadcn-ui-ref/apps/v4/registry/themes.ts`'s `"neutral"` theme entry (the
   registry object literal that generates shadcn's real `:root`/`.dark` CSS custom properties
   for the new-york-v4 style's neutral base color -- Awake's `ShadcnBaseColor.Neutral`
   counterpart) and writes a generated, test-only Kotlin object:
   `awake/engine/ui/ui-designsystem/src/commonTest/kotlin/io/github/ronjunevaldoz/awake/ui/designsystem/ShadcnReferenceTokens.kt`.
   Deterministic: re-running against an unchanged checkout produces byte-identical output.
3. **`ShadcnReferenceTokenExpandedTest.kt`** (same `commonTest` package) -- diffs `ShadcnTheme`'s
   resolved tokens against `ShadcnReferenceTokens`, in OKLCH-derived sRGB with tolerance, using
   the `KNOWN_DRIFTED` mechanism documented in that file's class doc for tokens the audit found
   already diverged from spec (fixing those values is a separate, later change -- this pipeline's
   job is ground truth and detection, not correction).

## Usage

```bash
tools/fetch_shadcn_reference.sh          # clone/update the pinned checkout
python3 tools/extract_shadcn_tokens.py   # regenerate ShadcnReferenceTokens.kt from it
./gradlew :awake:engine:ui:ui-designsystem:desktopTest --tests "*ShadcnReferenceToken*"
```

## Bumping The Pinned SHA

1. Pick a new commit on `shadcn-ui/ui`'s `main` (e.g. `git ls-remote https://github.com/shadcn-ui/ui.git HEAD`).
2. Edit `PINNED_SHA` in `tools/fetch_shadcn_reference.sh`.
3. Re-run `tools/fetch_shadcn_reference.sh` (moves the checkout), then
   `python3 tools/extract_shadcn_tokens.py` (regenerates `ShadcnReferenceTokens.kt`).
4. Diff the regenerated file. If any token values changed upstream, re-run
   `ShadcnReferenceTokenExpandedTest` and reconcile `KNOWN_DRIFTED` -- a token that was locked
   drift may now match (delete its entry) or a previously-matching token may now need one added.
5. Re-run the wider parity/theme suites this pipeline feeds (see Depends On below) before
   relying on the bump.

## Depends On This Pipeline

- `ShadcnReferenceTokenExpandedTest.kt` / `ShadcnReferenceTokenTest.kt`
  (`awake:engine:ui:ui-designsystem`) -- token-level OKLCH ground truth.
- `docs/reference/ui-validation.md`'s "Investigating Extra Space Reports" absolute-check step --
  points here for the shadcn reference location instead of a manual `/tmp` clone.
- Any future `ShadcnParityScreenshotTest`-style pixel-baseline work that wants a real rendered
  shadcn page to diff against, not just token values, should clone/build from the same pinned
  `third_party/shadcn-ui-ref/` checkout rather than a second ad hoc clone.

## Non-Goals

- This pipeline establishes ground truth and detects drift. It does not fix drifted production
  values -- see `ShadcnReferenceTokenExpandedTest.kt`'s `KNOWN_DRIFTED` worklist for what still
  needs a values-fix pass.
- Only the `"neutral"` base color / new-york-v4 style is extracted today. The other six base
  colors (`stone`, `zinc`, `mauve`, `olive`, `mist`, `taupe` -- `ShadcnBaseColor`'s remaining
  values) live in the same pinned `themes.ts` and can be added to the extractor the same way if
  a future task needs their ground truth too.
