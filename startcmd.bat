REM Get the absolute path of DITAOT's home directory
set DITA_DIR=%~dp0

REM Set environment variables
set ANT_HOME=%DITA_DIR%tools\ant
set PATH=%DITA_DIR%tools\ant\bin;%PATH%
set CLASSPATH=%DITA_DIR%lib\dost.jar;%DITA_DIR%lib\fop.jar;%DITA_DIR%lib\avalon-framework-cvs-20020806.jar;%DITA_DIR%lib\batik.jar;%DITA_DIR%lib\xalan.jar;%DITA_DIR%lib\xercesImpl.jar;%DITA_DIR%lib\serializer.jar;%DITA_DIR%lib\xml-apis.jar;%DITA_DIR%lib\icu4j.jar;%CLASSPATH%
start "DITA-OT" cmd.exe