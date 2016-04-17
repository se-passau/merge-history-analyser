#!/bin/bash

cd "$1"

if [ -e "gradlew" ]
then
    git apply gradle.patch
    ./gradlew clean jar
else
    echo NO BUILD POSSIBLE
fi