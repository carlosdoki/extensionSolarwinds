#!/bin/bash
echo "Inicio - $(date)" >> solar.log
/opt/AppDynamics/Controller/jre/bin/java -jar SolarWinds.jar "$@"
 for i; do 
    echo $i >> solar.log 
 done
echo "Fim - $(date)" >> solar.log
