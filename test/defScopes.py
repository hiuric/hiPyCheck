# [T]は-no_tent字は出ない
z:int=1
def a():
   if x :
      y=1 # [E] NOT FOUND
   else:
      y=2 # [E] NOT FOUND
   y=3 # [E] NOT FOUND
       # [T] USED in inner scope  
def b():
   if x :
      y:int=1 #OK
   else:
      y:int=2 #OK
def c():
   if x :
      y:int=1 #OK
   else:
      y:int=2 #OK
   y=3 # [E] NOT FOUND
       # [T] USED in inner scope
def c():
   if x :
      y:int=1 #OK
   else:
      y:int=2 #OK
   y:int=3 # [E] USED IN inner scope
z:int=1 # [E] ALREADY DEFINED
def d():
   if x :
      z=1 # [E] ASSIGN to GLOBAL <2>
   else:
      z=2 # [E] ASSIGN to GLOBAL <2>
   z=3 # [E] ASSIGN to GLOBAL <2>

