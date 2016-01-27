#!/bin/bash

cd /home/martin/hiwi_job/projekte/voldemort/
if [ -e "gradlew" ]
then
    ./gradlew clean jar
else
    echo NO BUILD POSSIBLE
fi