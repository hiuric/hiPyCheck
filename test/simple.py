# -*- coding: utf-8 -*-
import time
count = 0
while count<10 :
   conut = count+1      # このミスを発見する
   print("count="+str(count))
   time.sleep(1)
