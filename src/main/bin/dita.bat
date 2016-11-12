@echo off

REM This file is part of the DITA Open Toolkit project.
REM See the accompanying LICENSE file for applicable license.

REM Derived from Apache Ant command line tool.

REM  Licensed to the Apache Software Foundation (ASF) under one or more
REM  contributor license agreements.  See the NOTICE file distributed with
REM  this work for additional information regarding copyright ownership.
REM  The ASF licenses this file to You under the Apache License, Version 2.0
REM  (the "License"); you may not use this file except in compliance with
REM  the License.  You may obtain a copy of the License at
REM 
REM      http://www.apache.org/licenses/LICENSE-2.0
REM 
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

if "%DITA_HOME%"=="" goto setDefaultDitaHome

:setDefaultDitaHome
rem %~dp0 is expanded pathname of the current script under NT
set DITA_HOME=%~dp0..

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set DITA_CMD_LINE_ARGS=
:setupArgs
if ""%1""=="""" goto doneStart
set DITA_CMD_LINE_ARGS=%DITA_CMD_LINE_ARGS% %1
shift
goto setupArgs

rem This label provides a place for the argument list loop to break out
rem and for NT handling to skip to.

:doneStart

:checkJava
rem Set environment variables
call "%DITA_HOME%\resources\env.bat"

set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:runAnt
"%_JAVACMD%" %ANT_OPTS% -Djava.awt.headless=true -classpath "%DITA_HOME%\lib\ant-launcher.jar" "-Dant.home=%DITA_HOME%"  "-Ddita.dir=%DITA_HOME%" org.apache.tools.ant.launch.Launcher %ANT_ARGS% -cp "%CLASSPATH%" %DITA_CMD_LINE_ARGS% -buildfile "%DITA_HOME%\build.xml" -main "org.dita.dost.invoker.Main"
rem Check the error code of the Ant build
if not "%OS%"=="Windows_NT" goto onError
set ANT_ERROR=%ERRORLEVEL%
goto end

:onError
rem Windows 9x way of checking the error code.  It matches via brute force.
for %%i in (1 10 100) do set err%%i=
for %%i in (0 1 2) do if errorlevel %%i00 set err100=%%i
if %err100%==2 goto onError200
if %err100%==0 set err100=
for %%i in (0 1 2 3 4 5 6 7 8 9) do if errorlevel %err100%%%i0 set err10=%%i
if "%err100%"=="" if %err10%==0 set err10=
:onError1
for %%i in (0 1 2 3 4 5 6 7 8 9) do if errorlevel %err100%%err10%%%i set err1=%%i
goto onErrorEnd
:onError200
for %%i in (0 1 2 3 4 5) do if errorlevel 2%%i0 set err10=%%i
if err10==5 for %%i in (0 1 2 3 4 5) do if errorlevel 25%%i set err1=%%i
if not err10==5 goto onError1
:onErrorEnd
set ANT_ERROR=%err100%%err10%%err1%
for %%i in (1 10 100) do set err%%i=

:end
rem bug ID 32069: resetting an undefined env variable changes the errorlevel.
if not "%_JAVACMD%"=="" set _JAVACMD=
if not "%_DITA_CMD_LINE_ARGS%"=="" set DITA_CMD_LINE_ARGS=

if "%ANT_ERROR%"=="0" goto mainEnd

goto omega

:mainEnd

rem If there were no errors, we run the post script.
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal

:omega

exit /b %ANT_ERROR%
