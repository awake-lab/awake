#!/usr/bin/env python3
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
"""SVG -> Awake UiImageVector Kotlin codegen.

Parses an SVG file's <path> elements and emits a `val <name>: UiImageVector =
uiImageVector(...) { path { ... } }` Kotlin block using only the commands
Awake's UiPathBuilder supports: moveTo / lineTo / quadTo / cubicTo / close.
Curves are preserved exactly -- SVG arcs are converted to cubic Beziers
(W3C endpoint->center parameterization, <=90-degree segments), never
flattened to line segments.

Usage:
    python3 tools/svg_to_ui_image_vector.py icon.svg --name chevronDown \
        --dp 16 --source "heroicons chevron-down (20/solid)"
    python3 tools/svg_to_ui_image_vector.py --self-test

Engine limits enforced here:
- `fill-rule=evenodd` with NESTED subpaths (holes -- e.g. Heroicons' clock/camera)
  is supported and emitted as `path(fillRule = UiFillRule.EvenOdd)`; genuinely
  CROSSING subpaths are rejected since the engine's containment-based hole
  grouping cannot represent them.
- Stroke-only (outline-tier) SVGs are supported: `stroke-width`/`stroke-linecap`/
  `stroke-linejoin` are resolved through ancestors (Heroicons' outline tier
  declares them on the <svg> root) and emitted as `path(stroke = UiStroke(...))`.
  A <path> with BOTH a fill and a stroke is rejected -- one UiVectorPath draws
  one or the other, not both.
- `transform` attributes and non-path shape elements are rejected; flatten
  them to plain paths first (e.g. with picosvg).
"""

from __future__ import annotations

import argparse
import math
import os
import re
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET

SVG_NS = "{http://www.w3.org/2000/svg}"
NUMBER_RE = re.compile(r"[+-]?(?:\d*\.\d+|\d+\.?)(?:[eE][+-]?\d+)?")
WSP_RE = re.compile(r"[\s,]*")


class PathScanner:
    """Cursor over an SVG path `d` string. Arc flags must be read as single
    chars (heroicons-era data concatenates them: `a.75.75 0 011.06.02`)."""

    def __init__(self, d: str):
        self.d = d
        self.pos = 0

    def skip_wsp(self) -> None:
        self.pos = WSP_RE.match(self.d, self.pos).end()

    def at_end(self) -> bool:
        self.skip_wsp()
        return self.pos >= len(self.d)

    def peek(self) -> str:
        self.skip_wsp()
        return self.d[self.pos] if self.pos < len(self.d) else ""

    def read_command(self) -> str:
        self.skip_wsp()
        c = self.d[self.pos]
        self.pos += 1
        return c

    def read_number(self) -> float:
        self.skip_wsp()
        m = NUMBER_RE.match(self.d, self.pos)
        if not m:
            raise ValueError(f"expected number at {self.pos}: ...{self.d[self.pos:self.pos + 20]!r}")
        self.pos = m.end()
        return float(m.group())

    def read_flag(self) -> int:
        self.skip_wsp()
        c = self.d[self.pos]
        if c not in "01":
            raise ValueError(f"expected arc flag at {self.pos}: {c!r}")
        self.pos += 1
        return int(c)

    def has_number_next(self) -> bool:
        self.skip_wsp()
        return bool(NUMBER_RE.match(self.d, self.pos))


