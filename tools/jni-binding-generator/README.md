# jni-binding-generator (vendored)

Vendored copy of [jni-binding-generator](https://github.com/ronjunevaldoz/jni-binding-generator)
**v1.6.10**, used to generate JNI marshalling C++ from `external fun` declarations in
`awake-vulkan`. See [docs/decisions/D10-codegen-derisk-findings.md](../../docs/decisions/D10-codegen-derisk-findings.md)
for why this tool was chosen over the legacy `awake-vulkan-generator`, and for the full
history of gaps found and fixed while wiring it into this project (v1.6.8 → v1.6.10).

Licensed Apache 2.0 (same as Awake) — `LICENSE` in this directory is the upstream tool's
license, not a modification to Awake's own license.

## Updating

The generator has no PyPI package; its own integration docs assume the `scripts/` directory
is copied wholesale into the consuming project. To pick up a newer version:

```bash
cp /path/to/jni-binding-generator/scripts/{__init__.py,_*.py,jni-binding-generator.py,jni-utils.h} \
   tools/jni-binding-generator/scripts/
```

Then re-run `./gradlew generateJniBindings` and diff the output before committing — a version
bump can change generated code shape (see that repo's CHANGELOG.md for what changed).

## Usage

See the `generateJniBindings` Gradle task in
[awake-vulkan/android-native/build.gradle.kts](../../awake-vulkan/android-native/build.gradle.kts).
