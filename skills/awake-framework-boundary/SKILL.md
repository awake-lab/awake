---
name: awake-framework-boundary
description: Decide whether a proposed capability belongs in the Awake framework or in a consuming game repository. Use before adding an engine module, promoting sample code, or introducing networking, persistence, server, or MMO-oriented abstractions.
metadata:
  author: awake
  last-updated: '2026-08-20'
---

# Awake Framework Boundary

Read [framework-game-boundary.md](../../docs/reference/framework-game-boundary.md) before
deciding where a capability belongs.

Classify the proposal as **Awake capability**, **consumer/game code**, or **defer**.

1. A future MMORPG is one consumer, not justification for a new Awake module.
2. Prefer consumer-side composition. Promote only after two credible consumers demonstrate the
   same stable need, or a concrete public-API limitation makes that impossible.
3. Keep accounts, combat, quests, inventories, economy, zones, shards, protocol messages,
   databases, authentication, and live operations in the consuming game.
4. A promoted capability exposes the smallest backend-neutral contract and must not make a
   network library, database, renderer, or UI stack mandatory for ECS/core runtime modules.
5. Record exceptions with consumers, missing API, contract owner, excluded policy, dependency
   direction, and validation plan.

Use `awake-architecture-auditor` for the decision. Use implementation skills only after it.
