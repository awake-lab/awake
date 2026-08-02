#!/usr/bin/env bash
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
#
# Near-real-time UI preview loop: Gradle --continuous regenerates the ui-showcase
# preview gallery on source change (scoped to the narrow preview-writing test class,
# not the whole desktopTest suite), and a tiny static server auto-reloads the browser
# tab once the gallery HTML changes. See docs/reference/developer-docs.md's
# "Live Preview Loop" section.
set -euo pipefail
cd "$(dirname "$0")/.."

PORT="${1:-8090}"
REPORT_DIR="samples/ui-showcase/build/reports/ui-previews"

./gradlew :samples:ui-showcase:desktopTest --tests "*UiShowcasePreviewDocsTest*" --continuous -q &
GRADLE_PID=$!
trap 'kill "$GRADLE_PID" 2>/dev/null || true' EXIT

exec python3 tools/ui_preview_server.py "$REPORT_DIR" "$PORT"
