from sys import argv
from experiments import *


index = argv[1] if len(argv) > 1 else ""

if __name__ == "__main__":
	if index == "0":
		SaturatingBitsExperiment()
	elif index == "1":
		RegisterFileSizeExperiment()
	elif index == "2":
		ALULatencyExperiment()
	elif index == "3":
		LoadStoreLatencyExperiment()
	elif index == "4":
		LoadStoreAGULatencyExperiment()
	elif index == "5":
		CacheSizeExperiment()
	elif index == "6":
		CacheReadLatencyExperiment()
	elif index == "7":
		CacheWriteLatencyExperiment()