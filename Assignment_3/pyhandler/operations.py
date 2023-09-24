import os
import shutil
from main import index


def resetCustomConfig():
    # See if config_custom.{index}.xml exists
    if not os.path.isfile(f"config_custom.{index}.xml"):
        print(f"[INFO]: config_custom.{index}.xml not found, creating...")
    shutil.copyfile("config_base.xml", f"config_custom.{index}.xml")
    print(f"[INFO]: config_custom.{index}.xml restored to default")

def getIPC():
    # Get IPC
    with open(f"run.{index}.stat", "r") as f:
        return f.readlines()[28].split()[2]

def runTejas(benchmark):
    # Remove old stat file
    if os.path.isfile(f"run.{index}.stat"):
        os.remove(f"run.{index}.stat")
    # Run Tejas
    os.system(f"java -jar tejas/jars/tejas.jar config_custom.{index}.xml run.{index}.stat traces/{benchmark}")