#!/usr/bin/env bash
# Pre-commit hook: runs the kmp-audit architecture check on staged Kotlin files.
# Wired via .githooks/pre-commit (core.hooksPath), alongside the existing
# commit-msg and pre-push hooks in the same directory.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
AUDIT_SCRIPT="$REPO_ROOT/.claude/skills/kmp-audit/scripts/audit_project.py"

STAGED_KT="$(git diff --cached --name-only | grep -E '\.(kt|kts)$' || true)"

if [[ -z "$STAGED_KT" ]]; then
  exit 0
fi

if [[ ! -f "$AUDIT_SCRIPT" ]]; then
  echo "audit_project.py not found at $AUDIT_SCRIPT — skipping architecture audit." >&2
  exit 0
fi

echo "Running architecture audit on staged Kotlin files..."
# audit_project.py only accepts a project root, so this is a full-repo scan; cap it so a
# slow scan can never hang the commit (perl alarm because macOS has no coreutils timeout).
if ! perl -e 'alarm 20; exec @ARGV' python3 "$AUDIT_SCRIPT" "$REPO_ROOT"; then
  echo ""
  echo "Architecture audit found issues or timed out (warn-only — not blocking this commit)."
  echo "Run: python3 .claude/skills/kmp-audit/scripts/audit_project.py ."
  # ponytail: warn-only until the pre-existing findings backlog is cleared,
  # then drop this line so a non-zero audit blocks the commit again.
fi

exit 0
