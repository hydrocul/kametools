#!/bin/bash

if [ ! -f buildlib.tmp ] ; then
wget --no-check-certificate https://github.com/hydrocul/kamebuild/raw/master/buildlib.sh
mv buildlib.sh buildlib.tmp
fi

. buildlib.tmp

jarname=hydrocul-kametools.jar
fatjarname=hydrocul-kametools.jar
mainclass=hydrocul.kametools.Main

case $1 in
################################
all)
################################
buildlib_all $jarname

;;
################################
compile)
################################
buildlib_dl_commons_cli
buildlib_dl_kameutil
buildlib_dl_si4j
buildlib_dl_groovy
buildlib_dl_htmlunit

buildlib_compile

;;
################################
build-jar)
################################
buildlib_build_jar $jarname

;;
################################
build-fatjar)
################################
buildlib_build_fatjar $fatjarname $mainclass

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



