// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

// jolt-physics's bundled Emscripten output (dist/jolt-physics.wasm-compat.js) contains a
// `if (isNode) { const { createRequire } = await import("node:module"); ... }` fallback
// branch for running under plain Node.js (never taken in a browser -- `isNode` is false
// there), but webpack still statically resolves every `import()`/`require()` it sees
// regardless of the runtime guard around it, and fails the whole build with
// `UnhandledSchemeError: Reading from "node:module" is not handled by plugins` since
// webpack's browser target has no loader for the `node:` URI scheme. Kotlin/Wasm's
// `browser()` webpack task auto-merges any `*.js` file under this directory (same
// `webpack.config.d` convention Kotlin/JS's webpack DSL already documents) -- this maps
// each Node core-module specifier jolt-physics's bundle references in that dead branch to
// `false` (webpack's "provide an empty stub, don't try to bundle a real implementation"
// sentinel), which is enough to satisfy webpack's static resolution without ever needing
// the real Node module (the branch referencing them is unreachable in a browser).
// `resolve.fallback` alone isn't enough here -- webpack's `UnhandledSchemeError` for a
// `node:`-prefixed specifier is raised by its *scheme handling* step (no plugin registered
// for the `node:` URI scheme at all), which runs before normal module resolution/fallback
// ever gets a say. `IgnorePlugin` intercepts the request earlier, before scheme resolution,
// and skips creating a module for it entirely -- the correct fix for this class of error
// (confirmed by trying `resolve.fallback` first: it compiled but still threw the same
// `UnhandledSchemeError`).
const webpack = require('webpack');
config.plugins = config.plugins || [];
config.plugins.push(new webpack.IgnorePlugin({ resourceRegExp: /^node:/ }));
