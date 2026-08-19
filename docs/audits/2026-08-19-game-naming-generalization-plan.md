# "Game" naming — generalize for non-game consumers (video/animation apps)

Status: naming decision, execution via IDE rename (Android Studio "Rename..." refactor),
not a scripted migration. Triggered by a real stated requirement (not hypothetical): Awake's
`engine/game` layer must support non-game apps (video/animation tools) equally, and
`Game`/`GameApplication`/`GameShaderSet` name the abstraction after one use case it must not
be limited to.

## Why the name is actually wrong, not just narrow

`Game` (`engine/game/Game.kt`) is: `ready(renderer)`, `render(delta, w, h)`, `resize`,
`pause`, `resume`, `dispose`. Nothing here is game-specific — no input mapping, no score, no
game-loop-specific state. A video player or an animation/visualization tool implements this
identically. The abstraction is already generic; only the name claims otherwise.

## Blast radius (measured, not estimated — informs rename order, not a migration plan)

```
GameApplication   — 93 files (awake/, docs/, skills/)
GameShaderSet     — 45 files
engine.game (pkg) — 246 files
```

IDE rename handles reference updates mechanically and safely across this — the concern below
is naming quality and rename sequencing, not migration risk.

## Naming recommendation

| Current | Rename to | Why |
|---|---|---|
| `Game` (interface) | `AppBehavior` | Matches its own doc comment ("a game's own behavior, injected") almost verbatim — nothing game-specific in the contract. Considered `Content` (rejected — collides conceptually with `render-extensibility.md`'s own use of "content" for authored pipeline data) and `AwakeApp` (rejected for this slot — ties the *behavior contract* to the brand name; better reserved for the concrete subtype, below). |
| `GameApplication` | `AppRuntime` | Distinct from `WindowApplication` one layer down — avoids two stacked "Application" names. Reads as what it is: the Mediator wiring window/renderer/behavior together (skill §4). Considered `RenderApplication` (rejected — same stacking-ambiguity problem). |
| `VulkanGameApplication` / `WebGpuGameApplication` | `VulkanAppRuntime` / `WebGpuAppRuntime` | Follows from the `AppRuntime` rename. |
| `GameShaderSet` | `ShaderSet` | Was never game-specific — just a shader-resource-path bundle. |
| `AwakeGame` (the `Game` subtype `GameApplication.kt:26` casts to for `.input`) | `AwakeApp` | Same reasoning as `Game`, at the concrete-subtype level where the brand name fits naturally. Separately worth checking whether assuming every `AppBehavior` has `.input` still holds for a non-interactive video/animation consumer — the rename won't fix that assumption if it's wrong, just makes it visible. |
| module `engine/game` | `engine/app` | `engine/app` reads correctly for a video/animation consumer in a way `engine/game` never will. |

## Rename order

IDE rename per symbol, smallest/most-contained first, so each step's diff stays reviewable:

1. `Game` → `AppBehavior` (interface, few implementers).
2. `GameApplication` → `AppRuntime`, then `VulkanGameApplication`/`WebGpuGameApplication` →
   `VulkanAppRuntime`/`WebGpuAppRuntime`.
3. `GameShaderSet` → `ShaderSet`.
4. `AwakeGame` → `AwakeApp` last — touches the most call sites since it's the concrete type
   games actually implement.
5. Module path `engine/game` → `engine/app` as its own final step (move directory +
   `settings.gradle.kts` + import path updates). Keep this separate from the class-level
   renames above — mixing a module move into the same refactor operation as a class rename is
   what causes the IDE's refactor tool to lose track of references mid-operation.

## Scope note for in-flight drafts

`2026-08-19-render-feature-strategy-plan.md` and
`2026-08-19-application-layer-shape-options.md` are still unimplemented — if this rename is
done before that work starts, write `GameShaderSetSpec` as `ShaderSetSpec` directly rather
than renaming an already-drafted design after the fact.
