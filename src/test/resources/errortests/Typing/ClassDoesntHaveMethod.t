var a = new A()
var b = 1
var arr = [ 1, 2, 3 ]
a.Test() // res: T2001
A.Test() // res: T2001

b.Test() // res: T2001

println(arr.Size())
println(arr.Lol()) // res: T2002
var c = a.x // res: T2003
var d = A.x // res: T2003
var e = b.x // res: T2003

class A