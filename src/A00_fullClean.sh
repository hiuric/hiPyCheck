#!/bin/bash
# UTF-8,LF
read -p "are you sure clean bin? y/n "  -r
if [[ $REPLY =~ ^[Yy]$ ]]
then
   :
else
   exit 1;
fi
echo rm -f ../bin/*
rm -f ../bin/* 2> /dev/null
echo DONE
