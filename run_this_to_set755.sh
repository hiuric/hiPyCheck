#!/bin/sh
#Windowsで破壊された実行フラグを復活させる
find . -name \*.sh -exec chmod 755 {} +
#find  -name \*.sh -exec echo {} +
