import os
import xml.etree.ElementTree as ET
from main import index


def changeBranchPredictor(Predictor="TAGE-SC-L", SaturatingBits=2):
    os.system(f"sed -i 's/<Predictor_Mode>[a-zA-Z\-]*/<Predictor_Mode>{Predictor}/g' config_custom.{index}.xml")
    os.system(f"sed -i 's/<SaturatingBits>[0-9]*/<SaturatingBits>{SaturatingBits}/g' config_custom.{index}.xml")

def changeRegisterFileSize(factor=1):
    IntRegFileSize = int(280 * factor)
    VectorRegFileSize = int(224 * factor)
    os.system(f"sed -i 's/<IntRegFileSize>[0-9]*/<IntRegFileSize>{IntRegFileSize}/g' config_custom.{index}.xml")
    os.system(f"sed -i 's/<VectorRegFileSize>[0-9]*/<VectorRegFileSize>{VectorRegFileSize}/g' config_custom.{index}.xml")

def changeALULatency(factor=1):
    config = ET.parse(f"config_custom.{index}.xml")
    root = config.getroot()
    for eu in ["IntALU", "IntMul", "IntDiv", "FloatDiv", "IntVectorALU", "IntVectorMul"]:
        root.find(f"System/Core/{eu}/Latency").text = str(int(root.find(f"System/Core/{eu}/Latency").text) * factor)

def changeLoadStoreLatency(factor=1):
    config = ET.parse(f"config_custom.{index}.xml")
    root = config.getroot()
    for eu in ["Load", "Store"]:
        root.find(f"System/Core/{eu}/Latency").text = str(int(root.find(f"System/Core/{eu}/Latency").text) * factor)

def changeLoadStoreAGULatency(factor=1):
    config = ET.parse(f"config_custom.{index}.xml")
    root = config.getroot()
    for eu in ["LoadAGU", "StoreAGU"]:
        root.find(f"System/Core/{eu}/Latency").text = str(int(root.find(f"System/Core/{eu}/Latency").text) * factor)

def changeCacheSize(factor=1):
    config = ET.parse(f"config_custom.{index}.xml")
    root = config.getroot()
    for c in ["ICache_32K_8", "L1Cache_48K_12", "L2Cache_1280K_20"]:
        print(root.find(f"Library/{c}/Size").text)
        root.find(f"Library/{c}/Size").text = str(int(root.find(f"Library/{c}/Size").text) * factor)

def changeCacheReadLatency(factor=1):
    config = ET.parse(f"config_custom.{index}.xml")
    root = config.getroot()
    for c in ["ICache_32K_8", "L1Cache_48K_12", "L2Cache_1280K_20"]:
        root.find(f"Library/{c}/ReadLatency").text = str(int(root.find(f"Library/{c}/ReadLatency").text) * factor)

def changeCacheWriteLatency(factor=1):
    config = ET.parse(f"config_custom.{index}.xml")
    root = config.getroot()
    for c in ["ICache_32K_8", "L1Cache_48K_12", "L2Cache_1280K_20"]:
        root.find(f"Library/{c}/WriteLatency").text = str(int(root.find(f"Library/{c}/WriteLatency").text) * factor)