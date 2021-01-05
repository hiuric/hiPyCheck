#!/bin/bash
# UTF-8 LF
# A01_build.sh

#===============================================================
# A01_build.sh
# WindowsバッチでJavaプログラムのビルドを行い実行可能jarを作る
#   MAIN: メインクラス
#   LIBS: 参照するライブラリjar (展開し実行jarに取り込む)
#   JARS: jarに入れるもの　ライブラリのフォルダやクラスファイル
#         jarの-cは参照カレントフォルダ変更なので通常は使わない
#   TMPS: 二次ファイルとして最後に削除するもの
#=============================================================== 
MAIN=hiPyCheck
LIBS="./hiNote_3_10.jar"
SRCS="*.java"
JARS="otsu *.class"
TMPS="otsu META-INF *.class manifest.txt _temp.sh"
#===============================================================
#-------------------------------------------------------
# コンパイル
#-------------------------------------------------------
javac -encoding utf8 -classpath .:${LIBS} ${SRCS}
if [ $? -ne 0 ];then echo @@@ SOME ERROR OCCURED @@@;exit 1; fi
#-------------------------------------------------------
# ライブラリjarの展開
#-------------------------------------------------------
for lib_jar in ${LIBS} 
do
   jar xvf ${lib_jar}
   if [ $? -ne 0 ];then echo @@@ SOME ERROR OCCURED @@@;exit 1; fi
done
#-------------------------------------------------------
# jarにまとめる
# 重要な注意！ Main-Class:の後ろに１個空白が必要
#-------------------------------------------------------
echo Manifest-Version: 1.0 >  manifest.txt
echo Main-Class: ${MAIN}    >> manifest.txt
jar  cvfm ${MAIN}.jar manifest.txt ${JARS}
if [ $? -ne 0 ];then echo @@@ SOME ERROR OCCURED @@@;exit 1; fi
#-------------------------------------------------------
# コマンドを作る
#-------------------------------------------------------
echo java -jar \"\$0\" \"\$@\" > _temp.sh
echo exit \$? >> _temp.sh
cat _temp.sh ${MAIN}.jar >> ${MAIN}
chmod a+x ${MAIN}
#-------------------------------------------------------
# コマンドとjarをbinに移動
#-------------------------------------------------------
mv ${MAIN} ../bin
if [ $? -ne 0 ];then echo @@@ SOME ERROR OCCURED @@@;exit 1; fi
FILENAME=../bin/${MAIN}
ABS_PATH=$(cd $(dirname ${FILENAME}); pwd)/$(basename ${FILENAME})
echo "CREATED : ${ABS_PATH} as executable"
mv ${MAIN}.jar ../bin
if [ $? -ne 0 ];then echo @@@ SOME ERROR OCCURED @@@;exit 1; fi
FILENAME_J=../bin/${MAIN}.jar
ABS_PATH=$(cd $(dirname ${FILENAME_J}); pwd)/$(basename ${FILENAME_J})
echo "CREATED : ${ABS_PATH}"
#-------------------------------------------------------
# 二次ファイル、フォルダを削除する
#   削除はまずdelでファイルを消し、rmdirでフォルダを消す
#-------------------------------------------------------
for temp_file in ${TMPS} 
do
  rm -rf ${temp_file}
done
#===============================================================

#cat `echo  "java -jar \"$0\" \"$@\"\n exit $?"` A01_build.sh  > temp.sh
#cat `echo abc` 
echo === OK ===
