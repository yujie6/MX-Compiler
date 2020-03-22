#!/bin/zsh
cd ..
./gradlew installDist
cp -r ./build/install/Compiler/* release/
rm release/lib/icu*
