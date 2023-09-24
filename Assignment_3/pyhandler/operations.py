import os
import shutil


def resetCustomConfig():
    # See if config_custom.xml exists
    if not os.path.isfile("config_custom.xml"):
        print("[INFO]: config_custom.xml not found, creating...")
    shutil.copyfile("config_base.xml", "config_custom.xml")
    print("[INFO]: config_custom.xml restored to default")

def getIPC():
    # Get IPC
    with open("run.stat", "r") as f:
        return f.readlines()[28].split()[2]

def runTejas(benchmark):
    # Remove old stat file
    if os.path.isfile("run.stat"):
        os.remove("run.stat")
    # Run Tejas
    os.system(f"java -jar tejas/jars/tejas.jar config_custom.xml run.stat traces/{benchmark}")