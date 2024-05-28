#!/bin/bash

java -cp "lib/test/*" com.puppycrawl.tools.checkstyle.Main --help |\
grep "^  -.*" |\
sed -e "s/  -/-/" -e "s/[,=].*//" -e '/-V/d' -e '/-h/d' |\
sort > "src/test/resources/checkstyle-args.txt"