def arc_to_cubics(x1, y1, rx, ry, phi_deg, large, sweep, x2, y2):
    """One SVG elliptical arc -> list of ('C', (c1x, c1y, c2x, c2y, x, y))."""
    if rx == 0 or ry == 0 or (x1 == x2 and y1 == y2):
        return [("L", (x2, y2))]
    phi = math.radians(phi_deg)
    cp, sp = math.cos(phi), math.sin(phi)
    dx2, dy2 = (x1 - x2) / 2.0, (y1 - y2) / 2.0
    x1p = cp * dx2 + sp * dy2
    y1p = -sp * dx2 + cp * dy2
    rx, ry = abs(rx), abs(ry)
    lam = x1p * x1p / (rx * rx) + y1p * y1p / (ry * ry)
    if lam > 1:
        s = math.sqrt(lam)
        rx *= s
        ry *= s
    num = rx * rx * ry * ry - rx * rx * y1p * y1p - ry * ry * x1p * x1p
    den = rx * rx * y1p * y1p + ry * ry * x1p * x1p
    co = math.sqrt(max(num, 0.0) / den) if den else 0.0
    if large == sweep:
        co = -co
    cxp = co * rx * y1p / ry
    cyp = -co * ry * x1p / rx
    cx = cp * cxp - sp * cyp + (x1 + x2) / 2.0
    cy = sp * cxp + cp * cyp + (y1 + y2) / 2.0

    def angle(ux, uy, vx, vy):
        d = math.hypot(ux, uy) * math.hypot(vx, vy)
        c = max(-1.0, min(1.0, (ux * vx + uy * vy) / d))
        a = math.acos(c)
        return -a if ux * vy - uy * vx < 0 else a

    th1 = angle(1.0, 0.0, (x1p - cxp) / rx, (y1p - cyp) / ry)
    dth = angle((x1p - cxp) / rx, (y1p - cyp) / ry, (-x1p - cxp) / rx, (-y1p - cyp) / ry)
    if not sweep and dth > 0:
        dth -= 2 * math.pi
    elif sweep and dth < 0:
        dth += 2 * math.pi

    n = max(1, int(math.ceil(abs(dth) / (math.pi / 2.0))))
    delta = dth / n
    k = 4.0 / 3.0 * math.tan(delta / 4.0)

    def point(t):
        return (
            cx + rx * math.cos(t) * cp - ry * math.sin(t) * sp,
            cy + rx * math.cos(t) * sp + ry * math.sin(t) * cp,
        )

    def deriv(t):
        return (
            -rx * math.sin(t) * cp - ry * math.cos(t) * sp,
            -rx * math.sin(t) * sp + ry * math.cos(t) * cp,
        )

    out = []
    for i in range(n):
        t1 = th1 + i * delta
        t2 = t1 + delta
        p1, p2 = point(t1), point(t2)
        d1, d2 = deriv(t1), deriv(t2)
        end = (x2, y2) if i == n - 1 else p2  # exact endpoint, no fp drift
        out.append(("C", (
            p1[0] + k * d1[0], p1[1] + k * d1[1],
            p2[0] - k * d2[0], p2[1] - k * d2[1],
            end[0], end[1],
        )))
    return out


