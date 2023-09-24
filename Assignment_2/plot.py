import sys
from matplotlib import pyplot as plt

# Load the log file
file1 = open(f"dependency-{sys.argv[1]}.log", "r")

rawDistance = []
rawDistanceCount = []

warDistance = []
warDistanceCount = []

wawDistance = []
wawDistanceCount = []

StoreLoadDistance = []
StoreLoadDistanceCount = []

# Read the file line by line
lines = file1.readlines()
i = 2

while i < len(lines):
    if lines[i].split()[1] == "Distances:":
        i += 1
    if lines[i].split()[0] == "RAW":
        rawDistance.append(lines[i].split()[2])
        rawDistanceCount.append(int(lines[i].split()[4]))
    elif lines[i].split()[0] == "WAR":
        warDistance.append(lines[i].split()[2])
        warDistanceCount.append(int(lines[i].split()[4]))
    elif lines[i].split()[0] == "WAW":
        wawDistance.append(lines[i].split()[2])
        wawDistanceCount.append(int(lines[i].split()[4]))
    elif lines[i].split()[0] == "Store-Load":
        StoreLoadDistance.append(lines[i].split()[2])
        StoreLoadDistanceCount.append(int(lines[i].split()[4]))
    
    i += 1

len = 150
rawDistance = rawDistance[:len]
rawDistanceCount = rawDistanceCount[:len]
warDistance = warDistance[:len]
warDistanceCount = warDistanceCount[:len]
wawDistance = wawDistance[:len]
wawDistanceCount = wawDistanceCount[:len]
StoreLoadDistance = StoreLoadDistance[:len]
StoreLoadDistanceCount = StoreLoadDistanceCount[:len]

# Plot the line graphs
fig, ax = plt.subplots(4, 1, figsize=(8, 12))
fig.tight_layout(pad=5)
fig.suptitle(f"Dependency Analysis of {sys.argv[1]}", fontsize=16, y=0.995)
ax[0].plot(rawDistance, rawDistanceCount)
ax[0].set_title("RAW Dependency")
ax[0].set_xlabel("Distance")
ax[0].set_ylabel("Count")
ax[1].plot(warDistance, warDistanceCount)
ax[1].set_title("WAR Dependency")
ax[1].set_xlabel("Distance")
ax[1].set_ylabel("Count")
ax[2].plot(wawDistance, wawDistanceCount)
ax[2].set_title("WAW Dependency")
ax[2].set_xlabel("Distance")
ax[2].set_ylabel("Count")
ax[3].plot(StoreLoadDistance, StoreLoadDistanceCount)
ax[3].set_title("Store-Load Dependency")
ax[3].set_xlabel("Distance")
ax[3].set_ylabel("Count")
plt.savefig(f"dependency-{sys.argv[1]}.png")
plt.show()
