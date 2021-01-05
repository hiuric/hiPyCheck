@echo off
:: 
set /p ans=are you sure clean bin y/n? 
if not "%ans%"=="y" goto end

del ..\bin\* 2> NUL

::
:END
::
@if not "%1"=="" goto NOPAUSE
pause
:NOPAUSE