def parse_path_d(d: str):
    """Parse a `d` string into absolute commands normalized to M/L/Q/C/Z."""
    s = PathScanner(d)
    out = []
    cx = cy = 0.0  # current point
    sx = sy = 0.0  # subpath start
    prev_cmd = ""
    prev_c2 = None  # last cubic control2 (for S)
    prev_q1 = None  # last quad control (for T)

    while not s.at_end():
        c = s.peek()
        if c.isalpha():
            cmd = s.read_command()
        else:
            # implicit repetition: M -> L, m -> l, others repeat themselves
            if prev_cmd in ("M", "m"):
                cmd = "L" if prev_cmd == "M" else "l"
            elif prev_cmd:
                cmd = prev_cmd
            else:
                raise ValueError("path data starts with a number, not a command")
        rel = cmd.islower()
        u = cmd.upper()

        if u == "M":
            x, y = s.read_number(), s.read_number()
            if rel:
                x, y = cx + x, cy + y
            out.append(("M", (x, y)))
            cx, cy = x, y
            sx, sy = x, y
            prev_c2 = prev_q1 = None
        elif u == "L":
            x, y = s.read_number(), s.read_number()
            if rel:
                x, y = cx + x, cy + y
            out.append(("L", (x, y)))
            cx, cy = x, y
            prev_c2 = prev_q1 = None
        elif u == "H":
            x = s.read_number()
            if rel:
                x = cx + x
            out.append(("L", (x, cy)))
            cx = x
            prev_c2 = prev_q1 = None
        elif u == "V":
            y = s.read_number()
            if rel:
                y = cy + y
            out.append(("L", (cx, y)))
            cy = y
            prev_c2 = prev_q1 = None
        elif u == "C":
            c1x, c1y = s.read_number(), s.read_number()
            c2x, c2y = s.read_number(), s.read_number()
            x, y = s.read_number(), s.read_number()
            if rel:
                c1x, c1y = cx + c1x, cy + c1y
                c2x, c2y = cx + c2x, cy + c2y
                x, y = cx + x, cy + y
            out.append(("C", (c1x, c1y, c2x, c2y, x, y)))
            cx, cy = x, y
            prev_c2, prev_q1 = (c2x, c2y), None
        elif u == "S":
            c2x, c2y = s.read_number(), s.read_number()
            x, y = s.read_number(), s.read_number()
            if rel:
                c2x, c2y = cx + c2x, cy + c2y
                x, y = cx + x, cy + y
            if prev_c2 is not None:
                c1x, c1y = 2 * cx - prev_c2[0], 2 * cy - prev_c2[1]
            else:
                c1x, c1y = cx, cy
            out.append(("C", (c1x, c1y, c2x, c2y, x, y)))
            cx, cy = x, y
            prev_c2, prev_q1 = (c2x, c2y), None
        elif u == "Q":
            qx, qy = s.read_number(), s.read_number()
            x, y = s.read_number(), s.read_number()
            if rel:
                qx, qy = cx + qx, cy + qy
                x, y = cx + x, cy + y
            out.append(("Q", (qx, qy, x, y)))
            cx, cy = x, y
            prev_q1, prev_c2 = (qx, qy), None
        elif u == "T":
            x, y = s.read_number(), s.read_number()
            if rel:
                x, y = cx + x, cy + y
            if prev_q1 is not None:
                qx, qy = 2 * cx - prev_q1[0], 2 * cy - prev_q1[1]
            else:
                qx, qy = cx, cy
            out.append(("Q", (qx, qy, x, y)))
            cx, cy = x, y
            prev_q1, prev_c2 = (qx, qy), None
        elif u == "A":
            rx, ry = s.read_number(), s.read_number()
            rot = s.read_number()
            large, sweep = s.read_flag(), s.read_flag()
            x, y = s.read_number(), s.read_number()
            if rel:
                x, y = cx + x, cy + y
            out.extend(arc_to_cubics(cx, cy, rx, ry, rot, large, sweep, x, y))
            cx, cy = x, y
            prev_c2 = prev_q1 = None
        elif u == "Z":
            out.append(("Z", ()))
            cx, cy = sx, sy
            prev_c2 = prev_q1 = None
        else:
            raise ValueError(f"unsupported path command {cmd!r}")
        prev_cmd = cmd
    return out


def split_subpaths(commands):
    subpaths = []
    current = []
    for cmd in commands:
        if cmd[0] == "M" and current:
            subpaths.append(current)
            current = []
        current.append(cmd)
    if current:
        subpaths.append(current)
    return subpaths


def subpath_bbox(subpath):
    xs, ys = [], []
    for op, args in subpath:
        for i in range(0, len(args), 2):
            xs.append(args[i])
            ys.append(args[i + 1])
    return (min(xs), min(ys), max(xs), max(ys))


def bboxes_overlap(a, b):
    return a[0] < b[2] and b[0] < a[2] and a[1] < b[3] and b[1] < a[3]


def subpath_endpoints(subpath):
    """Command endpoints as a polygon -- good enough for nested-vs-crossing checks."""
    pts = []
    for op, args in subpath:
        if op in ("M", "L"):
            pts.append((args[0], args[1]))
        elif op == "Q":
            pts.append((args[2], args[3]))
        elif op == "C":
            pts.append((args[4], args[5]))
    return pts


