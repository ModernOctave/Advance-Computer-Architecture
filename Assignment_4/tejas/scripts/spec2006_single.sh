#!/bin/bash
#usage bash spec2006_tejasBase_runBenchmark.sh <benchmark>
#export LD_LIBRARY_PATH="/mnt/srishtistr0/scratch/rajshekar/tejas/lib/"

if [ "$#" -ne 5 ]; then
    echo "Illegal parameters"
	echo "Usage bash spec2006.sh benchmark jarfile configfile statsdir outputsdir"
	exit
fi

#jarfile="/mnt/srishtistr0/home/prathmesh/workspace/Tejas/jars/tejas.jar"
jarfile=$2

#configfile="/mnt/srishtistr0/home/prathmesh/workspace/Tejas/src/simulator/config/config.xml"
configfile=$3

#outputfiledir="/mnt/srishtistr0/home/prathmesh/scripts/stats"$2
outputfiledir=$4
outputfile=""

#stdoutfiledir="/mnt/srishtistr0/home/prathmesh/scripts/outputs"$2
stdoutfiledir=$5
stdoutfile=""

specpath="/mnt/srishtistr0/scratch/rajshekar/benchmarks/cpu2006/benchspec/CPU2006"

if [ $1 = perlbench ]
then
executable="$specpath/400.perlbench/run/run_base_test_amd64-m64-gcc43-nn.0000/perlbench_base.amd64-m64-gcc43-nn -I. -I./lib $specpath/400.perlbench/run/run_base_test_amd64-m64-gcc43-nn.0000/test.pl"
outputfile=$outputfiledir/"perlbench"
stdoutfile=$stdoutfiledir/"perlbench"
else
if [ $1 = "bzip2" ]
then
executable="$specpath/401.bzip2/run/run_base_test_amd64-m64-gcc43-nn.0000/bzip2_base.amd64-m64-gcc43-nn $specpath/401.bzip2/run/run_base_test_amd64-m64-gcc43-nn.0000/input.program 5"
#executable="$specpath/401.bzip2/run/run_base_ref_amd64-m64-gcc43-nn.0001/bzip2_base.amd64-m64-gcc43-nn $specpath/401.bzip2/run/run_base_ref_amd64-m64-gcc43-nn.0001/input.source 280"
outputfile=$outputfiledir/"bzip2"
stdoutfile=$stdoutfiledir/"bzip2"
else
if [ $1 = "gcc" ]
then
executable="$specpath/403.gcc/run/run_base_test_amd64-m64-gcc43-nn.0000/gcc_base.amd64-m64-gcc43-nn $specpath/403.gcc/run/run_base_test_amd64-m64-gcc43-nn.0000/cccp.i -o $specpath/403.gcc/run/run_base_test_amd64-m64-gcc43-nn.0000/cccp.s"
#executable="$specpath/403.gcc/run/run_base_ref_amd64-m64-gcc43-nn.0001/gcc_base.amd64-m64-gcc43-nn $specpath/403.gcc/run/run_base_ref_amd64-m64-gcc43-nn.0001/166.i -o $specpath/403.gcc/run/run_base_ref_amd64-m64-gcc43-nn.0001/166.s"
outputfile=$outputfiledir/"gcc"
stdoutfile=$stdoutfiledir/"gcc"
else
if [ $1 = "bwaves" ]
then
executable="$specpath/410.bwaves/run/run_base_test_amd64-m64-gcc43-nn.0000/bwaves_base.amd64-m64-gcc43-nn"
#executable="$specpath/410.bwaves/run/run_base_ref_amd64-m64-gcc43-nn.0000/bwaves_base.amd64-m64-gcc43-nn"
outputfile=$outputfiledir/"bwaves"
stdoutfile=$stdoutfiledir/"bwaves"
else
if [ $1 = "gamess" ]
then
executable="$specpath/416.gamess/run/run_base_test_amd64-m64-gcc43-nn.0000/gamess_base.amd64-m64-gcc43-nn"
#executable="$specpath/416.gamess/run/run_base_ref_amd64-m64-gcc43-nn.0000/gamess_base.amd64-m64-gcc43-nn"
outputfile=$outputfiledir/"gamess"
stdoutfile=$stdoutfiledir/"gamess"
else
if [ $1 = mcf ]
then
executable="$specpath/429.mcf/run/run_base_test_amd64-m64-gcc43-nn.0000/mcf_base.amd64-m64-gcc43-nn $specpath/429.mcf/run/run_base_test_amd64-m64-gcc43-nn.0000/inp.in"
#executable="$specpath/429.mcf/run/run_base_ref_amd64-m64-gcc43-nn.0000/mcf_base.amd64-m64-gcc43-nn $specpath/429.mcf/run/run_base_ref_amd64-m64-gcc43-nn.0000/inp.in"
outputfile=$outputfiledir/"mcf"
stdoutfile=$stdoutfiledir/"mcf"
else
if [ $1 = "milc" ]
then
executable="$specpath/433.milc/run/run_base_test_amd64-m64-gcc43-nn.0000/milc_base.amd64-m64-gcc43-nn"
#executable="$specpath/433.milc/run/run_base_ref_amd64-m64-gcc43-nn.0000/milc_base.amd64-m64-gcc43-nn"
outputfile=$outputfiledir/"milc"
stdoutfile=$stdoutfiledir/"milc"
else
if [ $1 = "zeusmp" ]
then
cd "$specpath/434.zeusmp/run/run_base_test_amd64-m64-gcc43-nn.0000"
executable="$specpath/434.zeusmp/run/run_base_test_amd64-m64-gcc43-nn.0000/zeusmp_base.amd64-m64-gcc43-nn"
#cd "$specpath/434.zeusmp/run/run_base_ref_amd64-m64-gcc43-nn.0000"
#executable="$specpath/434.zeusmp/run/run_base_ref_amd64-m64-gcc43-nn.0000/zeusmp_base.amd64-m64-gcc43-nn"
outputfile=$outputfiledir/"zeusmp"
stdoutfile=$stdoutfiledir/"zeusmp"
else
if [ $1 = gromacs ]
then
executable="$specpath/435.gromacs/run/run_base_test_amd64-m64-gcc43-nn.0000/gromacs_base.amd64-m64-gcc43-nn -silent -deffnm $specpath/435.gromacs/run/run_base_test_amd64-m64-gcc43-nn.0000/gromacs -nice 0"
#executable="$specpath/435.gromacs/run/run_base_ref_amd64-m64-gcc43-nn.0000/gromacs_base.amd64-m64-gcc43-nn -silent -deffnm $specpath/435.gromacs/run/run_base_ref_amd64-m64-gcc43-nn.0000/gromacs -nice 0"
outputfile=$outputfiledir/"gromacs"
stdoutfile=$stdoutfiledir/"gromacs"
else
if [ $1 = cactusADM ]
then
executable="$specpath/436.cactusADM/run/run_base_test_amd64-m64-gcc43-nn.0000/cactusADM_base.amd64-m64-gcc43-nn $specpath/436.cactusADM/run/run_base_test_amd64-m64-gcc43-nn.0000/benchADM.par"
#executable="$specpath/436.cactusADM/run/run_base_ref_amd64-m64-gcc43-nn.0000/cactusADM_base.amd64-m64-gcc43-nn $specpath/436.cactusADM/run/run_base_ref_amd64-m64-gcc43-nn.0000/benchADM.par"
outputfile=$outputfiledir/"cactusADM"
stdoutfile=$stdoutfiledir/"cactusADM"
else
if [ $1 = "leslie3d" ]
then
executable="$specpath/437.leslie3d/run/run_base_test_amd64-m64-gcc43-nn.0000/leslie3d_base.amd64-m64-gcc43-nn"
#executable="$specpath/437.leslie3d/run/run_base_ref_amd64-m64-gcc43-nn.0000/leslie3d_base.amd64-m64-gcc43-nn"
outputfile=$outputfiledir/"leslie3d"
stdoutfile=$stdoutfiledir/"leslie3d"
else
if [ $1 = namd ]
then
executable="$specpath/444.namd/run/run_base_test_amd64-m64-gcc43-nn.0000/namd_base.amd64-m64-gcc43-nn --input $specpath/444.namd/run/run_base_test_amd64-m64-gcc43-nn.0000/namd.input --iterations 1 --output $specpath/444.namd/run/run_base_test_amd64-m64-gcc43-nn.0000/namd.out"
#executable="$specpath/444.namd/run/run_base_ref_amd64-m64-gcc43-nn.0000/namd_base.amd64-m64-gcc43-nn --input $specpath/444.namd/run/run_base_ref_amd64-m64-gcc43-nn.0000/namd.input --iterations 38 --output $specpath/444.namd/run/run_base_ref_amd64-m64-gcc43-nn.0000/namd.out"
outputfile=$outputfiledir/"namd"
stdoutfile=$stdoutfiledir/"namd"
else
if [ $1 = "gobmk" ]
then
executable="$specpath/445.gobmk/run/run_base_test_amd64-m64-gcc43-nn.0000/gobmk_base.amd64-m64-gcc43-nn --quiet --mode gtp"
#executable="$specpath/445.gobmk/run/run_base_ref_amd64-m64-gcc43-nn.0001/gobmk_base.amd64-m64-gcc43-nn --quiet --mode gtp"
outputfile=$outputfiledir/"gobmk"
stdoutfile=$stdoutfiledir/"gobmk"
else
if [ $1 = dealII ]
then
executable="$specpath/447.dealII/run/run_base_test_amd64-m64-gcc43-nn.0000/dealII_base.amd64-m64-gcc43-nn 8"
#does not build
outputfile=$outputfiledir/"dealII"
stdoutfile=$stdoutfiledir/"dealII"
else
if [ $1 = "soplex" ]
then
executable="$specpath/450.soplex/run/run_base_test_amd64-m64-gcc43-nn.0000/soplex_base.amd64-m64-gcc43-nn -m10000 $specpath/450.soplex/run/run_base_test_amd64-m64-gcc43-nn.0000/test.mps"
#executable="$specpath/450.soplex/run/run_base_ref_amd64-m64-gcc43-nn.0000/soplex_base.amd64-m64-gcc43-nn -s1 -e -m45000 $specpath/450.soplex/run/run_base_ref_amd64-m64-gcc43-nn.0000/pds-50.mps"
outputfile=$outputfiledir/"soplex"
stdoutfile=$stdoutfiledir/"soplex"
else
if [ $1 = povray ]
then
executable="$specpath/453.povray/run/run_base_test_amd64-m64-gcc43-nn.0000/povray_base.amd64-m64-gcc43-nn $specpath/453.povray/run/run_base_test_amd64-m64-gcc43-nn.0000/SPEC-benchmark-test.ini"
#executable="$specpath/453.povray/run/run_base_ref_amd64-m64-gcc43-nn.0000/povray_base.amd64-m64-gcc43-nn $specpath/453.povray/run/run_base_ref_amd64-m64-gcc43-nn.0000/SPEC-benchmark-ref.ini"
outputfile=$outputfiledir/"povray"
stdoutfile=$stdoutfiledir/"povray"
else
if [ $1 = calculix ]
then
executable="$specpath/454.calculix/run/run_base_test_amd64-m64-gcc43-nn.0000/calculix_base.amd64-m64-gcc43-nn -i $specpath/454.calculix/run/run_base_test_amd64-m64-gcc43-nn.0000/beampic"
#executable="$specpath/454.calculix/run/run_base_ref_amd64-m64-gcc43-nn.0000/calculix_base.amd64-m64-gcc43-nn -i $specpath/454.calculix/run/run_base_ref_amd64-m64-gcc43-nn.0000/hyperviscoplastic"
outputfile=$outputfiledir/"calculix"
stdoutfile=$stdoutfiledir/"calculix"
else
if [ $1 = hmmer ]
then
executable="$specpath/456.hmmer/run/run_base_test_amd64-m64-gcc43-nn.0000/hmmer_base.amd64-m64-gcc43-nn --fixed 0 --mean 325 --num 45000 --sd 200 --seed 0 $specpath/456.hmmer/run/run_base_test_amd64-m64-gcc43-nn.0000/bombesin.hmm"
#executable="$specpath/456.hmmer/run/run_base_ref_amd64-m64-gcc43-nn.0001/hmmer_base.amd64-m64-gcc43-nn $specpath/456.hmmer/run/run_base_ref_amd64-m64-gcc43-nn.0001/nph3.hmm $specpath/456.hmmer/run/run_base_ref_amd64-m64-gcc43-nn.0001/swiss41"
outputfile=$outputfiledir/"hmmer"
stdoutfile=$stdoutfiledir/"hmmer"
else
if [ $1 = sjeng ]
then
executable="$specpath/458.sjeng/run/run_base_test_amd64-m64-gcc43-nn.0000/sjeng_base.amd64-m64-gcc43-nn $specpath/458.sjeng/run/run_base_test_amd64-m64-gcc43-nn.0000/test.txt"
#executable="$specpath/458.sjeng/run/run_base_ref_amd64-m64-gcc43-nn.0001/sjeng_base.amd64-m64-gcc43-nn $specpath/458.sjeng/run/run_base_ref_amd64-m64-gcc43-nn.0001/ref.txt"
outputfile=$outputfiledir/"sjeng"
stdoutfile=$stdoutfiledir/"sjeng"
else
if [ $1 = "GemsFDTD" ]
then
cd "$specpath/459.GemsFDTD/run/run_base_test_amd64-m64-gcc43-nn.0000/"
executable="$specpath/459.GemsFDTD/run/run_base_test_amd64-m64-gcc43-nn.0000/GemsFDTD_base.amd64-m64-gcc43-nn"
#cd "$specpath/459.GemsFDTD/run/run_base_ref_amd64-m64-gcc43-nn.0000/"
#executable="$specpath/459.GemsFDTD/run/run_base_ref_amd64-m64-gcc43-nn.0000/GemsFDTD_base.amd64-m64-gcc43-nn"
outputfile=$outputfiledir/"GemsFDTD"
stdoutfile=$stdoutfiledir/"GemsFDTD"
else
if [ $1 = libquantum ]
then
executable="$specpath/462.libquantum/run/run_base_test_amd64-m64-gcc43-nn.0000/libquantum_base.amd64-m64-gcc43-nn 33 5"
#executable="$specpath/462.libquantum/run/run_base_ref_amd64-m64-gcc43-nn.0001/libquantum_base.amd64-m64-gcc43-nn 1397 8"
outputfile=$outputfiledir/"libquantum"
stdoutfile=$stdoutfiledir/"libquantum"
else
if [ $1 = "h264ref" ]
then
cd "$specpath/464.h264ref/run/run_base_test_amd64-m64-gcc43-nn.0000/"
executable="$specpath/464.h264ref/run/run_base_test_amd64-m64-gcc43-nn.0000/h264ref_base.amd64-m64-gcc43-nn -d foreman_test_encoder_baseline.cfg"
#cd "$specpath/464.h264ref/run/run_base_ref_amd64-m64-gcc43-nn.0001/"
#executable="$specpath/464.h264ref/run/run_base_ref_amd64-m64-gcc43-nn.0001/h264ref_base.amd64-m64-gcc43-nn -d foreman_ref_encoder_baseline.cfg"
#or
#executable="$specpath/464.h264ref/run/run_base_ref_amd64-m64-gcc43-nn.0001/h264ref_base.amd64-m64-gcc43-nn -d $specpath/464.h264ref/run/run_base_ref_amd64-m64-gcc43-nn.0001/foreman_ref_encoder_baseline.cfg"
outputfile=$outputfiledir/"h264ref"
stdoutfile=$stdoutfiledir/"h264ref"
else
if [ $1 = "tonto" ]
then
cd "$specpath/465.tonto/run/run_base_test_amd64-m64-gcc43-nn.0000/"
executable="$specpath/465.tonto/run/run_base_test_amd64-m64-gcc43-nn.0000/tonto_base.amd64-m64-gcc43-nn"
#cd "$specpath/465.tonto/run/run_base_ref_amd64-m64-gcc43-nn.0000/"
#executable="$specpath/465.tonto/run/run_base_ref_amd64-m64-gcc43-nn.0000/tonto_base.amd64-m64-gcc43-nn"
outputfile=$outputfiledir/"tonto"
stdoutfile=$stdoutfiledir/"tonto"
else
if [ $1 = lbm ]
then
executable="$specpath/470.lbm/run/run_base_test_amd64-m64-gcc43-nn.0000/lbm_base.amd64-m64-gcc43-nn 20 reference.dat 0 1 $specpath/470.lbm/run/run_base_test_amd64-m64-gcc43-nn.0000/100_100_130_cf_a.of"
#executable="$specpath/470.lbm/run/run_base_ref_amd64-m64-gcc43-nn.0000/lbm_base.amd64-m64-gcc43-nn 3000 reference.dat 0 0 $specpath/470.lbm/run/run_base_ref_amd64-m64-gcc43-nn.0000/100_100_130_ldc.of"
outputfile=$outputfiledir/"lbm"
stdoutfile=$stdoutfiledir/"lbm"
else
if [ $1 = "omnetpp" ]
then
cd "$specpath/471.omnetpp/run/run_base_test_amd64-m64-gcc43-nn.0000/"
executable="$specpath/471.omnetpp/run/run_base_test_amd64-m64-gcc43-nn.0000/omnetpp_base.amd64-m64-gcc43-nn omnetpp.ini"
#executable="$specpath/471.omnetpp/run/run_base_ref_amd64-m64-gcc43-nn.0001/omnetpp_base.amd64-m64-gcc43-nn $specpath/471.omnetpp/run/run_base_ref_amd64-m64-gcc43-nn.0001/omnetpp.ini"
outputfile=$outputfiledir/"omnetpp"
stdoutfile=$stdoutfiledir/"omnetpp"
else
if [ $1 = "astar" ]
then
cd "$specpath/473.astar/run/run_base_test_amd64-m64-gcc43-nn.0000/"
executable="$specpath/473.astar/run/run_base_test_amd64-m64-gcc43-nn.0000/astar_base.amd64-m64-gcc43-nn lake.cfg"
#cd "$specpath/473.astar/run/run_base_ref_amd64-m64-gcc43-nn.0001/"
#executable="$specpath/473.astar/run/run_base_ref_amd64-m64-gcc43-nn.0001/astar_base.amd64-m64-gcc43-nn BigLakes2048.cfg"
#or
#executable="$specpath/473.astar/run/run_base_ref_amd64-m64-gcc43-nn.0001/astar_base.amd64-m64-gcc43-nn $specpath/473.astar/run/run_base_ref_amd64-m64-gcc43-nn.0001/BigLakes2048.cfg"
outputfile=$outputfiledir/"astar"
stdoutfile=$stdoutfiledir/"astar"
else
if [ $1 = "wrf" ]
then
cd "$specpath/481.wrf/run/run_base_test_amd64-m64-gcc43-nn.0000/"
executable="$specpath/481.wrf/run/run_base_test_amd64-m64-gcc43-nn.0000/wrf_base.amd64-m64-gcc43-nn"
#cd "$specpath/481.wrf/run/run_base_ref_amd64-m64-gcc43-nn.0000/"
#executable="$specpath/481.wrf/run/run_base_ref_amd64-m64-gcc43-nn.0000/wrf_base.amd64-m64-gcc43-nn"
outputfile=$outputfiledir/"wrf"
stdoutfile=$stdoutfiledir/"wrf"
else
if [ $1 = "sphinx3" ]
then
cd "$specpath/482.sphinx3/run/run_base_test_amd64-m64-gcc43-nn.0000/"
executable="$specpath/482.sphinx3/run/run_base_test_amd64-m64-gcc43-nn.0000/sphinx_livepretend_base.amd64-m64-gcc43-nn ctlfile $specpath/482.sphinx3/run/run_base_test_amd64-m64-gcc43-nn.0000 args.an4"
#cd "$specpath/482.sphinx3/run/run_base_ref_amd64-m64-gcc43-nn.0000/"
#executable="$specpath/482.sphinx3/run/run_base_ref_amd64-m64-gcc43-nn.0000/sphinx_livepretend_base.amd64-m64-gcc43-nn ctlfile $specpath/482.sphinx3/run/run_base_ref_amd64-m64-gcc43-nn.0000/ args.an4"
outputfile=$outputfiledir/"sphinx3"
stdoutfile=$stdoutfiledir/"sphinx3"
else
if [ $1 = xalancbmk ]
then
executable="$specpath/483.xalancbmk/run/run_base_test_amd64-m64-gcc43-nn.0000/Xalan_base.amd64-m64-gcc43-nn -v $specpath/483.xalancbmk/run/run_base_test_amd64-m64-gcc43-nn.0000/test.xml $specpath/483.xalancbmk/run/run_base_test_amd64-m64-gcc43-nn.0000/xalanc.xsl"
#executable="$specpath/483.xalancbmk/run/run_base_ref_amd64-m64-gcc43-nn.0001/Xalan_base.amd64-m64-gcc43-nn -v $specpath/483.xalancbmk/run/run_base_ref_amd64-m64-gcc43-nn.0001/t5.xml $specpath/483.xalancbmk/run/run_base_ref_amd64-m64-gcc43-nn.0001/xalanc.xsl"
outputfile=$outputfiledir/"xalancbmk"
stdoutfile=$stdoutfiledir/"xalancbmk"
else
if [ $1 = "998.specrand" ]
#do not call
then
executable=""
outputfile=""
else
if [ $1 = "999.specrand" ]
#do not call
then
executable=""
outputfile=""
else
echo "invalid argument "$1
exit 1
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
fi
echo "starting : "$outputfile
java -Xmx1024m -jar $jarfile $configfile $outputfile $executable > $stdoutfile
echo "finished : "$outputfile
