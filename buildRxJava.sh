#!/bin/bash

cd /home/martin/hiwi_job/projekte/RxJava
if [ -e "gradlew" ]
then
    git apply gradle.patch
    ./gradlew clean jar
else
    echo NO BUILD POSSIBLE
fi