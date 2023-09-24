PIN_ROOT := pin-3.28-98749-g6643ecee5-gcc-linux
MAKE := make PIN_ROOT=../$(PIN_ROOT)

NAMD := ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/508.namd_r/namd_r_base.mytest-m64 --input ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables//508.namd_r/apoa1.input --iterations 5
MCF := ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/505.mcf_r/mcf_r_base.mytest-m64 ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/505.mcf_r/inp.in
XALANCBMK := ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/523.xalancbmk_r/cpuxalan_r_base.mytest-m64 -v ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/523.xalancbmk_r/allbooks.xml ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/523.xalancbmk_r/xalanc.xsl
GCC := ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/502.gcc_r/cpugcc_r_base.mytest-m64 ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/502.gcc_r/scilab.c -O3 -finline-limit=0 -fif-conversion -fif-conversion2 -o ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/502.gcc_r/scilab.opts-O3_-finline-limit_0_-fif-conversion_-fif-conversion2.s
LBM := ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/519.lbm_r/lbm_r_base.mytest-m64 300 ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/519.lbm_r/lbm.in 0 0 ../Assignment_1/CS810_resources/CPU2017_benchmarks/linux_executables/519.lbm_r/100_100_130_cf_a.of

namd: dependencytool
	./$(PIN_ROOT)/pin -t myTool/obj-intel64/dependency.so -o dependency-namd.log -- $(NAMD)

mcf: dependencytool
	./$(PIN_ROOT)/pin -t myTool/obj-intel64/dependency.so -o dependency-mcf.log -- $(MCF)

xalancbmk: dependencytool
	./$(PIN_ROOT)/pin -t myTool/obj-intel64/dependency.so -o dependency-xalancbmk.log -- $(XALANCBMK)

gcc: dependencytool
	./$(PIN_ROOT)/pin -t myTool/obj-intel64/dependency.so -o dependency-gcc.log -- $(GCC)

lbm: dependencytool
	./$(PIN_ROOT)/pin -t myTool/obj-intel64/dependency.so -o dependency-lbm.log -- $(LBM)

runall: namd mcf xalancbmk gcc lbm

dependencytool:
	cd myTool && $(MAKE) dependency.test

clean:
	cd myTool && $(MAKE) clean