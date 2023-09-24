

from sys import platform
import subprocess
import xml.etree.ElementTree as ET
import os, sys
import shutil

# TEJAS_HOME = "/home/sandeep/Desktop/Work/PhD/Tejas"
# PIN_HOME = "/home/sandeep/Desktop/Test/pin-3.7-97619-g0d0c92f4f-gcc-linux/"

print (("\n\nStep 1 : Reading Config files"))
fname  = 'bin/config/config.xml'
tree = ET.parse(fname)
root = tree.getroot()
emulator = root.find('Emulator')
TEJAS_HOME=emulator.find('Tejashome').text
PIN_HOME=emulator.find('PinTool').text


#PIN
print (("\n\nStep 2 : setting up Pin"))
print ("Building Pin")
subprocess.call(['./tejaspin.sh', TEJAS_HOME, PIN_HOME])
print ("Building PIN done")

#configuring
print ('\n\nStep 3 : Configuring')

jniInclude=""
fname=""

jniInclude =  "-I/usr/lib/jvm/java-8-openjdk-amd64/include/linux -I/usr/lib/jvm/java-8-openjdk-amd64/include "
fname = 'src/emulator/pin/makefile_linux_mac'
	
print ('setting PINPATH in ' + fname + " to " + PIN_HOME)
f = open(fname, 'r')
lines = f.readlines()
i = 0
for line in lines:
	if "PIN_KIT ?=" in line:
		lines[i] = "PIN_KIT ?=" + PIN_HOME + "\n"
	if "JNINCLUDE =" in line:
		lines[i] = "JNINCLUDE =" + jniInclude + "\n"
	i = i + 1
f.close()
f = open(fname, 'w')
for line in lines:
	f.write(line)
f.close()

#update config.xml
fname  = 'src/simulator/config/config.xml'
tree = ET.parse(fname)
root = tree.getroot()
emulator = root.find('Emulator')
print ('setting PinTool in ' + fname + ' to ' + PIN_HOME)
emulator.find('PinTool').text = PIN_HOME
print ('setting PinInstrumentor in ' + fname + ' to ' + TEJAS_HOME + '/src/emulator/pin/obj-pin/causalityTool.so')
emulator.find('PinInstrumentor').text = TEJAS_HOME + "/src/emulator/pin/obj-pin/causalityTool.so"
print ('setting ShmLibDirectory in ' + fname + ' to ' + TEJAS_HOME + '/src/emulator/pin/obj-comm')
emulator.find('ShmLibDirectory').text = TEJAS_HOME + "/src/emulator/pin/obj-comm"
print ('setting KillEmulatorScript in ' + fname + ' to ' + TEJAS_HOME + '/src/simulator/main/killAllDescendents.sh')
emulator.find('KillEmulatorScript').text = TEJAS_HOME + "/src/simulator/main/killAllDescendents.sh"

system = root.find('System')
noc = system.find('NOC')
print ('setting NocConfigFile in ' + fname + ' to ' + TEJAS_HOME + '/src/simulator/config/NocConfig.txt')
noc.find('NocConfigFile').text = TEJAS_HOME + '/src/simulator/config/NocConfig.txt'



if sys.version_info < (2, 7):
	tree.write(fname, encoding="UTF-8")
else:
	tree.write(fname, encoding="UTF-8", xml_declaration=True)

print ("configure successful")




#building
print ('\n\nStep 4 : Building Jar File')
print ("jniInclude is " + jniInclude)
status = subprocess.call('ant make-jar',shell=True)
if status != 0 or os.path.exists(TEJAS_HOME + "/src/emulator/pin/obj-pin/causalityTool.so") == False or os.path.exists(TEJAS_HOME + "/src/emulator/pin/obj-comm/libshmlib.so") == False:
	print ("error building : " + str(os.WEXITSTATUS(status)))
	# print (output)
	sys.exit(1)
else:
	print ("build successful")


print ("------------- Tejas installed successfuly ----------------\n" )
print ("Tejas jar has been created here : " + TEJAS_HOME + "/jars/tejas.jar")
print ("Configuration file is kept here : " + TEJAS_HOME + "/src/simulator/config/config.xml")
print ("Use this command to run tejas : java -jar <tejas.jar> <config-file> <output-file> <input-program and arguments>")
