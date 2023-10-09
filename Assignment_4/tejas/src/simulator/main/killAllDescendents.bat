@echo off
setlocal ENABLEDELAYEDEXPANSION
set cmd="wmic process where (ParentProcessId=%1) get ProcessId"
for /F "tokens=*" %%a in ('%cmd%') do call :Foo %%a %0
goto End

:Foo 
echo(%~1|findstr "^[-][1-9][0-9]*$ ^[1-9][0-9]*$ ^0$">nul&& ( echo %1 & CALL %2 %1 & taskkill /F /IM %1 >nul 2>nul )
goto :eof

:End
