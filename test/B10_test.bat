@echo off
:: SJIS CR-LF
:: hiPyCheck試験
::----------------------------------------
set PYS=nestScope.py selfScope.py globalScope.py defScopes.py
::set PYS=defScopes.py
::set OPTIONS=-verbose -detail -step -no_tent
set OPTIONS=
set CHECKER=../bin/hiPyCheck.jar
set CHECK=java -jar %CHECKER% %OPTIONS%
::------------------------
:: チェック実行
::------------------------
echo %CHECK% %PYS%
%CHECK% %PYS% > kekka.txt
:: if ERRORLEVEL 1 goto ERR:
type kekka.txt
::------------------------
:: 結果検証
::------------------------
echo check kekka.txt
java -cp %CHECKER% otsu.hiNote.COMMAND diff kekka.txt kekka.ref
if ERRORLEVEL 1 goto ERR:
::------------------------
:: 念のため対象py実行
::------------------------
echo --------------------
for %%f in (%PYS%) do (
   echo python %%f
   python  %%f
   if ERRORLEVEL 1 goto ERR:
)
::- - - - - - - - - - - - - - - - - -
echo *** SUCCRESS ***
goto END:
::------------------------------------
:ERR
echo *** SOME ERROR OCCURED ***
:END
if not "%1"=="" goto NOPAUSE
:PAUSE
pause
:NOPAUSE
popd
exit /b %result%

