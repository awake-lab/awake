# About Awake

## Why

Unreal and Unity are editor-first: huge surface area, and it wasn't clear where
to start or what was actually happening under the hood. Godot's editor is nicer,
but same shape of problem — an editor between you and the game. Hand-written
code beats an editor for visibility into what the engine is actually doing.

libGDX was the closest fit for a code-first workflow, but its 3D support is
thin — not enough to build on.

Kotlin Multiplatform was already the daily tool. One codebase that targets
every platform — Desktop, Mobile, Web — instead of maintaining separate
engines or bolting a scripting layer onto a C++/C# core, seemed worth trying.

## The name

Short, one word, same shape as Unity, Unreal, libGDX, Godot. "Awake" is
growing into a meaning beyond the name: the awake developer — someone who
wants to see and control the whole build instead of delegating it to an
editor.

## Lineage

Awake is the third attempt at this. The earlier two didn't make it far enough
to matter, but they're why this one leads with Vulkan and WebGPU instead of
retrofitting a renderer later, and why the ECS and scene layers were designed
before any sample game was built on top of them.

Work started in 2023 (repo history here is shallow and doesn't reach back
that far, so treat the year as approximate).

## Who's building this

Ron June Valdoz — a Senior Software Engineer and long-time Kotlin user, which
is the whole reason this is a KMP engine instead of a C++/C# one. Awake is a
solo project, not a studio: no company behind it, one person deciding what
gets built next.

## Where this goes

The near-term goal is an engine solid enough to ship real games on. Longer
term, the aim is a studio built around it — the same hand-written, editor-optional
approach, scaled up once the engine has the capability to support it.
