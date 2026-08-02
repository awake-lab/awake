#!/usr/bin/env python3
# Copyright (c) Ron June Valdoz
# SPDX-License-Identifier: Apache-2.0
"""Serve a static HTML report directory with auto-reload on file change.

Built for the ui-previews gallery (samples/ui-showcase/build/reports/ui-previews),
which `./gradlew :samples:ui-showcase:desktopTest --tests "*UiShowcasePreviewDocsTest*"
--continuous` regenerates in place on source changes. This script just serves that
directory and injects a tiny poll script into HTML responses so the open tab reloads
itself once the mtime of any file in the directory changes.

ponytail: polls every second and hashes directory mtimes -- fine for a single-directory
dev gallery with a handful of files; swap for a real filesystem-event watch (watchdog) if
this ever needs to scale beyond one report directory.

Usage:
    python3 tools/ui_preview_server.py <directory> [port]
"""
from __future__ import annotations

import http.server
import os
import sys
import threading
import time

RELOAD_SNIPPET = b"""
<script>
(function () {
  var lastStamp = null;
  function poll() {
    fetch('/__reload_stamp__', { cache: 'no-store' })
      .then(function (r) { return r.text(); })
      .then(function (stamp) {
        if (lastStamp !== null && stamp !== lastStamp) {
          location.reload();
          return;
        }
        lastStamp = stamp;
        setTimeout(poll, 1000);
      })
      .catch(function () { setTimeout(poll, 1000); });
  }
  poll();
})();
</script>
</body>"""


def directory_stamp(root: str) -> str:
    total = 0.0
    for dirpath, _dirnames, filenames in os.walk(root):
        for name in filenames:
            try:
                total += os.path.getmtime(os.path.join(dirpath, name))
            except OSError:
                pass
    return f"{total:.3f}"


def make_handler(root: str) -> type[http.server.SimpleHTTPRequestHandler]:
    class ReloadingHandler(http.server.SimpleHTTPRequestHandler):
        def __init__(self, *args, **kwargs):
            super().__init__(*args, directory=root, **kwargs)

        def log_message(self, fmt: str, *args) -> None:
            pass

        def do_GET(self) -> None:  # noqa: N802 (BaseHTTPRequestHandler API)
            if self.path == "/__reload_stamp__":
                stamp = directory_stamp(root).encode("utf-8")
                self.send_response(200)
                self.send_header("Content-Type", "text/plain")
                self.send_header("Content-Length", str(len(stamp)))
                self.end_headers()
                self.wfile.write(stamp)
                return
            super().do_GET()

        def send_head(self):
            path = self.translate_path(self.path)
            if os.path.isdir(path):
                path = os.path.join(path, "index.html")
            if not path.endswith(".html") or not os.path.isfile(path):
                return super().send_head()
            with open(path, "rb") as f:
                body = f.read()
            if b"</body>" in body:
                body = body.replace(b"</body>", RELOAD_SNIPPET, 1)
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            import io

            return io.BytesIO(body)

    return ReloadingHandler


def main() -> int:
    if len(sys.argv) < 2:
        print("usage: ui_preview_server.py <directory> [port]", file=sys.stderr)
        return 2
    root = os.path.abspath(sys.argv[1])
    port = int(sys.argv[2]) if len(sys.argv) > 2 else 8090
    if not os.path.isdir(root):
        print(f"waiting for report directory to exist: {root}")
        while not os.path.isdir(root):
            time.sleep(1)
    handler = make_handler(root)
    server = http.server.ThreadingHTTPServer(("127.0.0.1", port), handler)
    print(f"Serving {root} at http://127.0.0.1:{port} (auto-reload on change)")
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        server.shutdown()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
