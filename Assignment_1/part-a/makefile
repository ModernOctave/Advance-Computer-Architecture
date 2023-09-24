PIN_ROOT := pin-3.28-98749-g6643ecee5-gcc-linux
MAKE := make PIN_ROOT=../$(PIN_ROOT)

NAMD := ../CS810_resources/CPU2017_benchmarks/linux_executables/508.namd_r/namd_r_base.mytest-m64 --input ../CS810_resources/CPU2017_benchmarks/linux_executables//508.namd_r/apoa1.input --iterations 5
MCF := ../CS810_resources/CPU2017_benchmarks/linux_executables/505.mcf_r/mcf_r_base.mytest-m64 ../CS810_resources/CPU2017_benchmarks/linux_executables/505.mcf_r/inp.in
XALANCBMK := ../CS810_resources/CPU2017_benchmarks/linux_executables/523.xalancbmk_r/cpuxalan_r_base.mytest-m64 -v ../CS810_resources/CPU2017_benchmarks/linux_executables/523.xalancbmk_r/allbooks.xml ../CS810_resources/CPU2017_benchmarks/linux_executables/523.xalancbmk_r/xalanc.xsl
GCC := ../CS810_resources/CPU2017_benchmarks/linux_executables/502.gcc_r/cpugcc_r_base.mytest-m64 ../CS810_resources/CPU2017_benchmarks/linux_executables/502.gcc_r/scilab.c -O3 -finline-limit=0 -fif-conversion -fif-conversion2 -o ../CS810_resources/CPU2017_benchmarks/linux_executables/502.gcc_r/scilab.opts-O3_-finline-limit_0_-fif-conversion_-fif-conversion2.s
LBM := ../CS810_resources/CPU2017_benchmarks/linux_executables/519.lbm_r/lbm_r_base.mytest-m64 300 ../CS810_resources/CPU2017_benchmarks/linux_executables/519.lbm_r/lbm.in 0 0 ../CS810_resources/CPU2017_benchmarks/linux_executables/519.lbm_r/100_100_130_cf_a.of

namd: insmixtool
	./$(PIN_ROOT)/pin -t myTool/obj-intel64/insmix.so -o insmix-namd.log -- $(NAMD)

mcf: insmixtool
	./$(PIN_ROOT)/pin -t myTool/obj-intel64/insmix.so -o insmix-mcf.log -- $(MCF)

xalancbmk: insmixtool
	./$(PIN_ROOT)/pin -t myTool/obj-intel64/insmix.so -o insmix-xalancbmk.log -- $(XALANCBMK)

gcc: insmixtool
	./$(PIN_ROOT)/pin -t myTool/obj-intel64/insmix.so -o insmix-gcc.log -- $(GCC)

lbm: insmixtool
	./$(PIN_ROOT)/pin -t myTool/obj-intel64/insmix.so -o insmix-lbm.log -- $(LBM)

runall: namd mcf xalancbmk gcc lbm

insmixtool:
	cd myTool && $(MAKE) insmix.test

clean:
	cd myTool && $(MAKE) clean