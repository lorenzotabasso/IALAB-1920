EXPERIMENT="E1"
OUTFILE=$EXPERIMENT"_out.txt"
MEM="14G"

cd ../out/static/
ORDERINGS=("mindegree" "minfill")
VERBOSE="false"
JSON="../../input/static/Avg_queries.json"
NETWORKS="earthquake asia sachs alarm win95pts insurance munin_full pigs link andes"
PRUNING="false false false"

echo "last execution " $(date) >>$OUTFILE
for network in $NETWORKS; do # loop trough networks
  echo "processing..."
  for i in 0 1; do # loop trough orderings
    echo "Network:"$network "Order:"${ORDERINGS[i]} "Pruning:"$PRUNING # to stdout

    echo __________________________________________________________________ >>$OUTFILE
    echo "Network:"$network "Order:"${ORDERINGS[i]} "Pruning:"$PRUNING >>$OUTFILE
    echo >>$OUTFILE
    java -Xms$MEM -jar static.jar ${ORDERINGS[i]} $VERBOSE $JSON $network $PRUNING >>$OUTFILE 2>&1

    echo __________________________________________________________________ >>$OUTFILE
    echo >>$OUTFILE
    echo >>$OUTFILE
  done
done
