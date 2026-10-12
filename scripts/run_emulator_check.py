"""Run the UI checklist on the dedicated, already booted Android 16 emulator.

Usage: python scripts/run_emulator_check.py [--reset-test-data]
The optional reset only clears this app on MNunesNails_API36_Test.
"""
import argparse
import runpy
from emulator_ui import *

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--reset-test-data", action="store_true")
args = parser.parse_args()
assert adb("emu", "avd", "name").splitlines()[0] == "MNunesNails_API36_Test", "Wrong emulator; no changes made"
assert adb("shell", "getprop", "sys.boot_completed") == "1", "Wait for Android to finish booting"
adb("install", "-r", str(ROOT / "app/build/outputs/apk/debug/app-debug.apk"))
if args.reset_test_data:
    adb("shell", "pm", "clear", PACKAGE)
assert records() == {"services": [], "incomes": []}, "Use a fresh test install, or explicitly pass --reset-test-data"
adb("shell", "wm", "size", "reset")
adb("shell", "wm", "density", "reset")
adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
adb("shell", "settings", "put", "system", "user_rotation", "0")
adb("shell", "settings", "put", "secure", "show_ime_with_hard_keyboard", "1")
adb("shell", "cmd", "uimode", "night", "no")
adb("shell", "am", "force-stop", PACKAGE)
adb("shell", "am", "start", "-n", PACKAGE + "/.MainActivity")
time.sleep(1)
find("Seu primeiro lançamento")
screenshot("01-home-light")
tap("Serviços", "content-desc")
find("Seu catálogo começa aqui")
tap("Receitas", "content-desc")
find("Nenhum recebimento ainda")
tap("Início", "content-desc")
tap("Cadastrar Serviço", "content-desc")
screenshot("02-service-light")
try:
    for stage in ["emulator_service_check.py", "emulator_income_check.py", "emulator_final_check.py"]:
        runpy.run_path(str(ROOT / "scripts" / stage), run_name="__main__")
finally:
    adb("shell", "wm", "size", "reset")
    adb("shell", "wm", "density", "reset")
    adb("shell", "settings", "put", "system", "user_rotation", "0")
