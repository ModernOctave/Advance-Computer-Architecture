rm tests/run.out
g++ tests/hello_world.cpp -o tests/hello_world.o
java -jar ../jars/tejas.jar ../bin/config/config.xml tests/run.out tests/hello_world.o
cat tests/run.out
