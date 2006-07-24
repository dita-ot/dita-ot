REM (c) Copyright IBM Corp. 2006 All Rights Reserved.

REM Get the absolute path of DITAOT's home directory
set DITA_DIR=%~dp0

set ANT_HOME=%DITA_DIR%tools\ant
set PATH=%PATH%;%DITA_DIR%tools\ant\bin;
set CLASSPATH=%CLASSPATH%;%DITA_DIR%lib\dost.jar;%DITA_DIR%lib\fop.jar;%DITA_DIR%lib\avalon-framework-cvs-20020806.jar;%DITA_DIR%lib\batik.jar;%DITA_DIR%lib\xalan.jar;%DITA_DIR%lib\xercesImpl.jar;%DITA_DIR%lib\serializer.jar;%DITA_DIR%lib\xml-apis.jar;

set ANT_CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:setupArgs
if ""%1""=="""" goto doneStart
set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% %1
shift
goto setupArgs

:doneStart
ant %ANT_CMD_LINE_ARGS%

