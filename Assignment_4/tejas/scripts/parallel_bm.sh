#Parallel Benchmark Suite. Contains parsec, splash, parboil, custom-benchmarks

if [ "$#" -ne 4 ]; then
    echo "Illegal parameters"
	echo "Usage bash parallel_bm.sh jarfile configfile statsdir outputsdir"
	exit
fi

jarfile=$1
configfile=$2
statsdir=$3
outputsdir=$4

parsecdir="/mnt/srishtistr0/home/eldhose/PARSEC/parsec-2.1-core"
splash2dir="/local_scratch/mayur/jitter-code/splash2"
parboildir="/mnt/srishtistr0/home/eldhose/parboil"
custombmdir=

echo "Please don't use forward slash at the end of directory path"

echo "processing fmm"
java -Xmx1g -jar $jarfile $configfile $statsdir/fmm $splash2dir/codes/apps/fmm/FMM > $outputsdir/fmm 2>&1

echo "processing fft"
java -Xmx1g -jar $jarfile $configfile $statsdir/fft $splash2dir/codes/kernels/fft/FFT -p32 -m24 > $outputsdir/fft 2>&1



echo "processing lu_contiguous"
java -Xmx1g -jar $jarfile $configfile $statsdir/lu_contiguous $splash2dir/codes/kernels/lu/contiguous_blocks/LU -n4096 -p32 -b8 > $outputsdir/lu_contiguous 2>&1


echo "processing radiosity"
java -Xmx1g -jar $jarfile $configfile $statsdir/radiosity $splash2dir/codes/apps/radiosity/RADIOSITY -p 32 -batch -largeroom > $outputsdir/radiosity 2>&1


echo "processing blackscholes"
java -Xmx1g -jar $jarfile $configfile $statsdir/blackscholes $parsecdir/pkgs/apps/blackscholes/inst/amd64-linux.gcc-hooks/bin/blackscholes 32 $parsecdir/pkgs/apps/blackscholes/run/in_64K.txt /mnt/srishtistr0/home/eldhose/parsec-2.1-core/pkgs/apps/blackscholes/run/prices.txt > $outputsdir/blackscholes 2>&1


echo "processing fluidanimate"
java -Xmx1g -jar $jarfile $configfile $statsdir/fluid $parsecdir/pkgs/apps/fluidanimate/inst/amd64-linux.gcc-hooks/bin/fluidanimate 32 5 $parsecdir/pkgs/apps/fluidanimate/run/in_300K.fluid $parsecdir/pkgs/apps/fluidanimate/run/out.fluid > $outputsdir/fluidanimate 2>&1


echo "processing swaptions"
java -Xmx1g -jar $jarfile $configfile $statsdir/swaptions $parsecdir/pkgs/apps/swaptions/inst/amd64-linux.gcc-hooks/bin/swaptions -ns 64 -sm 20000 -nt 32 > $outputsdir/swaptions 2>&1


echo "processing streamcluster"
java -Xmx1g -jar $jarfile $configfile $statsdir/stream $parsecdir/pkgs/kernels/streamcluster/inst/amd64-linux.gcc-hooks/bin/streamcluster 10 20 128 16384 16384 1000 none $parsecdir/pkgs/kernels/streamcluster/run/output.txt 32 > $outputsdir/streamcluster 2>&1


echo "processing bodytrack"
#java -Xmx1g -jar $jarfile $configfile $statsdir/bodytrack $parsecdir/pkgs/apps/bodytrack/inst/amd64-linux.gcc-hooks/bin/bodytrack $parsecdir/pkgs/apps/bodytrack/run/sequenceB_4 4 4 4000 5 0 32 > $outputsdir/bodytrack 2>&1


echo "processing freqmine"
#java -Xmx1g -jar $jarfile $configfile $statsdir/freqmine $parsecdir/pkgs/apps/freqmine/inst/amd64-linux.gcc-hooks/bin/freqmine $parsecdir/pkgs/apps/freqmine/run/kosarak_250k.dat 790freqmine > $outputsdir/freqmine 2>&1


echo "processing vips"
#java -Xmx1g -jar $jarfile $configfile $statsdir/vips  $parsecdir/pkgs/apps/vips/inst/amd64-linux.gcc-hooks/bin/vips im_benchmark $parsecdir/pkgs/apps/vips/run/bigben_2662x5500.v $parsecdir/pkgs/apps/vips/run/output.v > $outputsdir/vips 2>&1


echo "processing histo"
java -Xmx1g -jar $jarfile $configfile $statsdir/histo $parboildir/benchmarks/histo/build/omp_base_default/histo -i $parboildir/datasets/histo/default/input/img.bin -o $parboildir/benchmarks/histo/run/default/ref.bmp -- 20 4 > $outputsdir/histo 2>&1


echo "processing stencil"
java -Xmx1g -jar $jarfile $configfile $statsdir/stencil $parboildir/benchmarks/stencil/build/omp_base_default/stencil -i $parboildir/datasets/stencil/small/input/128x128x32.bin -o $parboildir/benchmarks/stencil/run/small/128x128x32.out -- 128 128 32 100 > $outputsdir/stencil 2>&1


echo "processing radix"
#java -Xmx1g -jar $jarfile $configfile $statsdir/radix $splash2dir/codes/kernels/radix/RADIX -p32 -n7666996 -r4 -m524288 > $outputsdir/radix 2>&1



#echo "processing "
#java -Xmx1g -jar $jarfile > $outputsdir/ 2>&1


echo "OVER!!!"
