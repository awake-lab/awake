#!/usr/bin/env python3
import json
import sys
import tempfile
import unittest
from pathlib import Path

from PIL import Image

# Keep the standalone helper importable both as `python3 tools/test_...py` and
# through unittest discovery from the repository root. The tools directory is
# intentionally a script collection rather than a Python package.
sys.path.insert(0, str(Path(__file__).resolve().parent))

from compare_component_crops import load_semantic_node, run_case, semantic_crop_box, semantic_crop_box_for_nodes


class CompareComponentCropsTest(unittest.TestCase):
    def test_semantic_bounds_scale_from_logical_to_raster_and_add_padding(self) -> None:
        document = {"width": 100, "height": 80}
        node = {"id": "button", "bounds": {"x": 10, "y": 5, "w": 20, "h": 12}}
        self.assertEqual(
            semantic_crop_box(document, node, (200, 160), padding=2),
            (16, 6, 64, 38),
        )

    def test_duplicate_semantic_ids_are_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "scene.json"
            path.write_text(json.dumps({"semantics": [{"id": "button"}, {"id": "button"}]}))
            with self.assertRaisesRegex(ValueError, "duplicated"):
                load_semantic_node(path, "button")

    def test_grouped_nodes_crop_the_union_once(self) -> None:
        document = {"width": 100, "height": 80}
        nodes = [
            {"id": "one", "bounds": {"x": 10, "y": 5, "w": 20, "h": 12}},
            {"id": "two", "bounds": {"x": 40, "y": 8, "w": 10, "h": 8}},
        ]
        self.assertEqual(
            semantic_crop_box_for_nodes(document, nodes, (200, 160), padding=2),
            (16, 6, 104, 38),
        )

    def test_run_case_writes_crop_diff_and_metrics(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            awake_path = root / "awake.png"
            reference_path = root / "reference.png"
            semantic_path = root / "awake.json"
            Image.new("RGBA", (20, 20), (255, 255, 255, 255)).save(awake_path)
            Image.new("RGBA", (8, 8), (255, 255, 255, 255)).save(reference_path)
            semantic_path.write_text(
                json.dumps(
                    {
                        "width": 10,
                        "height": 10,
                        "semantics": [{"id": "button", "bounds": {"x": 1, "y": 1, "w": 4, "h": 4}}],
                    }
                )
            )
            result = run_case(
                {
                    "name": "button",
                    "awakePng": str(awake_path),
                    "semanticJson": str(semantic_path),
                    "nodeId": "button",
                    "referencePng": str(reference_path),
                    "maxMismatchPct": 0,
                },
                root / "out",
                default_max_mismatch=0,
                default_tolerance=2,
            )
            self.assertEqual(result["crop_box"], [2, 2, 10, 10])
            self.assertEqual(result["status"], "OK")
            self.assertTrue((root / "out/button_awake.png").exists())
            self.assertTrue((root / "out/button_diff.png").exists())
            self.assertTrue((root / "out/button.json").exists())


if __name__ == "__main__":
    unittest.main()