def point_in_polygon(x, y, polygon):
    inside = False
    j = len(polygon) - 1
    for i in range(len(polygon)):
        xi, yi = polygon[i]
        xj, yj = polygon[j]
        if (yi > y) != (yj > y) and x < (xj - xi) * (y - yi) / (yj - yi) + xi:
            inside = not inside
        j = i
    return inside


def parse_svg(path: str):
    tree = ET.parse(path)
    root = tree.getroot()
    view_box = root.get("viewBox")
    if not view_box:
        raise SystemExit("error: svg has no viewBox")
    vb = [float(v) for v in view_box.replace(",", " ").split()]
    if vb[0] != 0 or vb[1] != 0:
        raise SystemExit(f"error: viewBox with non-zero origin unsupported: {view_box}")
    viewport = (vb[2], vb[3])

    unsupported_shapes = ("rect", "circle", "ellipse", "line", "polyline", "polygon")
    for el in root.iter():
        tag = el.tag.removeprefix(SVG_NS)
        if el.get("transform"):
            raise SystemExit(f"error: <{tag} transform=...> unsupported -- flatten first (picosvg)")
        if tag in unsupported_shapes:
            raise SystemExit(f"error: <{tag}> unsupported -- convert shapes to paths first (picosvg)")

    # `fill` and `stroke` inherit. Heroicons' outline tier sets fill="none" and
    # stroke="currentColor" on the <svg> root and leaves the <path> carrying only
    # stroke-linecap/linejoin, so checking the path's own attributes alone accepts an
    # outline icon and fills its stroke centreline as a solid polygon -- silent garbage.
    # Resolve each attribute against the element's ancestors instead.
    parents = {child: parent for parent in root.iter() for child in parent}

    def inherited(el, name, default=None):
        node = el
        while node is not None:
            value = node.get(name)
            if value is not None:
                return value
            node = parents.get(node)
        return default

    paths = []
    for el in root.iter(f"{SVG_NS}path"):
        has_stroke = (inherited(el, "stroke") or "none") != "none"
        has_fill = inherited(el, "fill") != "none"
        if has_stroke and has_fill:
            raise SystemExit(
                "error: <path> has both a fill and a stroke -- UiVectorPath draws one or the "
                "other, not both. Split the SVG into two <path> elements."
            )
        if has_stroke:
            paths.append({
                "d": el.get("d"),
                "kind": "stroke",
                "width": float(inherited(el, "stroke-width", "1")),
                "cap": inherited(el, "stroke-linecap", "butt"),
                "join": inherited(el, "stroke-linejoin", "miter"),
            })
        elif has_fill:
            paths.append({"d": el.get("d"), "kind": "fill", "fill_rule": inherited(el, "fill-rule", "nonzero")})
    if not paths:
        raise SystemExit("error: no fillable or strokeable <path> elements found")
    return viewport, paths


def fmt(v: float) -> str:
    r = round(v, 4)
    if r == 0:
        r = 0.0
    s = f"{r:.4f}".rstrip("0").rstrip(".")
    return f"{s}f"


_CAP_NAMES = {"butt": "Butt", "round": "Round", "square": "Square"}
_JOIN_NAMES = {"miter": "Miter", "round": "Round", "bevel": "Bevel"}


