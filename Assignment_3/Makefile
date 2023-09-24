run: build
	java -jar tejas/jars/tejas.jar config_base.xml gcc.stat traces/gcc

build: prefetcher
	cd tejas && ant make-jar

prefetcher:
	cd tejas/src/simulator/pipeline/branchpredictor/TAGESCL/ && make

clean: clear
	cd tejas && ant clean
	cd tejas/src/simulator/pipeline/branchpredictor/TAGESCL/ && make clean
	rm config_custom.*.xml run.*.stat

clear:
	rm config_custom.*.xml run.*.stat