@echo off

SET KT_HOME=%~dp0

scala -cp %KT_HOME%\class;%KT_HOME%\lib\* hydrocul.kametools.Main %*
