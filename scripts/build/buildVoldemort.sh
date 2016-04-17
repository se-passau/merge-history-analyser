#!/bin/bash

cd "$1"

if [ -e "gradlew" ]
then
    ./gradlew clean jar
else
    echo NO BUILD POSSIBLE
fi