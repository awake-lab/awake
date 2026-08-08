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
if ! python3 "$AUDIT_SCRIPT" "$REPO_ROOT"; then
  echo ""
  echo "Architecture audit found issues (warn-only — not blocking this commit)."
  echo "Run: python3 .claude/skills/kmp-audit/scripts/audit_project.py ."
  # ponytail: warn-only until the pre-existing findings backlog is cleared,
  # then drop this line so a non-zero audit blocks the commit again.
fi

exit 0
