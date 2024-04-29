#!/bin/bash

MAIN="com.puppycrawl.tools.checkstyle.Main"
TMPNEW=/tmp/checkcliargs-new
TMPOLD=/tmp/checkcliargs-old

java -cp "lib/test/*" $MAIN --help >$TMPNEW
java -cp "examples/lib/test/*" $MAIN --help >$TMPOLD

if [ "$1" == "-v" ]; then
	code --wait --diff $TMPOLD $TMPNEW
else
	diff $TMPOLD $TMPNEW
fi

rm -rf $TMPNEW $TMPOLD