def emit_kotlin(name, viewport, blocks, dp, source, indent="    ", level=2):
    """blocks: list of (subpaths, kind, extra) -- one path {} per SVG <path>. kind == "fill":
    extra is the kotlin fill-rule name or None. kind == "stroke": extra is (width, cap, join)."""
    i0 = indent * level
    i1 = indent * (level + 1)
    i2 = indent * (level + 2)
    lines = []
    if source:
        lines.append(f"{i0}/** {source}, generated by tools/svg_to_ui_image_vector.py -- do not hand-edit. */")
    lines.append(f"{i0}val {name}: UiImageVector = uiImageVector(")
    lines.append(f"{i1}defaultWidth = {fmt(dp)}.dp,")
    lines.append(f"{i1}defaultHeight = {fmt(dp)}.dp,")
    lines.append(f"{i1}viewportWidth = {fmt(viewport[0])},")
    lines.append(f"{i1}viewportHeight = {fmt(viewport[1])},")
    lines.append(f"{i0}) {{")
    for subpaths, kind, extra in blocks:
        if kind == "stroke":
            width, cap, join = extra
            cap_name = _CAP_NAMES.get(cap, "Butt")
            join_name = _JOIN_NAMES.get(join, "Miter")
            header = (
                f"{i1}path(stroke = UiStroke(width = {fmt(width)}.dp, "
                f"cap = UiStrokeCap.{cap_name}, join = UiStrokeJoin.{join_name})) {{"
            )
        else:
            fill_rule = extra
            header = f"{i1}path(fillRule = UiFillRule.{fill_rule}) {{" if fill_rule else f"{i1}path {{"
        lines.append(header)
        for subpath in subpaths:
            for op, args in subpath:
                a = [fmt(v) for v in args]
                if op == "M":
                    lines.append(f"{i2}moveTo({a[0]}, {a[1]})")
                elif op == "L":
                    lines.append(f"{i2}lineTo({a[0]}, {a[1]})")
                elif op == "Q":
                    lines.append(f"{i2}quadTo({a[0]}, {a[1]}, {a[2]}, {a[3]})")
                elif op == "C":
                    lines.append(f"{i2}cubicTo({a[0]}, {a[1]}, {a[2]}, {a[3]}, {a[4]}, {a[5]})")
                elif op == "Z":
                    lines.append(f"{i2}close()")
        lines.append(f"{i1}}}")
    lines.append(f"{i0}}}")
    return "\n".join(lines)


def classify_fill_rule(subpaths, fill_rule):
    """Returns 'EvenOdd' when the subpaths express holes, None for plain NonZero emission.
    Rejects crossing (overlapping but non-nested) evenodd subpaths."""
    if fill_rule != "evenodd" or len(subpaths) < 2:
        return None
    polygons = [subpath_endpoints(sp) for sp in subpaths]
    boxes = [subpath_bbox(sp) for sp in subpaths]
    has_nesting = False
    for a in range(len(boxes)):
        for b in range(a + 1, len(boxes)):
            if not bboxes_overlap(boxes[a], boxes[b]):
                continue
            def bbox_contains(outer, inner):
                return outer[0] <= inner[0] and outer[1] <= inner[1] and inner[2] <= outer[2] and inner[3] <= outer[3]

            a_in_b = bbox_contains(boxes[b], boxes[a]) and point_in_polygon(
                polygons[a][0][0], polygons[a][0][1], polygons[b]
            )
            b_in_a = bbox_contains(boxes[a], boxes[b]) and point_in_polygon(
                polygons[b][0][0], polygons[b][0][1], polygons[a]
            )
            if a_in_b or b_in_a:
                has_nesting = True
            else:
                raise SystemExit(
                    "error: evenodd path with CROSSING (non-nested) subpaths -- the engine's "
                    "containment-based hole grouping cannot represent this. Rework the source SVG."
                )
    return "EvenOdd" if has_nesting else None


def convert(svg_path, name, dp, source, indent_level):
    viewport, raw_paths = parse_svg(svg_path)
    blocks = []
    for entry in raw_paths:
        subpaths = split_subpaths(parse_path_d(entry["d"]))
        if entry["kind"] == "stroke":
            blocks.append((subpaths, "stroke", (entry["width"], entry["cap"], entry["join"])))
        else:
            blocks.append((subpaths, "fill", classify_fill_rule(subpaths, entry["fill_rule"])))
    return emit_kotlin(name, viewport, blocks, dp, source, level=indent_level)


