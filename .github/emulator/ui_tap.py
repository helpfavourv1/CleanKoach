"""Tap a UI element on the emulator by its visible text or content description.
Usage: ui_tap.py <label> [--scroll]   Exit 0 if tapped, 1 if not found."""
import re
import subprocess
import sys
import xml.etree.ElementTree as ET


def adb(*args):
    return subprocess.run(["adb", *args], capture_output=True, text=True).stdout


def dump():
    adb("shell", "uiautomator", "dump", "/sdcard/u.xml")
    raw = adb("shell", "cat", "/sdcard/u.xml")
    try:
        return ET.fromstring(raw[raw.index("<?xml"):])
    except Exception:
        return None


def find(root, label):
    if root is None:
        return None
    for node in root.iter("node"):
        if node.get("text") == label or node.get("content-desc") == label:
            m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds", ""))
            if m:
                l, t, r, b = map(int, m.groups())
                return (l + r) // 2, (t + b) // 2
    return None


def main():
    label = sys.argv[1]
    scroll = "--scroll" in sys.argv
    size = re.search(r"(\d+)x(\d+)", adb("shell", "wm", "size"))
    w, h = (int(size.group(1)), int(size.group(2))) if size else (1080, 2400)
    for _ in range(14 if scroll else 1):
        pos = find(dump(), label)
        if pos:
            adb("shell", "input", "tap", str(pos[0]), str(pos[1]))
            return 0
        if not scroll:
            break
        adb("shell", "input", "swipe", str(w // 2), str(int(h * 0.8)), str(w // 2), str(int(h * 0.35)), "300")
    print(f"not found: {label}")
    return 1


sys.exit(main())
