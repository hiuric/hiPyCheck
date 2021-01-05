
g:int=1
if g==1 :
  h:int=3
def a():
  print("a")
  print(g)
def b():
  print("b")
  g=5     # [E] ASSIGN to GLOBAL
  h=7     # [E] ASSIGN to GLOBAL
  print(g)
def c():
  print("c")
  global g
  print(g)
  g=7      # OK
  global h
  h=11     # OK
def d():
  print("d")
  print(g) # 実行時エラー、なんじゃそれ？！
  g:int=3  # [E] REDEFINE of GLOBAL
  h:int=13 # [E] REDEFINE of GLOBAL
def e():
  print("e")
  print(g) # 実行時エラー、なんじゃそれ？！
  g=2      # [E] ASSIGN to GLOBAL
  h=5      # [E] ASSIGN to GLOBAL
a()
b()
c()
try:
   d()
except Exception as _ex:
   print("d:"+str(_ex))
try:
   e()
except Exception as _ex:
   print("e:"+str(_ex))

class X:
   def a(self):
      print("a")
      print(g)
   def a(self):
      print("a")
      print(g)
   def b(self):
      print("b")
      g=5     # [E] ASSIGN to GLOBAL
      h=7     # [E] ASSIGN to GLOBAL
      print(g)
   def c(self):
      print("c")
      global g
      print(g)
      g=7      # OK
      global h
      h=11     # OK
   def d(self):
      print("d")
      print(g) # 実行時エラー、なんじゃそれ？！
      g:int=3  # [E] REDEFINE of GLOBAL
      h:int=13 # [E] REDEFINE of GLOBAL
   def e(self):
      print("e")
      print(g) # 実行時エラー、なんじゃそれ？！
      g=2      # [E] ASSIGN to GLOBAL
      h=5      # [E] ASSIGN to GLOBAL
x:X=X()
x.a()
x.b()
x.c()
try:
   x.d()
except Exception as _ex:
   print("x.d:"+str(_ex))
try:
   x.e()
except Exception as _ex:
   print("x.e:"+str(_ex))
