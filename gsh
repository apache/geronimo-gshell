#!/bin/sh

JAVA_OPTS="$JAVA_OPTS -ea" `dirname $0`/target/gshell-*/bin/gsh -e "$@"
