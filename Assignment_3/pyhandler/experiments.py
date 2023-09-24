import json
from logging import log
from operations import *
from modifiers import *


traces = ["gcc", "lbm", "mcf", "namd", "xalancbmk"]

def SaturatingBitsExperiment():
	log("\n------------------------\n")
	log("Saturating Bits Experiment\n")

	bits = [1, 2, 3, 4]
	predictors = ["Bimodal", "TAGE-SC-L", "PerfectPredictor"]

	for t in traces:
		log(f"Running {t}")

		log("Bits", end="\t")
		for p in predictors:
			log(f"{p}", end="\t")
		log("")

		for i in bits:
			log(f"{i}", end="\t")
			for p in predictors:
				resetCustomConfig()
				print(f"[INFO]: Running {t} with {p} and {i} saturating bits")
				changeBranchPredictor(Predictor=p, SaturatingBits=i)
				runTejas(t)
				log(f"{getIPC()}", end="\t")
			log("")

	log("\n------------------------\n")

def RegisterFileSizeExperiment():
	log("\n------------------------\n")
	log("Register File Size Experiment\n")

	factors = [0.5, 1, 2, 4]

	log("Trace", end="\t")
	for i in factors:
		log(f"{i}x", end="\t")
	log("")

	for t in traces:
		log(f"{t}", end="\t")
		for i in factors:
			print(f"[INFO]: Running {t} with {i}x register file size")
			resetCustomConfig()
			changeRegisterFileSize(factor=i)
			runTejas(t)
			log(f"{getIPC()}", end="\t")
		log("")

	log("\n------------------------\n")

def ALULatencyExperiment():
	log("\n------------------------\n")
	log("ALU Latency Experiment\n")

	factors = [0.5, 1, 2, 4]

	log("Trace", end="\t")
	for i in factors:
		log(f"{i}x", end="\t")
	log("")

	for t in traces:
		log(f"{t}", end="\t")
		for i in factors:
			print(f"[INFO]: Running {t} with {i}x ALU latency")
			resetCustomConfig()
			changeALULatency(factor=i)
			runTejas(t)
			log(f"{getIPC()}", end="\t")
		log("")

	log("\n------------------------\n")

def LoadStoreLatencyExperiment():
	log("\n------------------------\n")
	log("Load/Store Latency Experiment\n")

	factors = [0.5, 1, 2, 4]

	log("Trace", end="\t")
	for i in factors:
		log(f"{i}x", end="\t")
	log("")

	for t in traces:
		log(f"{t}", end="\t")
		for i in factors:
			print(f"[INFO]: Running {t} with {i}x Load/Store latency")
			resetCustomConfig()
			changeLoadStoreLatency(factor=i)
			runTejas(t)
			log(f"{getIPC()}", end="\t")
		log("")

	log("\n------------------------\n")

def LoadStoreAGULatencyExperiment():
	log("\n------------------------\n")
	log("Load/Store AGU Latency Experiment\n")

	factors = [0.5, 1, 2, 4]

	log("Trace", end="\t")
	for i in factors:
		log(f"{i}x", end="\t")
	log("")

	for t in traces:
		log(f"{t}", end="\t")
		for i in factors:
			print(f"[INFO]: Running {t} with {i}x Load/Store AGU latency")
			resetCustomConfig()
			changeLoadStoreAGULatency(factor=i)
			runTejas(t)
			log(f"{getIPC()}", end="\t")
		log("")

	log("\n------------------------\n")

def CacheSizeExperiment():
	log("\n------------------------\n")
	log("Cache Size Experiment\n")

	factors = [0.5, 1, 2, 4]

	log("Trace", end="\t")
	for i in factors:
		log(f"{i}x", end="\t")
	log("")

	for t in traces:
		log(f"{t}", end="\t")
		for i in factors:
			print(f"[INFO]: Running {t} with {i}x cache size")
			resetCustomConfig()
			changeCacheSize(factor=i)
			runTejas(t)
			log(f"{getIPC()}", end="\t")
		log("")

	log("\n------------------------\n")

def CacheReadLatencyExperiment():
	log("\n------------------------\n")
	log("Cache Read Latency Experiment\n")

	factors = [0.5, 1, 2, 4]

	log("Trace", end="\t")
	for i in factors:
		log(f"{i}x", end="\t")
	log("")

	for t in traces:
		log(f"{t}", end="\t")
		for i in factors:
			print(f"[INFO]: Running {t} with {i}x cache read latency")
			resetCustomConfig()
			changeCacheReadLatency(factor=i)
			runTejas(t)
			log(f"{getIPC()}", end="\t")
		log("")

	log("\n------------------------\n")

def CacheWriteLatencyExperiment():
	log("\n------------------------\n")
	log("Cache Write Latency Experiment\n")

	factors = [0.5, 1, 2, 4]

	log("Trace", end="\t")
	for i in factors:
		log(f"{i}x", end="\t")
	log("")

	for t in traces:
		log(f"{t}", end="\t")
		for i in factors:
			print(f"[INFO]: Running {t} with {i}x cache write latency")
			resetCustomConfig()
			changeCacheWriteLatency(factor=i)
			runTejas(t)
			log(f"{getIPC()}", end="\t")
		log("")

	log("\n------------------------\n")