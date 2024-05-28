#!/bin/bash

main="com.puppycrawl.tools.checkstyle.Main"
new=/tmp/checkcliargs-new
old=/tmp/checkcliargs-old

java -cp "lib/test/*" $main --help >$new
java -cp "examples/lib/test/*" $main --help >$old

if [ "$1" == "-v" ]; then
	code --wait --diff $old $new
else
	diff $old $new
fi

rm -rf $new $old
