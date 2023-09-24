javaver=java-11
ifeq ($(shell java -version 2>&1|grep 1.8.0 >/dev/null; printf $$?),0)
	javaver=java-8
endif

all:
	javac -h . TAGESCLInvoker.java
	gcc -fPIC -I/usr/lib/jvm/$(javaver)-openjdk-amd64/include/ -I/usr/lib/jvm/$(javaver)-openjdk-amd64/include/linux/ -shared -o libnative.so pipeline_branchpredictor_TAGESCL_TAGESCLInvoker.cc
clean:
	rm -f *.h
	rm -f *.class
	rm -f *.so
