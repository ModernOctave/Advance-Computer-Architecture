#!/bin/bash
#echo "received pid = "$1
child_pids=(`pgrep -P $1`)
#echo "child pids = "${child_pids[@]}
let "len=${#child_pids[@]}-1"
for (( i=$len; i>=0; i-- ))
do
pid=${child_pids[$i]}
#echo "processing child "$pid" : "
#ps -p $pid
if [[ ( "$pid" != "$1" ) && ( "$pid" != "$$" ) ]]
then
bash $0 $pid
echo "killing.."
ps -p $pid
kill -9 $pid
echo
fi
done
benchmarkPID=`cat "/tmp/"$1"_benchmarkPID.txt"`
rm "/tmp/"$1"_benchmarkPID.txt"
echo "kill script received pinbin PID as "$benchmarkPID
kill -9 $benchmarkPID
