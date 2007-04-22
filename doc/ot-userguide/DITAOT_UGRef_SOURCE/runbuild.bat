@echo off
REM
REM Windows batch script that starts a build by invoking Ant.
REM
REM The script does two things in addition to starting Ant:
REM
REM 1. Sets the basedir property to the location of the Toolkit directory.
REM
REM 2. Specifies the -logger option that causes the Toolkit to send some build messages
REM    to the console and logs all build messages to a disk file.
REM
ant -Dbasedir=%DITA_DIR% -f ant_scripts\DITAOTUG_all.xml -logger org.dita.dost.log.DITAOTBuildLogger %1
