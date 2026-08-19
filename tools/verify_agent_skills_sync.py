#!/usr/bin/env python3
"""
Verify synchronization of Awake's agents, domain skills, catalog documentation, and entrypoints.

Checks:
1. Agent Catalog Parity: Every agent file in skills/awake/agents/ is listed in docs/reference/agent-catalog.md.
2. Frontmatter Validity: Every agent has valid YAML frontmatter with name, description, tools, and model.
3. Domain Skills Integrity: Every cited domain skill exists in skills/ with valid SKILL.md frontmatter.
4. Entrypoint Parity: AGENTS.md, CLAUDE.md, GEMINI.md, and .claude/AGENTS.md have matching mandatory skill lists.
5. Symlink Resolution: .claude/agents and .agents/skills resolve to valid targets.

Exit code 0 on success, 1 on validation error.
"""

import sys
import re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
AGENTS_DIR = REPO_ROOT / "skills" / "awake" / "agents"
SKILLS_DIR = REPO_ROOT / "skills"
CATALOG_PATH = REPO_ROOT / "docs" / "reference" / "agent-catalog.md"
ENTRYPOINTS = [
    REPO_ROOT / "AGENTS.md",
    REPO_ROOT / "CLAUDE.md",
    REPO_ROOT / "GEMINI.md",
    REPO_ROOT / ".claude" / "AGENTS.md",
]


def check_agent_frontmatter(agent_path: Path) -> list[str]:
    errors = []
    content = agent_path.read_text(encoding="utf-8")
    if not content.startswith("---"):
        return [f"{agent_path.name}: Missing YAML frontmatter start ('---')"]

    parts = content.split("---", 2)
    if len(parts) < 3:
        return [f"{agent_path.name}: Malformed YAML frontmatter"]

    frontmatter = parts[1]
    expected_name = agent_path.stem

    name_match = re.search(r"^name:\s*(.+)$", frontmatter, re.MULTILINE)
    if not name_match or name_match.group(1).strip() != expected_name:
        errors.append(f"{agent_path.name}: 'name:' in frontmatter should be '{expected_name}'")

    if not re.search(r"^description:", frontmatter, re.MULTILINE):
        errors.append(f"{agent_path.name}: Missing 'description:' in frontmatter")

    if not re.search(r"^tools:\s*(.+)$", frontmatter, re.MULTILINE):
        errors.append(f"{agent_path.name}: Missing 'tools:' in frontmatter")

    if not re.search(r"^model:\s*(.+)$", frontmatter, re.MULTILINE):
        errors.append(f"{agent_path.name}: Missing 'model:' in frontmatter")

    return errors


def check_catalog_parity(disk_agents: set[str]) -> list[str]:
    errors = []
    if not CATALOG_PATH.exists():
        return [f"Catalog file not found: {CATALOG_PATH}"]

    catalog_content = CATALOG_PATH.read_text(encoding="utf-8")

    # Check file map links
    catalog_files = set(re.findall(r"\[awake-[\w-]+\.md\]", catalog_content))
    catalog_agent_names = {f.strip("[]").replace(".md", "") for f in catalog_files}

    missing_in_catalog = disk_agents - catalog_agent_names
    extra_in_catalog = catalog_agent_names - disk_agents

    if missing_in_catalog:
        errors.append(f"Agents on disk but missing from catalog Current File Map: {missing_in_catalog}")
    if extra_in_catalog:
        errors.append(f"Agents in catalog Current File Map but not on disk: {extra_in_catalog}")

    return errors


def check_domain_skills() -> list[str]:
    errors = []
    mandatory_skills = [
        "awake-core-math",
        "awake-ecs-authoring",
        "awake-ecs-scene-runtime",
        "awake-render-pipeline",
        "awake-render-vulkan",
        "awake-render-webgpu",
        "awake-physics-jolt",
        "awake-ui-authoring",
        "awake-ui-shadcn-consuming",
        "awake-ui-shadcn-styling",
        "awake-ui-icons",
        "awake-ui-verification",
    ]

    for skill_name in mandatory_skills:
        skill_file = SKILLS_DIR / skill_name / "SKILL.md"
        if not skill_file.exists():
            errors.append(f"Mandatory skill missing: {skill_file}")
            continue

        content = skill_file.read_text(encoding="utf-8")
        if not content.startswith("---"):
            errors.append(f"{skill_name}/SKILL.md: Missing YAML frontmatter")

    return errors