def self_test():
    # implicit lineto after moveto, absolute + relative
    cmds = parse_path_d("M1 2 3 4")
    assert cmds == [("M", (1.0, 2.0)), ("L", (3.0, 4.0))], cmds
    cmds = parse_path_d("m1 2 3 4")
    assert cmds == [("M", (1.0, 2.0)), ("L", (4.0, 6.0))], cmds

    # H/V, close resets current point
    cmds = parse_path_d("M1 1H4V5ZL2 2")
    assert cmds[1] == ("L", (4.0, 1.0)) and cmds[2] == ("L", (4.0, 5.0)), cmds
    assert cmds[4] == ("L", (2.0, 2.0)), cmds

    # number run without separators: `.75.75`
    s = PathScanner(".75.75")
    assert s.read_number() == 0.75 and s.read_number() == 0.75

    # concatenated arc flags (old heroicons style): `a.75.75 0 011.06.02`
    cmds = parse_path_d("M5.23 7.21a.75.75 0 011.06.02")
    end = cmds[-1][1][-2:]
    assert abs(end[0] - 6.29) < 1e-9 and abs(end[1] - 7.23) < 1e-9, end
    assert all(op == "C" for op, _ in cmds[1:]), cmds

    # spaced arc flags (current heroicons style) hit the exact endpoint
    cmds = parse_path_d("M5.22 8.22a.75.75 0 0 1 1.06 0")
    end = cmds[-1][1][-2:]
    assert abs(end[0] - 6.28) < 1e-9 and abs(end[1] - 8.22) < 1e-9, end

    # S reflects previous cubic control2
    cmds = parse_path_d("M0 0C0 1 1 1 1 0S2 -1 2 0")
    assert cmds[2][1][:2] == (1.0, -1.0), cmds  # reflected control1

    # full circle from two arcs stays on radius 1 (sample each cubic midpoint)
    cmds = parse_path_d("M0 -1A1 1 0 1 1 0 1A1 1 0 1 1 0 -1Z")
    px, py = 0.0, -1.0
    for op, args in cmds[1:]:
        if op != "C":
            continue
        c1x, c1y, c2x, c2y, x, y = args
        mx = 0.125 * px + 0.375 * c1x + 0.375 * c2x + 0.125 * x
        my = 0.125 * py + 0.375 * c1y + 0.375 * c2y + 0.125 * y
        assert abs(math.hypot(mx, my) - 1.0) < 3e-4, (mx, my)
        px, py = x, y

    # subpath split + non-overlap check shape (squares-2x2 style)
    subs = split_subpaths(parse_path_d("M0 0h1v1h-1Z M2 0h1v1h-1Z"))
    assert len(subs) == 2 and not bboxes_overlap(subpath_bbox(subs[0]), subpath_bbox(subs[1]))
    assert classify_fill_rule(subs, "evenodd") is None  # disjoint: no holes, plain NonZero

    # nested evenodd subpaths = holes (clock/camera style) -> EvenOdd passthrough
    ring = split_subpaths(parse_path_d("M0 0h10v10h-10Z M2 2h6v6h-6Z"))
    assert classify_fill_rule(ring, "evenodd") == "EvenOdd"

    # crossing evenodd subpaths are rejected
    crossing = split_subpaths(parse_path_d("M0 0h6v6h-6Z M3 3h6v6h-6Z"))
    try:
        classify_fill_rule(crossing, "evenodd")
        raise AssertionError("crossing evenodd subpaths must be rejected")
    except SystemExit:
        pass

    # formatting: trimmed zeros, no negative zero
    assert fmt(4.0) == "4f" and fmt(4.80) == "4.8f" and fmt(-0.0) == "0f" and fmt(1.23456) == "1.2346f"

    # inherited fill/stroke: Heroicons' outline tier puts fill="none" stroke="currentColor"
    # (plus stroke-width/linecap/linejoin) on the <svg> root, so a path-only attribute check
    # would silently fill the stroke centreline instead of resolving it as a real stroke.
    import tempfile, os
    outline = (
        '<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" '
        'stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" '
        'stroke-linejoin="round" d="M4 10 L20 10"/></svg>'
    )
    handle, temp = tempfile.mkstemp(suffix=".svg")
    os.write(handle, outline.encode())
    os.close(handle)
    try:
        _, raw_paths = parse_svg(temp)
        assert len(raw_paths) == 1
        entry = raw_paths[0]
        assert entry["kind"] == "stroke"
        assert entry["width"] == 1.5
        assert entry["cap"] == "round" and entry["join"] == "round"
    finally:
        os.unlink(temp)

    # a <path> with both a fill and a stroke is still rejected -- not representable by one
    # UiVectorPath yet.
    both = (
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">'
        '<path fill="black" stroke="red" d="M0 0h10v10h-10Z"/></svg>'
    )
    handle, temp = tempfile.mkstemp(suffix=".svg")
    os.write(handle, both.encode())
    os.close(handle)
    try:
        parse_svg(temp)
        raise AssertionError("a path with both fill and stroke must be rejected")
    except SystemExit as failure:
        assert "fill and a stroke" in str(failure), failure
    finally:
        os.unlink(temp)

    print("self-test OK")


