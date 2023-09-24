import os


def changeBranchPredictor(Predictor="TAGE-SC-L", SaturatingBits=2):
    os.system(f"sed -i 's/<Predictor_Mode>[a-zA-Z\-]*/<Predictor_Mode>{Predictor}/g' config_custom.xml")
    os.system(f"sed -i 's/<SaturatingBits>[0-9]*/<SaturatingBits>{SaturatingBits}/g' config_custom.xml")

def changeRegisterFileSize(factor=1):
    IntRegFileSize = int(280 * factor)
    VectorRegFileSize = int(224 * factor)
    os.system(f"sed -i 's/<IntRegFileSize>[0-9]*/<IntRegFileSize>{IntRegFileSize}/g' config_custom.xml")
    os.system(f"sed -i 's/<VectorRegFileSize>[0-9]*/<VectorRegFileSize>{VectorRegFileSize}/g' config_custom.xml")

def changeALULatency(factor=1):
    config = ET.parse("config_custom.xml")
    root = config.getroot()
    for eu in ["IntALU", "IntMult", "IntDiv", "FloatDiv", "IntVectorALU", "IntVectorMul"]:
        root.find(eu).find("Latency").text = str(int(root.find(eu).find("Latency").text) * factor)

def changeLoadStoreLatency(factor=1):
    config = ET.parse("config_custom.xml")
    root = config.getroot()
    for eu in ["Load", "Store"]:
        root.find(eu).find("Latency").text = str(int(root.find(eu).find("Latency").text) * factor)

def changeLoadStoreAGULatency(factor=1):
    config = ET.parse("config_custom.xml")
    root = config.getroot()
    for eu in ["LoadAGU", "StoreAGU"]:
        root.find(eu).find("Latency").text = str(int(root.find(eu).find("Latency").text) * factor)

def changeCacheSize(factor=1):
    config = ET.parse("config_custom.xml")
    root = config.getroot()
    for c in ["ICache_32K_8", "L1Cache_32K_8", "L2Cache_256K_4"]:
        root.find(c).find("Size").text = str(int(root.find(c).find("Size").text) * factor)

def changeCacheReadLatency(factor=1):
    config = ET.parse("config_custom.xml")
    root = config.getroot()
    for c in ["ICache_32K_8", "L1Cache_32K_8", "L2Cache_256K_4"]:
        root.find(c).find("ReadLatency").text = str(int(root.find(c).find("ReadLatency").text) * factor)

def changeCacheWriteLatency(factor=1):
    config = ET.parse("config_custom.xml")
    root = config.getroot()
    for c in ["ICache_32K_8", "L1Cache_32K_8", "L2Cache_256K_4"]:
        root.find(c).find("WriteLatency").text = str(int(root.find(c).find("WriteLatency").text) * factor)