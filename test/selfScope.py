# -*- coding: utf-8 -*-
class X:
   class_val=1         # 無視
   class_val2:any=2    # 無視
   def __init__(self):
      self.a:int=1     # [D]定義
      self.b    =2     # [E]未定義
      #
   def func(self):
      self.a=3         # 定義あり
      self.b=6         # [E/T]未定義
      self.c:int=5     # [D]定義
      if self.a == 3 :
         self.x:int=6  # [D]定義
         self.y=8      # [E]後出参照不可
         print("selfScope (1)")
      self.x=5         # 階層を超えて見ることができる
      print("selfScope (2)")
   def func2(self):
      self.c:int=7     # [E]関数を超えても定義済み
      self.a=4         # 関数を超えてみることができる
      self.y:int=6     # 後定義
      #ign             # 次行のチェックをしない
      self.x:int=9     # 多重定義だがエラーにしない
      print("selfScope (3)")
class Y:
   def __init__(self):
      self.a:int=1     # [D]定義
      self.b    =2     # [E]未定義
      #ign             # 次行のチェックをしない
      self.c    =3     # 未定義だがエラーとしない
   def func(self):
      self.x =3        # [E]他のクラスの変数は見えない
      print("selfScope (4)")
#
x:X  = X()
y:any= Y() # any型でも構わない
x.func()
x.func2()
y.func()
print("selfScope OK")
