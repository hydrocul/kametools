#!/bin/bash

if [ ! -f buildlib.tmp ] ; then
wget --no-check-certificate https://github.com/hydrocul/kamebuild/raw/master/buildlib.sh
mv buildlib.sh buildlib.tmp
fi

. buildlib.tmp

case $1 in
################################
compile)
################################
buildlib_dl_commons_cli
buildlib_dl_kameutil
buildlib_dl_si4j
buildlib_dl_groovy
buildlib_dl_htmlunit

buildlib_compile hydrocul-kametools.jar

;;
################################
test)
################################
buildlib_test

;;
################################
build)
################################
buildlib_build_jar hydrocul-kametools.jar

;;
################################
scaladoc)
################################
buildlib_scaladoc

;;
################################
clean)
################################
buildlib_clean

;;
################################
*)
################################
echo "Usage: ./make.sh [compile|test|build|scaladoc|clean]"
echo "Default action is compile."
. make.sh compile

;;
################################
esac



