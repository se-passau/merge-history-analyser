#!/bin/bash

cd /home/martin/hiwi_job/projekte/voldemort/
if [ -e "gradlew" ]
then
    ./gradlew clean jar
fi
    echo NO BUILD POSSIBLE
