# Tasks

## Current Objective

Wire the serialized scene runtime into the MVP bootstrap path and keep the asset binding
layer clean.

## Active Phase

- 2026-07-10: `SceneDocument`, `SceneLoader`, and the `scene.json` contract have landed in
  `awake-scene`; next we connect renderable requests to the app-side bootstrap and asset
  resolver.

## Open Questions

- Should the first scene bootstrap live in `awake-demo/shared` or a dedicated runtime entry
  point?
- Do we resolve renderable requests eagerly during scene load, or keep them as a second
  bind step?
- Should the sample `scene.json` live in the engine module, or in the consuming app?

## Fix Lanes

- Dev: Scene runtime bootstrap
- Beta: None yet
- Stable: Refresh runtime docs after the app-side binding path is verified

## Task Log

- [2026-07-09-decouple-world](tasks/2026-07-09-decouple-world.md)
- [2026-07-10-scene-runtime](tasks/2026-07-10-scene-runtime.md)

## Archive Index

- None yet
