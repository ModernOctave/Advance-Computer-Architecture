#!/usr/bin/env bash

# This is used to build the PIN tools

echo "Compiling PIN tools $1"
TEJAS_HOME="../../.."
PIN_HOME=$2

cd src/emulator/pin


g++ -Wall -Werror -Wno-unknown-pragmas -D__PIN__=1 -DPIN_CRT=1 -fno-stack-protector -fno-exceptions -funwind-tables -fasynchronous-unwind-tables -fno-rtti -DTARGET_IA32E -DHOST_IA32E -fPIC -DTARGET_LINUX -fabi-version=2 -I$PIN_HOME/source/include/pin -I$PIN_HOME/source/include/pin/gen -isystem $PIN_HOME/extras/stlport/include -isystem $PIN_HOME/extras/libstdc++/include -isystem $PIN_HOME/extras/crt/include -isystem $PIN_HOME/extras/crt/include/arch-x86_64 -isystem $PIN_HOME/extras/crt/include/kernel/uapi -isystem $PIN_HOME/extras/crt/include/kernel/uapi/asm-x86 -I$PIN_HOMEextras/components/include -I$PIN_HOME/extras/components/include -I$PIN_HOME//extras/xed-intel64/include/xed -I$PIN_HOME/source/tools/InstLib -O3 -fomit-frame-pointer -fno-strict-aliasing   -c -I$TEJAS_HOME/src/simulator/emulatorinterface/communication -I$TEJAS_HOME/src/simulator/emulatorinterface/communication/shm -I$TEJAS_HOME/src/simulator/emulatorinterface/communication/filePacket causalityTool.cpp ../../simulator/emulatorinterface/communication/shm/shmem.cc

mkdir obj-pin
mv causalityTool.o shmem.o obj-pin/

g++ -shared -Wl,--hash-style=sysv $PIN_HOME/intel64/runtime/pincrt/crtbeginS.o -Wl,-Bsymbolic -Wl,--version-script=$PIN_HOME/source/include/pin/pintool.ver -fabi-version=2    -o obj-pin/causalityTool.so obj-pin/causalityTool.o obj-pin/shmem.o  -L$PIN_HOME/intel64/runtime/pincrt -L$PIN_HOME/intel64/lib -L$PIN_HOME/intel64/lib-ext -L$PIN_HOME/extras/xed-intel64/lib -lpin -lxed $PIN_HOME/intel64/runtime/pincrt/crtendS.o -lpin3dwarf  -ldl-dynamic -nostdlib -lstlport-dynamic -lm-dynamic -lc-dynamic -lunwind-dynamic

cd $TEJAS_HOME

