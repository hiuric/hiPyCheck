# -*- coding: utf-8 -*-
a=1     # [E]定義なし
b:int=2 # [D]
def func():
   global a        # [D]
   a=3             # GLOBAL
   b=4             # [E]GLOBALへ代入
   c=5             # [E]定義なし
   d:str="abc"     # [D]型定義
   if a == 3 :     #
      e:float=7.8  # [D]型定義
      e=9.0        # 同階層で定義済み
      if b == 4 :  #
         e=11.0    # 上の階層で定義してある
         d:int=5   # [E]この階層で再定義
         print("nestScope (1)")
   d="x"           # 同階層で定義済み
   d:int=5         # [E]多重定義
   e=12.0          # [E]スコープ内に定義なし
   f="nn"          # [E]後出定義無効
   #ign            # 次行のチェックをしない
   g=2             # 未定義だがrefの後はエラーにしない
   print("nestScope (2)")
def func2():
   b:int=2         # [E]GLOBAL再定義
   f:str="xyz"     # [D]後出
   g:int=1         # [D]後出
   d=5             # [E]スコープ内に定義なし
   print("nestScope (3)")
#ign               # 次行のチェックをしない
b:int=1            # 多重定義だがエラーにしない
#
a=2                # 定義あり
if b==1 :
   a=3             # 定義あり
   b=4             # 定義あり
   c=5             # [E]定義なし
func()
func2()
print("nestScope OK")