def check_entrypoint_skills() -> list[str]:
    errors = []
    expected_skills = [
        "skills/awake-core-math/SKILL.md",
        "skills/awake-ecs-authoring/SKILL.md",
        "skills/awake-ecs-scene-runtime/SKILL.md",
        "skills/awake-render-pipeline/SKILL.md",
        "skills/awake-render-vulkan/SKILL.md",
        "skills/awake-render-webgpu/SKILL.md",
        "skills/awake-physics-jolt/SKILL.md",
        "skills/awake-ui-authoring/SKILL.md",
        "skills/awake-ui-shadcn-consuming/SKILL.md",
        "skills/awake-ui-shadcn-styling/SKILL.md",
        "skills/awake-ui-icons/SKILL.md",
        "skills/awake-ui-verification/SKILL.md",
    ]

    for ep in ENTRYPOINTS:
        if not ep.exists():
            errors.append(f"Entrypoint file missing: {ep}")
            continue

        content = ep.read_text(encoding="utf-8")
        for skill in expected_skills:
            if skill not in content:
                errors.append(f"{ep.name}: Missing reference to '{skill}'")

    return errors


def check_symlinks() -> list[str]:
    errors = []
    claude_agents = REPO_ROOT / ".claude" / "agents"
    if not claude_agents.exists():
        errors.append(".claude/agents symlink or directory does not exist")
    elif claude_agents.is_symlink() and not claude_agents.resolve().exists():
        errors.append(f".claude/agents points to non-existent target: {claude_agents.readlink()}")

    agents_awake = REPO_ROOT / ".agents" / "skills" / "awake"
    if not agents_awake.exists():
        errors.append(".agents/skills/awake does not exist")

    return errors


def check_module_readmes() -> list[str]:
    errors = []
    primary_modules = [
        REPO_ROOT / "awake" / "core",
        REPO_ROOT / "awake" / "core" / "geometry",
        REPO_ROOT / "awake" / "core" / "animation",
        REPO_ROOT / "awake" / "asset" / "gltf",
        REPO_ROOT / "awake" / "asset" / "mesh-optimizer",
        REPO_ROOT / "awake" / "asset" / "shaders",
        REPO_ROOT / "awake" / "ecs",
        REPO_ROOT / "awake" / "scene",
        REPO_ROOT / "awake" / "scene" / "authoring",
        REPO_ROOT / "awake" / "scene" / "rendering",
        REPO_ROOT / "awake" / "engine" / "render" / "contract",
        REPO_ROOT / "awake" / "engine" / "render" / "passes",
        REPO_ROOT / "awake" / "ui",
        REPO_ROOT / "awake" / "ui" / "ui-core",
        REPO_ROOT / "awake" / "ui" / "headless",
        REPO_ROOT / "awake" / "ui" / "designsystem",
        REPO_ROOT / "awake" / "ui" / "text",
        REPO_ROOT / "awake" / "engine" / "game",
        REPO_ROOT / "awake" / "engine" / "game-authoring",
        REPO_ROOT / "awake" / "engine" / "app",
        REPO_ROOT / "awake" / "backend" / "vulkan",
        REPO_ROOT / "awake" / "backend" / "webgpu",
        REPO_ROOT / "awake" / "physics" / "api",
        REPO_ROOT / "awake" / "backend" / "jolt",
    ]

    for mod_path in primary_modules:
        readme = mod_path / "README.md"
        if not readme.exists():
            errors.append(f"Module missing README.md: {mod_path.relative_to(REPO_ROOT)}")
        elif len(readme.read_text(encoding="utf-8").strip()) < 50:
            errors.append(f"Module README.md is empty or too short: {mod_path.relative_to(REPO_ROOT)}")

    return errors


def main():
    print("Verifying Awake Agents, Skills & Module Docs Synchronization...")
    all_errors = []

    if not AGENTS_DIR.exists():
        print(f"ERROR: Agents directory not found at {AGENTS_DIR}", file=sys.stderr)
        sys.exit(1)

    disk_agent_files = list(AGENTS_DIR.glob("awake-*.md"))
    disk_agents = {f.stem for f in disk_agent_files}

    print(f"Found {len(disk_agents)} agent files in {AGENTS_DIR.relative_to(REPO_ROOT)}")

    # 1. Frontmatter check
    for agent_file in disk_agent_files:
        all_errors.extend(check_agent_frontmatter(agent_file))

    # 2. Catalog parity
    all_errors.extend(check_catalog_parity(disk_agents))

    # 3. Domain skills check
    all_errors.extend(check_domain_skills())

    # 4. Entrypoint parity check
    all_errors.extend(check_entrypoint_skills())

    # 5. Symlinks check
    all_errors.extend(check_symlinks())

    # 6. Module README coverage check
    all_errors.extend(check_module_readmes())

    if all_errors:
        print("\n❌ Verification FAILED with the following errors:", file=sys.stderr)
        for err in all_errors:
            print(f"  - {err}", file=sys.stderr)
        sys.exit(1)

    print("\n✅ All 12 agents, 12 domain skills, 24 module READMEs, and entrypoints are fully synchronized!")
    sys.exit(0)


if __name__ == "__main__":
    main()
