#!/usr/bin/env bash
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
#
# Pins a real, reproducible checkout of shadcn-ui/ui into third_party/shadcn-ui-ref/ (gitignored)
# so shadcn parity/reference tooling (tools/extract_shadcn_tokens.py, ShadcnParityScreenshotTest,
# the *ShadcnReferenceToken* tests) has real ground truth instead of an ephemeral /tmp clone.
# See docs/reference/shadcn-reference-pipeline.md.
#
# Idempotent: re-running always resets the checkout to PINNED_SHA below, whether that means
# creating the clone for the first time or moving an existing one forward/back to a bumped SHA.
set -euo pipefail
cd "$(dirname "$0")/.."

# Bump this to move the pinned reference forward -- see docs/reference/shadcn-reference-pipeline.md
# for the full bump procedure. Pinned 2026-08-08: HEAD of shadcn-ui/ui's `main` branch that day.
PINNED_SHA="6261bd89f72d794aea491482cc2acfd8dc3d63e2"
REPO_URL="https://github.com/shadcn-ui/ui.git"
DEST="third_party/shadcn-ui-ref"

if [ ! -d "$DEST/.git" ]; then
    rm -rf "$DEST"
    mkdir -p "$DEST"
    git -C "$DEST" init -q
    git -C "$DEST" remote add origin "$REPO_URL"
fi

git -C "$DEST" fetch --depth 1 origin "$PINNED_SHA"
git -C "$DEST" checkout -q --detach FETCH_HEAD

ACTUAL_SHA="$(git -C "$DEST" rev-parse HEAD)"
if [ "$ACTUAL_SHA" != "$PINNED_SHA" ]; then
    echo "error: checked out $ACTUAL_SHA, expected $PINNED_SHA" >&2
    exit 1
fi

echo "shadcn-ui/ui pinned at $PINNED_SHA -> $DEST"
