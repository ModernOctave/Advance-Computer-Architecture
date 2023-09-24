import sys
from matplotlib import pyplot as plt

# Load the log file
file1 = open(f"perf-{sys.argv[1]}.1.log", "r")
file2 = open(f"perf-{sys.argv[1]}.2.log", "r")

time1 = []
time2 = []
ipc = []
branch = []
branch_accuracy = []
l1_load = []
l1_store = []
l1_hit_rate = []
llc_load = []
llc_store = []
llc_load_hit_rate = []
llc_store_hit_rate = []
mem_load = []
mem_store = []
power = []

# Read the file line by line
lines = file1.readlines()
i = 3
while i < len(lines):
	if lines[i].split()[0] == "#":
		i += 1
	time1.append(float(lines[i].split()[0]))
	ipc.append(float(lines[i+1].split()[4]))
	branch.append(float(lines[i+2].split()[1].replace(",", "")))
	branch_accuracy.append(100-float(lines[i+3].split()[4].strip("%")))
	l1_load.append(float(lines[i+4].split()[1].replace(",", "")))
	l1_store.append(float(lines[i+5].split()[1].replace(",", "")))
	l1_hit_rate.append(100-float(lines[i+6].split()[4].strip("%")))
	mem_load.append(float(lines[i+7].split()[1].replace(",", "")))
	mem_store.append(float(lines[i+8].split()[1].replace(",", "")))
	power.append(float(lines[i+9].split()[1]))

	i += 10

lines = file2.readlines()
i = 3
while i < len(lines):
	if lines[i].split()[0] == "#":
		i += 1
	time2.append(float(lines[i].split()[0]))
	llc_load.append(float(lines[i].split()[1].replace(",", "")))
	llc_store.append(float(lines[i+1].split()[1].replace(",", "")))
	llc_load_hit_rate.append(100-float(lines[i+2].split()[4].strip("%")))
	llc_store_hit_rate.append(100-float(lines[i+3].split()[1].replace(",", ""))/llc_store[-1]*100)

	i += 4

# Plot the graphs
fig, ax = plt.subplots(6, 1, figsize=(8, 12))
fig.tight_layout(pad=5)
fig.suptitle(f"Performance Analysis of {sys.argv[1]}", fontsize=16, y=0.995)
ax[0].plot(time1, ipc)
ax[0].set_xlabel("Time (s)")
ax[0].set_ylabel("IPC")
ax[0].set_title("IPC vs Time")
ax[1].plot(time1, branch_accuracy)
ax[1].set_xlabel("Time (s)")
ax[1].set_ylabel("Branch Accuracy (%)")
ax[1].set_title("Branch Accuracy vs Time")
ax[2].plot(time1, l1_load, label="L1 Load")
ax[2].plot(time1, l1_store, label="L1 Store")
ax[2].set_xlabel("Time (s)")
ax[2].set_ylabel("L1 Cache Accesses")
ax[2].set_title("L1 Cache Accesses vs Time")
ax[2].legend()
ax[3].plot(time1, l1_hit_rate)
ax[3].set_xlabel("Time (s)")
ax[3].set_ylabel("L1 Cache Hit Rate (%)")
ax[3].set_title("L1 Cache Hit Rate vs Time")
ax[4].plot(time1, mem_load, label="Memory Load")
ax[4].plot(time1, mem_store, label="Memory Store")
ax[4].set_xlabel("Time (s)")
ax[4].set_ylabel("Memory Accesses")
ax[4].set_title("Memory Accesses vs Time")
ax[4].legend()
ax[5].plot(time1, power)
ax[5].set_xlabel("Time (s)")
ax[5].set_ylabel("Power (W)")
ax[5].set_title("Power vs Time")
plt.savefig(f"perf-{sys.argv[1]}.1.png")

fig, ax = plt.subplots(2, 1, figsize=(8, 5))
fig.tight_layout(pad=5)
ax[0].plot(time2, llc_load, label="Load")
ax[0].plot(time2, llc_store, label="Store")
ax[0].set_xlabel("Time (s)")
ax[0].set_ylabel("LLC Accesses")
ax[0].set_title("LLC Accesses vs Time")
ax[0].legend()
ax[1].plot(time2, llc_load_hit_rate, label="Load")
ax[1].plot(time2, llc_store_hit_rate, label="Store")
ax[1].set_xlabel("Time (s)")
ax[1].set_ylabel("LLC Hit Rate (%)")
ax[1].set_title("LLC Hit Rate vs Time")
ax[1].legend()
plt.savefig(f"perf-{sys.argv[1]}.2.png")

# Print average values
print(f"Average IPC: {sum(ipc)/len(ipc)}")
print(f"Average Branch Accuracy: {sum(branch_accuracy)/len(branch_accuracy)}")
print(f"Average L1 Hit Rate: {sum(l1_hit_rate)/len(l1_hit_rate)}")
print(f"Average LLC Load Hit Rate: {sum(llc_load_hit_rate)/len(llc_load_hit_rate)}")
print(f"Average LLC Store Hit Rate: {sum(llc_store_hit_rate)/len(llc_store_hit_rate)}")
print(f"Average Power: {sum(power)/len(power)}")
print(f"Average L1 Loads: {sum(l1_load)/len(l1_load)}")
print(f"Average Branches: {sum(branch)/len(branch)}")