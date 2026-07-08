import importlib.util
import pathlib
import sys

_spec = importlib.util.spec_from_file_location(
    "jni_binding_generator._impl",
    pathlib.Path(__file__).parent / "jni-binding-generator.py",
)
_mod = importlib.util.module_from_spec(_spec)
sys.modules[_spec.name] = _mod  # register before exec so dataclasses can resolve __module__
_spec.loader.exec_module(_mod)  # type: ignore[union-attr]
main = _mod.main
