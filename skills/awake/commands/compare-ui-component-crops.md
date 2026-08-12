# /awake:compare-ui-component-crops $ARGUMENTS

Run the semantic component crop/parity workflow. Use this when a full showcase screenshot is
too broad to diagnose a component or layout regression.

## Inputs

Prefer a manifest path in `$ARGUMENTS`:

```bash
python3 tools/compare_component_crops.py \
  --manifest tools/ui_component_parity_cases.json
```

For one case, provide the Awake preview PNG, its generated semantic JSON, one or more semantic
node IDs, and the matching component-cropped PNG from `tools/capture_shadcn_local.py`:

```bash
python3 tools/compare_component_crops.py \
  --awake-png samples/ui-showcase/build/ui-previews/<preview-id>.png \
  --semantic-json samples/ui-showcase/build/ui-previews/<preview-id>.json \
  --node-id <component-node-id> \
  --reference-png docs/reference/shadcn-previews-local/<matching-case>_light.png \
  --name <case-name> \
  --padding 4 \
  --out-dir build/reports/ui-component-parity
```

Repeat `--node-id` for a reference case made of several sibling controls. The tool unions their
semantic bounds before cropping; it does not tile or hand-align them.

## Review rules

- The reference PNG must represent the same content, state, theme, and density as the Awake
  crop. A low mismatch number from different content is not evidence of fidelity.
- Open the generated `<name>_awake.png` and `<name>_diff.png` before choosing a threshold.
- A case without `maxMismatchPct` is reported as `REVIEW`, not pass/fail. The tool never records
  or overwrites a baseline. Use `--fail-on-mismatch` only after a
  reviewed per-case `maxMismatchPct` has been added to a local manifest.
- A crop diff answers both "did this component move/change?" and, when paired with the pinned
  shadcn reference, "does this component resemble the source?" It does not replace semantic,
  content-fit, or real-GPU validation.

Outputs are written under `build/reports/ui-component-parity/`:

- cropped Awake PNGs
- red/blue diff heatmaps
- per-case JSON metrics
- `component-parity-metrics.json`
