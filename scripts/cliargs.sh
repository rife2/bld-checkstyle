#!/bin/bash

java -cp "examples/lib/.sandbox/bld-checkstyle/*" com.puppycrawl.tools.checkstyle.Main --help |\
grep "^  -.*" |\
sed -e "s/  -/-/" -e "s/[,=].*//" -e '/-V/d' -e '/-h/d' |\
sort > "src/test/resources/checkstyle-args.txt"
