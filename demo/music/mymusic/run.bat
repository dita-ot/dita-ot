@echo off

echo The generated library will be placed in the music/ subdirectory.


REM Get the absolute path of DITAOT's home directory
set DITA_DIR=..\..\..\

REM Set environment variables
set ANT_OPTS=-Xmx800m %ANT_OPTS%
set ANT_OPTS=%ANT_OPTS% -Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl
set ANT_HOME=%DITA_DIR%tools\ant
set PATH=%DITA_DIR%tools\ant\bin;%PATH%
set CLASSPATH=%DITA_DIR%lib;%DITA_DIR%lib\dost.jar;%DITA_DIR%lib\resolver.jar;%DITA_DIR%lib\icu4j.jar;%CLASSPATH%
set CLASSPATH=%DITA_DIR%lib\saxon\saxon9.jar;%DITA_DIR%lib\saxon\saxon9-dom.jar;%CLASSPATH%
set CLASSPATH=%DITA_DIR%lib\saxon\saxon9-dom4j.jar;%DITA_DIR%lib\saxon\saxon9-jdom.jar;%CLASSPATH%
set CLASSPATH=%DITA_DIR%lib\saxon\saxon9-s9api.jar;%DITA_DIR%lib\saxon\saxon9-sql.jar;%CLASSPATH%
set CLASSPATH=%DITA_DIR%lib\saxon\saxon9-xom.jar;%DITA_DIR%lib\saxon\saxon9-xpath.jar;%DITA_DIR%lib\saxon\saxon9-xqj.jar;%CLASSPATH%
ant -f music.xml

