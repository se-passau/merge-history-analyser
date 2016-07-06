#!/usr/bin/env bash

cat collectResults >> "$1"/build.gradle

cd "$1" || exit 1

if [ -e "gradlew" ]
then
    ./gradlew cleanTest
    ./gradlew -Dtest.single=QueryKeyResultTest test
    ./gradlew collectResults
else
    echo NO BUILD POSSIBLE
fi