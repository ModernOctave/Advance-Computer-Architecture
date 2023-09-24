import json
from operations import *
from modifiers import *

traces = ["gcc", "lbm", "mcf", "namd", "xalancbmk"]

def SaturatingBitsExperiment():
    with open("log.txt", "w") as f:
        f.write("\n------------------------\n\n")
        f.write("Saturating Bits Experiment\n")

    bits = [1, 2, 3, 4]
    predictors = ["Bimodal", "TAGE-SC-L", "PerfectPredictor"]

    for t in traces:
        with open("log.txt", "a") as f:
            f.write(f"Running {t}\n")
            f.write("\t")
            for p in predictors:
                f.write(f"{p}\t")
            f.write("\n")
            for i in bits:
                f.write(f"{i} bits:\t")
                for p in predictors:
                    resetCustomConfig()
                    print(f"[INFO]: Running {t} with {p} and {i} saturating bits")
                    changeBranchPredictor(Predictor=p, SaturatingBits=i)
                    runTejas(t)
                    f.write(f"{getIPC()}\n")
            f.write("\n")

    with open("log.txt", "a") as f:
        f.write("\n------------------------\n\n")

def RegisterFileSizeExperiment():
    experiment = {"Name": {"Register File Size"}}

    factors = [0.25, 0.5, 1, 2, 4]

    for t in traces:
        experiment[t] = {}
        for i in factors:
            experiment[t][i] = {}
            print(f"[INFO]: Running {t} with {i}x register file size")
            resetCustomConfig()
            changeRegisterFileSize(factor=i)
            runTejas(t)
            experiment[t][i] = getIPC()

    log = json.load(open("log.json", "r"))
    log.append(experiment)
    json.dump(log, open("log.json", "w"))

def ALULatencyExperiment():
    experiment = {"Name": {"ALU Latency"}}

    factors = [0.5, 1, 2, 4]

    for t in traces:
        experiment[t] = {}
        for i in factors:
            experiment[t][i] = {}
            print(f"[INFO]: Running {t} with {i}x ALU latency")
            resetCustomConfig()
            changeALULatency(factor=i)
            runTejas(t)
            experiment[t][i] = getIPC()

    log = json.load(open("log.json", "r"))
    log.append(experiment)
    json.dump(log, open("log.json", "w"))

def LoadStoreLatencyExperiment():
    experiment = {"Name": {"Load/Store Latency"}}

    factors = [0.5, 1, 2, 4]

    for t in traces:
        experiment[t] = {}
        for i in factors:
            experiment[t][i] = {}
            print(f"[INFO]: Running {t} with {i}x Load/Store latency")
            resetCustomConfig()
            changeLoadStoreLatency(factor=i)
            runTejas(t)
            experiment[t][i] = getIPC()

    log = json.load(open("log.json", "r"))
    log.append(experiment)
    json.dump(log, open("log.json", "w"))

def LoadStoreAGULatencyExperiment():
    experiment = {"Name": {"Load/Store AGU Latency"}}

    factors = [0.5, 1, 2, 4]

    for t in traces:
        experiment[t] = {}
        for i in factors:
            experiment[t][i] = {}
            print(f"[INFO]: Running {t} with {i}x Load/Store AGU latency")
            resetCustomConfig()
            changeLoadStoreAGULatency(factor=i)
            runTejas(t)
            experiment[t][i] = getIPC()

    log = json.load(open("log.json", "r"))
    log.append(experiment)
    json.dump(log, open("log.json", "w"))

def CacheSizeExperiment():
    experiment = {"Name": {"Cache Size"}}

    factors = [0.5, 1, 2, 4]

    for t in traces:
        experiment[t] = {}
        for i in factors:
            experiment[t][i] = {}
            print(f"[INFO]: Running {t} with {i}x cache size")
            resetCustomConfig()
            changeCacheSize(factor=i)
            runTejas(t)
            experiment[t][i] = getIPC()

    log = json.load(open("log.json", "r"))
    log.append(experiment)
    json.dump(log, open("log.json", "w"))

def CacheReadLatencyExperiment():
    experiment = {"Name": {"Cache Read Latency"}}

    factors = [0.5, 1, 2, 4]

    for t in traces:
        experiment[t] = {}
        for i in factors:
            experiment[t][i] = {}
            print(f"[INFO]: Running {t} with {i}x cache read latency")
            resetCustomConfig()
            changeCacheReadLatency(factor=i)
            runTejas(t)
            experiment[t][i] = getIPC()

    log = json.load(open("log.json", "r"))
    log.append(experiment)
    json.dump(log, open("log.json", "w"))

def CacheWriteLatencyExperiment():
    experiment = {"Name": {"Cache Write Latency"}}

    factors = [0.5, 1, 2, 4]

    for t in traces:
        experiment[t] = {}
        for i in factors:
            experiment[t][i] = {}
            print(f"[INFO]: Running {t} with {i}x cache write latency")
            resetCustomConfig()
            changeCacheWriteLatency(factor=i)
            runTejas(t)
            experiment[t][i] = getIPC()

    log = json.load(open("log.json", "r"))
    log.append(experiment)
    json.dump(log, open("log.json", "w"))

if __name__ == "__main__":
    # ipcs = []
    # for i in range(1, 18, 4):
    #     updateCustomConfig(branchMispredPenalty=17-i)
    #     runTejas("gcc")
    #     ipcs.append(getIPC())
    #
    # print("\n-----------------------------")
    # for i in range(1, 18, 4):
    #     print(f"[INFO]: (Branch Penalty = {17-i}) IPC = {ipcs[i//4]}")

    SaturatingBitsExperiment()