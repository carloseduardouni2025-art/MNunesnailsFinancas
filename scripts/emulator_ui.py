"""ADB helpers for the dedicated MNunesNails_API36_Test emulator.

Screenshots and UI dumps are stored under app/build/emulator-test.
This module never clears application data or targets a physical device.
"""
import json
import os
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "app/build/emulator-test"
OUT.mkdir(parents=True, exist_ok=True)
ADB = Path(os.environ["LOCALAPPDATA"]) / "Android/Sdk/platform-tools/adb.exe"
SERIAL = "emulator-5556"
PACKAGE = "br.com.mnunesnails.financas"


def adb(*args, binary=False):
    result = subprocess.run([str(ADB), "-s", SERIAL, *args], capture_output=True, timeout=40)
    if result.returncode:
        raise RuntimeError(result.stderr.decode("utf-8", errors="replace"))
    return result.stdout if binary else result.stdout.decode("utf-8", errors="replace").strip()


def tree():
    adb("shell", "uiautomator", "dump", "/sdcard/mnunes-window.xml")
    text = adb("shell", "cat", "/sdcard/mnunes-window.xml")
    (OUT / "last-window.xml").write_text(text, encoding="utf-8")
    return ET.fromstring(text)


def inspect():
    for node in tree().iter("node"):
        attrs = node.attrib
        if attrs.get("text") or attrs.get("content-desc"):
            print({key: attrs.get(key) for key in ["text", "content-desc", "class", "bounds", "clickable"]})


def find(value, attribute="text", scroll=False):
    for attempt in range(5 if scroll else 1):
        for node in tree().iter("node"):
            if node.get(attribute) == value:
                return node
        if scroll:
            swipe()
    raise AssertionError(f"UI element not found: {attribute}={value}")


def tap_node(node):
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
    adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
    time.sleep(.25)


def tap(value, attribute="text", scroll=False):
    tap_node(find(value, attribute, scroll))


def fill(label, value):
    tap(label, "content-desc", scroll=True)
    adb("shell", "input", "keyevent", "KEYCODE_MOVE_END")
    adb("shell", "input", "keycombination", "KEYCODE_CTRL_LEFT", "KEYCODE_A")
    adb("shell", "input", "text", value.replace(" ", "%s"))
    adb("shell", "input", "keyevent", "KEYCODE_BACK")


def swipe():
    size = adb("shell", "wm", "size")
    width, height = map(int, re.findall(r"(\d+)x(\d+)", size)[-1])
    adb("shell", "input", "swipe", str(width//2), str(height*3//4), str(width//2), str(height//3), "350")


def screenshot(name):
    (OUT / f"{name}.png").write_bytes(adb("exec-out", "screencap", "-p", binary=True))


def records():
    result = subprocess.run([str(ADB), "-s", SERIAL, "shell", "run-as", PACKAGE, "cat", "shared_prefs/finance.xml"], capture_output=True)
    if result.returncode:
        if b"No such file" in result.stderr or b"No such file" in result.stdout:
            return {"services": [], "incomes": []}
        raise RuntimeError(result.stderr.decode())
    root = ET.fromstring(result.stdout)
    result = {element.get("name"): json.loads(element.text) for element in root if element.tag == "string"}
    return {"services": result.get("services", []), "incomes": result.get("incomes", [])}


if __name__ == "__main__":
    inspect()
