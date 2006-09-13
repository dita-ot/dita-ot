REM  This file is part of the DITA Open Toolkit project hosted on 
REM  Sourceforge.net. See the accompanying license.txt file for 
REM  applicable licenses.
REM  (c) Copyright IBM Corp. 2006 All Rights Reserved.

REM Get the absolute path of DITAOT's home directory
set DITA_DIR=%~dp0

REM Set environment variables
set ANT_OPTS=-Xmx512m %ANT_OPTS%
set ANT_HOME=%DITA_DIR%tools\ant
set PATH=%DITA_DIR%tools\ant\bin;%PATH%
set CLASSPATH=%DITA_DIR%lib;%DITA_DIR%lib\dost.jar;%DITA_DIR%lib\resolver.jar;%DITA_DIR%lib\fop.jar;%DITA_DIR%lib\avalon-framework-cvs-20020806.jar;%DITA_DIR%lib\batik.jar;%DITA_DIR%lib\xalan.jar;%DITA_DIR%lib\xercesImpl.jar;%DITA_DIR%lib\xml-apis.jar;%DITA_DIR%lib\icu4j.jar;%CLASSPATH%
start "DITA-OT" cmd.exe