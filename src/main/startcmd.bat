@echo off
REM  This file is part of the DITA Open Toolkit project
REM  See the accompanying license.txt file for applicable licenses.
REM  (c) Copyright IBM Corp. 2006 All Rights Reserved.
echo "NOTE: The startcmd.bat has been deprecated, use the dita.bat command instead."
pause

REM Get the absolute path of DITAOT's home directory
set DITA_DIR=%~dp0

REM Set environment variables
set ANT_OPTS=-Xmx512m %ANT_OPTS%
set ANT_OPTS=%ANT_OPTS% -Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl
set ANT_HOME=%DITA_DIR%
set PATH=%DITA_DIR%\bin;%PATH%
set CLASSPATH=%DITA_DIR%lib;%DITA_DIR%lib\dost.jar;%DITA_DIR%lib\commons-codec.jar;%DITA_DIR%lib\xml-resolver.jar;%DITA_DIR%lib\icu4j.jar;%CLASSPATH%
set CLASSPATH=%DITA_DIR%lib\xercesImpl.jar;%DITA_DIR%lib\xml-apis.jar;%CLASSPATH%
set CLASSPATH=%DITA_DIR%lib\saxon.jar;%DITA_DIR%lib\saxon-dom.jar;%CLASSPATH%
start "DITA-OT" cmd.exe
