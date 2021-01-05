#!/bin/bash
# UTF-8 LF
# A01_test.sh
PYS="nestScope.py selfScope.py globalScope.py defScopes.py"
#OPTIONS="-verbose -detail -step"
OPTIONS=""
CHECKER=../bin/hiPyCheck.jar
CHECK="java -jar ${CHECKER} ${OPTIONS}"
#------------------------
# チェック実行
#------------------------
echo ${CHECK} ${PYS}
${CHECK} ${PYS} |tee kekka.txt
# if [ $? -ne 0 ];then exit 1;fi
#------------------------
# 結果検証
#------------------------
echo ----- check kekka.txt
java -cp ${CHECKER} otsu.hiNote.COMMAND diff kekka.txt kekka.ref
if [ $? -ne 0 ];then echo "*** SOME ERROR OCCURED ***"; exit 1;fi
echo --------------------
for py in ${PYS}; do
   python ${py}
   if [ $? -ne 0 ];then echo "*** SOME ERROR OCCURED ***";exit 1;fi
done

echo "*** SUCCRESS ***"
