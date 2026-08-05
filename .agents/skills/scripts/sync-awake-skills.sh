#!/usr/bin/env bash
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
#
# Syncs every repo-local awake-* skill from .agents/skills/ (source of truth, tracked/edited
# here) into skills/ (real, byte-identical copy -- matches skills/awake's own existing
# convention, not a symlink) and makes sure .claude/skills/<name> symlinks to skills/<name>
# (not directly into .agents/skills/) for each one.
#
# Run this after editing anything under .agents/skills/awake* -- skills/ and .claude/skills/
# are both deploy artifacts, never edit them directly.
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
cd "$repo_root"

for source_dir in .agents/skills/awake*; do
    [ -d "$source_dir" ] || continue
    name="$(basename "$source_dir")"
    target_dir="skills/$name"

    mkdir -p "$target_dir"
    rsync -a --delete "$source_dir/" "$target_dir/"
    echo "synced $source_dir -> $target_dir"

    link=".claude/skills/$name"
    if [ -L "$link" ]; then
        rm "$link"
    elif [ -e "$link" ]; then
        echo "refusing to overwrite non-symlink $link" >&2
        exit 1
    fi
    ln -s "../../skills/$name" "$link"
    echo "linked $link -> ../../skills/$name"
done
