#!/bin/bash
CLASSPATH=""
rm -rf ./pcells
unzip ../dist/pcells.zip > /dev/null  
for f in `find ./pcells -type f -name '*.*' `; do CLASSPATH=$CLASSPATH:$f; done
#echo Classpath: $CLASSPATH
prog=$1
shift
cmd=${1:-""}
shift
jython -J-cp $CLASSPATH -J-Xmx1024m  -J-Xms128m ${prog} -v localhost 22224 admin ${HOME}/.ssh/id_dsa.der "$cmd" "$*" 
