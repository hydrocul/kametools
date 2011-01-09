#!/bin/sh

if [ -z "$KT_HOME" ] ; then
  KT_HOME="."
fi

KT_PLATFORM=
if [ `uname` = "Darwin" ] ; then
  KT_PLATFORM="Mac"
fi

scala -Dkt.platform=$KT_PLATFORM -cp "$KT_HOME/class:$KT_HOME/lib/*" hydrocul.kametools.Main "$@"