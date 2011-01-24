@echo off

if "%JWEBSOCKET_HOME%"=="" goto error
if "%JWEBSOCKET_VER%"=="" goto error
goto continue
:error
echo Environment variable(s) JWEBSOCKET_HOME and/or JWEBSOCKET_VER not set!
pause
exit
:continue

if "%1"=="/y" goto dontAsk1
echo This will clean and build jWebSocket %JWEBSOCKET_VER%. Are you sure?
pause
:dontAsk1

cd ..
call mvn clean install
cd jWebSocketDeployment

if "%1"=="/y" goto dontAsk2
pause
:dontAsk2
