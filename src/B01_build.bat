@echo off
pushd %~dp0
::===============================================================
:: A01_build.bat
:: Windows�o�b�`��Java�v���O�����̃r���h���s�����s�\jar�����
::   MAIN: ���C���N���X
::   LIBS: �Q�Ƃ��郉�C�u����jar (�W�J�����sjar�Ɏ�荞��)
::   JARS: jar�ɓ������́@���C�u�����̃t�H���_��N���X�t�@�C��
::         jar��-c�͎Q�ƃJ�����g�t�H���_�ύX�Ȃ̂Œʏ�͎g��Ȃ�
::   TMPS: �񎟃t�@�C���Ƃ��čŌ�ɍ폜�������
::=============================================================== 
set MAIN=hiPyCheck
set LIBS=.\hiNote_3_10.jar
set SRCS=*.java
set JARS=otsu *.class
set TMPS=otsu;META-INF;*.class,manifest.txt,_temp.sh
::===============================================================
::-------------------------------------------------------
:: �R���p�C��
::-------------------------------------------------------
javac -encoding utf8 -classpath .;%LIBS% %SRCS%
if ERRORLEVEL 1 goto ERR
::-------------------------------------------------------
:: ���C�u����jar�̓W�J
::-------------------------------------------------------
For %%a In (%LIBS%) Do (
   jar xvf %%a
   if ERRORLEVEL 1 goto ERR
)
::-------------------------------------------------------
:: jar�ɂ܂Ƃ߂�
:: �d�v�Ȓ��ӁI Main-Class:�̌��ɂP�󔒂��K�v
::-------------------------------------------------------
echo Manifest-Version: 1.0 >  manifest.txt
echo Main-Class: %MAIN%    >> manifest.txt
jar  cvfm %MAIN%.jar manifest.txt %JARS%
if ERRORLEVEL 1 goto ERR
::-------------------------------------------------------
:: Linux �R�}���h�����(���Ȃ�)
::-------------------------------------------------------
::echo java -jar \"\$0\" \"\$@\" > _temp.sh
::echo exit \$? >> _temp.sh
::copy _temp.sh/B+%MAIN%.jar/B %MAIN%/B
::move %MAIN% ..\bin
::dir /b /s ..\bin\%MAIN%
::-------------------------------------------------------
:: %MAIN%.jar��bin�t�H���_�ֈړ�
move %MAIN%.jar ..\bin
dir /b /s ..\bin\%MAIN%.jar
::-------------------------------------------------------
:: �񎟃t�@�C���A�t�H���_���폜����
::   �폜�͂܂�del�Ńt�@�C���������Armdir�Ńt�H���_������
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