def preprocess_svg(svg_path: str, use_svgo: bool = False, expand_strokes: bool = False) -> str:
    """Preprocesses an SVG file via SVGO (flatten shapes/transforms) and/or picosvg (expand strokes to fill).
    Returns path to a temporary processed SVG file, or the original path if no processing was applied."""
    current_path = svg_path
    created_temps = []

    # 1. SVGO Optimization Pass
    if use_svgo:
        svgo_bin = shutil.which("svgo") or shutil.which("npx")
        if svgo_bin:
            handle, temp_file = tempfile.mkstemp(suffix=".svg")
            os.close(handle)
            created_temps.append(temp_file)
            cmd = [svgo_bin, "svgo", current_path, "-o", temp_file] if svgo_bin.endswith("npx") else [svgo_bin, current_path, "-o", temp_file]
            try:
                subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                current_path = temp_file
            except Exception:
                pass
        else:
            sys.stderr.write("warning: svgo/npx not found on PATH; skipping SVGO preprocessing\n")

    # 2. picosvg Stroke Expansion Pass
    if expand_strokes:
        try:
            from picosvg.svg import SVG
            with open(current_path, "r", encoding="utf-8") as f:
                svg_text = f.read()
            pico_xml = SVG.fromstring(svg_text).topicosvg().tostring()
            handle, temp_file = tempfile.mkstemp(suffix=".svg")
            os.write(handle, pico_xml.encode("utf-8"))
            os.close(handle)
            current_path = temp_file
        except ImportError:
            sys.stderr.write("warning: picosvg is not installed; skipping stroke-to-fill expansion\n")

    return current_path


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("svg", nargs="?", help="input .svg file")
    ap.add_argument("--name", help="Kotlin val name (required unless --self-test)")
    ap.add_argument("--dp", type=float, default=None, help="defaultWidth/Height in dp (default: viewport size)")
    ap.add_argument("--source", default="", help="provenance note for the generated KDoc")
    ap.add_argument("--indent-level", type=int, default=2, help="indent depth of the emitted val (default 2 = nested object)")
    ap.add_argument("--svgo", action="store_true", help="run SVGO preprocessing via npx/svgo to flatten shapes and transforms")
    ap.add_argument("--expand-strokes", action="store_true", help="run picosvg stroke expansion to convert stroke centerlines into pre-expanded fill paths")
    ap.add_argument("--self-test", action="store_true")
    args = ap.parse_args()

    if args.self_test:
        self_test()
        return
    if not args.svg or not args.name:
        ap.error("svg and --name are required")

    processed_svg = preprocess_svg(args.svg, use_svgo=args.svgo, expand_strokes=args.expand_strokes)
    try:
        viewport, _ = parse_svg(processed_svg)
        dp = args.dp if args.dp is not None else viewport[0]
        print(convert(processed_svg, args.name, dp, args.source, args.indent_level))
    finally:
        if processed_svg != args.svg and os.path.exists(processed_svg):
            os.unlink(processed_svg)


if __name__ == "__main__":
    main()
