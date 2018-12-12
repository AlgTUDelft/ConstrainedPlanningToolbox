@echo off
SET mylibpath=%PATH%;%cd%\lpsolve_64
java -cp Server.jar;libs/* -Djava.library.path="%mylibpath%" executables.Server %*
