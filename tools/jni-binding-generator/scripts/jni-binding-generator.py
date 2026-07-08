#!/usr/bin/env python3
"""
JNI Binding Generator

Generates C++ JNI boilerplate from Kotlin ``external fun`` declarations.

Usage:
    python3 jni-binding-generator.py --kotlin-source <path> --output <path>
    python3 jni-binding-generator.py --kotlin-source <path> --output <path> --dry-run
    python3 jni-binding-generator.py --kotlin-source <path> --output <path> --check

Exit codes: 0 ok, 1 usage/no input, 2 parse or type error, 3 drift (--check).
"""

from __future__ import annotations

import sys
from pathlib import Path

# Make the scripts/ directory importable so the submodules (_types, _parser, etc.) resolve.
_HERE = Path(__file__).resolve().parent
if str(_HERE) not in sys.path:
    sys.path.insert(0, str(_HERE))

# Re-export everything tests and callers expect on this module.
# ``as name`` is the PEP 484 convention for intentional re-exports
# (suppresses F401 "unused import" without noqa comments).
from _driver import collect_kotlin_files as collect_kotlin_files
from _driver import main as main
from _driver import parse_args as parse_args
from _driver import run as run
from _generator import generate_file as generate_file
from _generator import generate_function as generate_function
from _generator import generate_test_file as generate_test_file
from _generator import output_basename as output_basename
from _generator import test_output_basename as test_output_basename
from _ios import generate_ios_cinterop_files as generate_ios_cinterop_files
from _kotlin_gen import KotlinFun as KotlinFun
from _kotlin_gen import KotlinParam as KotlinParam
from _kotlin_gen import _header_to_object_name as _header_to_object_name
from _kotlin_gen import generate_kotlin_from_header as generate_kotlin_from_header
from _kotlin_gen import generate_kotlin_stubs as generate_kotlin_stubs
from _kotlin_gen import parse_c_header as parse_c_header
from _kotlin_gen import parse_c_header_file as parse_c_header_file
from _models import EXIT_DRIFT as EXIT_DRIFT
from _models import EXIT_OK as EXIT_OK
from _models import EXIT_PARSE as EXIT_PARSE
from _models import EXIT_USAGE as EXIT_USAGE
from _models import ExternalFunction as ExternalFunction
from _models import KotlinField as KotlinField
from _models import KotlinStruct as KotlinStruct
from _models import Param as Param
from _models import ParsedFile as ParsedFile
from _parser import collect_enum_names as collect_enum_names
from _parser import collect_enum_packages as collect_enum_packages
from _parser import collect_struct_types as collect_struct_types
from _parser import collect_typealiases as collect_typealiases
from _parser import jni_function_name as jni_function_name
from _parser import mangle as mangle
from _parser import parse_kotlin_file as parse_kotlin_file
from _parser import parse_kotlin_source as parse_kotlin_source
from _parser import parse_kotlin_source_multi as parse_kotlin_source_multi
from _scorer import ScoreCard as ScoreCard
from _scorer import print_scorecard as print_scorecard
from _scorer import score as score
from _struct_gen import collect_needed_structs as collect_needed_structs
from _struct_gen import generate_all_struct_blocks as generate_all_struct_blocks
from _struct_gen import generate_struct_block as generate_struct_block
from _types import _MAKE_HELPER_MAP as _MAKE_HELPER_MAP
from _types import RETURN_MAP as RETURN_MAP
from _types import TYPE_MAP as TYPE_MAP
from _types import TypeInfo as TypeInfo
from _types import UnknownTypeError as UnknownTypeError
from _types import load_type_map as load_type_map
from _types import map_param_type as map_param_type
from _types import map_return_type as map_return_type

if __name__ == "__main__":
    sys.exit(main())
