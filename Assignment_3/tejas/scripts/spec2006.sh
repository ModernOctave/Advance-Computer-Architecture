#!/bin/bash

if [ "$#" -ne 6 ]; then
    echo "Illegal parameters"
	echo "Usage bash spec2006.sh script_status_file jarfile configfile statsdir outputsdir scriptdir"
	exit
fi

rm $1
jarfile=$2
configfile=$3
statsdir=$4
outputsdir=$5
scriptdir=$6

echo "Please don't use forward slash at the end of directory path"

#bash "$scriptdir"spec2006_single.sh "perlbench" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1
bash "$scriptdir"spec2006_single.sh "bzip2" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "gcc" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "bwaves" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "gamess" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "mcf" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "milc" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "zeusmp" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "gromacs" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "cactusADM" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "leslie3d" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "namd" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "gobmk" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
#bash "$scriptdir"spec2006_single.sh "dealII" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1
bash "$scriptdir"spec2006_single.sh "soplex" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "povray" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "calculix" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "hmmer" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "sjeng" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "GemsFDTD" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "libquantum" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "h264ref" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "tonto" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "lbm" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "omnetpp" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "astar" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "wrf" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "sphinx3" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
bash "$scriptdir"spec2006_single.sh "xalancbmk" $jarfile $configfile $statsdir $outputsdir >>  $1 2>&1 
