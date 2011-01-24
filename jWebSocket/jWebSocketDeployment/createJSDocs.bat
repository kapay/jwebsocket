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
echo Auto Generation of jWebSocket v%JWEBSOCKET_VER% JavaScript Docs, are you sure?
pause
:dontAsk1

rem set log=..\jWebSocketDeployment\createJSDocs.log
set log=con

rem save current deployment folder
pushd
cd /d ..\jWebSocketClient\web\res\js
copy /b jWebSocket.js + jwsCanvasPlugIn.js + jwsChatPlugIn.js + jwsClientGamingPlugIn.js + jwsFileSystemPlugIn.js + jwsJDBCPlugIn.js + jwsMailPlugIn.js + jwsRPCPlugIn.js + jwsSamplesPlugIn.js + jwsSharedObjectsPlugIn.js + jwsStreamingPlugIn.js + jwsTwitterPlugIn.js + jwsXMPPPlugIn.js jWebSocket_Bundle.js

rem switch back to deployment folder
popd
rem cd ..\jWebSocketDeployment

echo finished! Please check if JavaScript Docs have been created.
if "%1"=="/y" goto dontAsk2
pause
:dontAsk2