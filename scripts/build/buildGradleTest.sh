#!/usr/bin/env bash

cd "$1" || exit 1

if [ -e "gradlew" ]
then
    ./gradlew clean jar
else
    echo NO BUILD POSSIBLE
fi