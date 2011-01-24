@echo off

if "%JWEBSOCKET_HOME%"=="" goto error
if "%JWEBSOCKET_VER%"=="" goto error
goto continue
:error
echo Environment variable(s) JWEBSOCKET_HOME and/or JWEBSOCKET_VER not set!
pause
exit
:continue

echo This will create the entire jWebSocket v%JWEBSOCKET_VER% Nightly Build. Are you sure?
pause

rem generate the java docs (saved to client web)
rem call createJavaDocs.bat
rem clean and build the project
call cleanAndBuildAll.bat /y
rem create Run-Time-Environment
call createRunTimeFiles.bat /y
rem create download from Run-Time-Environment
call createDownloadFiles.bat /y

pause