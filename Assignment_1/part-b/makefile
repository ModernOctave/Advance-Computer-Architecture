NAMD := ../CS810_resources/CPU2017_benchmarks/linux_executables/508.namd_r/namd_r_base.mytest-m64 --input ../CS810_resources/CPU2017_benchmarks/linux_executables//508.namd_r/apoa1.input --iterations 5
MCF := ../CS810_resources/CPU2017_benchmarks/linux_executables/505.mcf_r/mcf_r_base.mytest-m64 ../CS810_resources/CPU2017_benchmarks/linux_executables/505.mcf_r/inp.in
XALANCBMK := ../CS810_resources/CPU2017_benchmarks/linux_executables/523.xalancbmk_r/cpuxalan_r_base.mytest-m64 -v ../CS810_resources/CPU2017_benchmarks/linux_executables/523.xalancbmk_r/allbooks.xml ../CS810_resources/CPU2017_benchmarks/linux_executables/523.xalancbmk_r/xalanc.xsl
GCC := ../CS810_resources/CPU2017_benchmarks/linux_executables/502.gcc_r/cpugcc_r_base.mytest-m64 ../CS810_resources/CPU2017_benchmarks/linux_executables/502.gcc_r/scilab.c -O3 -finline-limit=0 -fif-conversion -fif-conversion2 -o ../CS810_resources/CPU2017_benchmarks/linux_executables/502.gcc_r/scilab.opts-O3_-finline-limit_0_-fif-conversion_-fif-conversion2.s
LBM := ../CS810_resources/CPU2017_benchmarks/linux_executables/519.lbm_r/lbm_r_base.mytest-m64 300 ../CS810_resources/CPU2017_benchmarks/linux_executables/519.lbm_r/lbm.in 0 0 ../CS810_resources/CPU2017_benchmarks/linux_executables/519.lbm_r/100_100_130_cf_a.of

# PERF := perf stat -a -I 10 -e cycles,instructions,branches,branch-misses,L1-dcache-loads,L1-dcache-stores,LLC-loads,LLC-stores,L1-dcache-load-misses,LLC-load-misses,LLC-store-misses,mem-loads,mem-stores,power/energy-cores/

PERF1 := perf stat -a -I 10 -r 10 -e cycles,instructions,branches,branch-misses,L1-dcache-loads,L1-dcache-stores,L1-dcache-load-misses,mem-loads,mem-stores,power/energy-cores/

PERF2 := perf stat -a -I 10 -r 10 -e LLC-loads,LLC-stores,LLC-load-misses,LLC-store-misses

all: namd mcf xalancbmk gcc lbm

namd:
	$(PERF1) -o perf-namd.1.log $(NAMD)
	$(PERF2) -o perf-namd.2.log $(NAMD)

mcf:
	$(PERF1) -o perf-mcf.1.log $(MCF)
	$(PERF2) -o perf-mcf.2.log $(MCF)

xalancbmk:
	$(PERF1) -o perf-xalancbmk.1.log $(XALANCBMK)
	$(PERF2) -o perf-xalancbmk.2.log $(XALANCBMK)

gcc:
	$(PERF1) -o perf-gcc.1.log $(GCC)
	$(PERF2) -o perf-gcc.2.log $(GCC)

lbm:
	$(PERF1) -o perf-lbm.1.log $(LBM)
	$(PERF2) -o perf-lbm.2.log $(LBM)

plot-namd:
	python3 plot.py namd

plot-mcf:
	python3 plot.py mcf

plot-xalancbmk:
	python3 plot.py xalancbmk

plot-gcc:
	python3 plot.py gcc

plot-lbm:
	python3 plot.py lbm

plot-all: plot-namd plot-mcf plot-xalancbmk plot-gcc plot-lbm

clean:
	rm -f perf-*.log
