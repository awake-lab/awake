#!/usr/bin/env bash
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
#
# Near-real-time UI preview loop: Gradle --continuous regenerates the unified, searchable UI
# component lookup (merging the ui-showcase page-level preview gallery and the ui-headless
# bare-widget snapshot gallery -- see the "uiComponentLookupReport" task in the root
# build.gradle.kts) on source change, scoped to the two narrow preview/snapshot-writing test
# classes rather than either module's whole desktopTest suite. A tiny static server auto-reloads
# the browser tab once the merged gallery HTML changes. See docs/reference/developer-docs.md's
# "Live Preview Loop" section.
set -euo pipefail
cd "$(dirname "$0")/.."

PORT="${1:-8090}"
REPORT_DIR="build/reports/ui-component-lookup"

./gradlew \
    :samples:ui-showcase:desktopTest --tests "*UiShowcasePreviewDocsTest*" \
    :awake:ui:headless:desktopTest --tests "*UiSnapshotTest*" \
    uiComponentLookupReport \
    --continuous -q &
GRADLE_PID=$!
trap 'kill "$GRADLE_PID" 2>/dev/null || true' EXIT

exec python3 tools/ui_preview_server.py "$REPORT_DIR" "$PORT"
