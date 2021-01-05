@echo off
pushd %~dp0
::===============================================================
:: A01_build.bat
:: WindowsバッチでJavaプログラムのビルドを行い実行可能jarを作る
::   MAIN: メインクラス
::   LIBS: 参照するライブラリjar (展開し実行jarに取り込む)
::   JARS: jarに入れるもの　ライブラリのフォルダやクラスファイル
::         jarの-cは参照カレントフォルダ変更なので通常は使わない
::   TMPS: 二次ファイルとして最後に削除するもの
::=============================================================== 
set MAIN=hiPyCheck
set LIBS=.\hiNote_3_10.jar
set SRCS=*.java
set JARS=otsu *.class
set TMPS=otsu;META-INF;*.class,manifest.txt,_temp.sh
::===============================================================
::-------------------------------------------------------
:: コンパイル
::-------------------------------------------------------
javac -encoding utf8 -classpath .;%LIBS% %SRCS%
if ERRORLEVEL 1 goto ERR
::-------------------------------------------------------
:: ライブラリjarの展開
::-------------------------------------------------------
For %%a In (%LIBS%) Do (
   jar xvf %%a
   if ERRORLEVEL 1 goto ERR
)
::-------------------------------------------------------
:: jarにまとめる
:: 重要な注意！ Main-Class:の後ろに１個空白が必要
::-------------------------------------------------------
echo Manifest-Version: 1.0 >  manifest.txt
echo Main-Class: %MAIN%    >> manifest.txt
jar  cvfm %MAIN%.jar manifest.txt %JARS%
if ERRORLEVEL 1 goto ERR
::-------------------------------------------------------
:: Linux コマンドを作る(やらない)
::-------------------------------------------------------
::echo java -jar \"\$0\" \"\$@\" > _temp.sh
::echo exit \$? >> _temp.sh
::copy _temp.sh/B+%MAIN%.jar/B %MAIN%/B
::move %MAIN% ..\bin
::dir /b /s ..\bin\%MAIN%
::-------------------------------------------------------
:: %MAIN%.jarをbinフォルダへ移動
move %MAIN%.jar ..\bin
dir /b /s ..\bin\%MAIN%.jar
::-------------------------------------------------------
:: 二次ファイル、フォルダを削除する
::   削除はまずdelでファイルを消し、rmdirでフォルダを消す
::-------------------------------------------------------
For %%a In (%TMPS%) Do (
   del /q %%a >NUL 2>&1
   rmdir /s /q %%a >NUL 2>&1
   if ERRORLEVEL 1 goto ERR
)
:OK
@if not "%1"=="" goto NOPAUSE
echo === OK ===
set result=0
goto END
:ERR
set result=1
echo === SOME ERROR OCCURED ===
:END
pause
:NOPAUSE
popd
exit /b %result%
