pstree -p $1 | grep pinbin | cut -d"(" -f3 | cut -d")" -f1 > "/tmp/"$1"_benchmarkPID.txt"
