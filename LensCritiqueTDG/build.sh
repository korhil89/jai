#!/bin/bash
set -e

find src -name *.java -print >javafiles
if [ ! -d bin ]; then
    mkdir bin
fi
javac -d bin @javafiles
cp -R src/ili/jai/lenscritique/jai/TDG/* bin
jar -cf lenscritique-data-tdg.jar -C bin/ .

