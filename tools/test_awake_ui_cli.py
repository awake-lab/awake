#!/usr/bin/env python3
import importlib.util
import json
import sys
import tempfile
import unittest
from pathlib import Path

from PIL import Image


SCRIPT = Path(__file__).resolve().parent.parent / "scripts" / "awake_ui.py"
SPEC = importlib.util.spec_from_file_location("awake_ui", SCRIPT)
awake_ui = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
sys.modules[SPEC.name] = awake_ui
SPEC.loader.exec_module(awake_ui)


class AwakeUiCliTest(unittest.TestCase):
    def test_component_alias_and_reference_state_mapping_are_explicit(self) -> None:
        radio = awake_ui.component_spec("radio-group")
        self.assertEqual(radio.canonical_name, "radio")
        self.assertEqual(awake_ui.resolve_reference_cases(radio, ("rest",)), ("radio-group-states",))

    def test_unknown_state_fails_instead_of_falling_back_to_rest(self) -> None:
        with self.assertRaisesRegex(SystemExit, "no official fixture for state 'hover'"):
            awake_ui.resolve_reference_cases(awake_ui.component_spec("button"), ("hover",))

    def test_parity_mapping_is_not_guessed_from_a_filename(self) -> None:
        progress_cases = awake_ui.matching_parity_cases(awake_ui.component_spec("progress"), "light")
        self.assertEqual([case["name"] for case in progress_cases], ["progress-states-light"])

    def test_debug_overlay_uses_semantic_bounds_lanes(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            png = root / "awake.png"
            report = root / "awake.json"
            output = root / "debug.png"
            Image.new("RGBA", (20, 20), (255, 255, 255, 255)).save(png)
            report.write_text(json.dumps({
                "width": 10,
                "height": 10,
                "semantics": [{
                    "id": "button",
                    "bounds": {"x": 1, "y": 1, "w": 5, "h": 5},
                    "contentBounds": {"x": 2, "y": 2, "w": 3, "h": 3},
                    "clippedBounds": {"x": 3, "y": 3, "w": 1, "h": 1},
                }],
            }))
            original_root = awake_ui.REPO_ROOT
            try:
                awake_ui.REPO_ROOT = root
                result = awake_ui.make_debug_overlay(
                    {"awakePng": "awake.png", "semanticJson": "awake.json"}, output,
                )
            finally:
                awake_ui.REPO_ROOT = original_root
            self.assertEqual(result, output)
            self.assertTrue(output.exists())
            self.assertNotEqual(Image.open(output).getpixel((2, 2)), (255, 255, 255, 255))


if __name__ == "__main__":
    unittest.main()
