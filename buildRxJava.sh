#!/bin/bash

cd /home/martin/hiwi_job/projekte/RxJava
if [ -e "gradlew" ]
then
    JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre ./gradlew clean jar
fi
echo NO BUILD POSSIBLE
