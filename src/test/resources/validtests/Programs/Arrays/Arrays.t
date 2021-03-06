var int: Int[]    = new Int[5]
var str: String[] = new String[5]
var bool: Bool[]  = new Bool[5]
var a: A[]        = new A[5]

println(int.Size())  // res: 5
println(str.Size())  // res: 5
println(bool.Size()) // res: 5
println(a.Size())    // res: 5

for(var i = 0; i < 5; i++)
	int[i]  = i
	str[i]  = "" + i
	bool[i] = i % 2 == 0
	a[i]    = new A(i)

for(var i = 0; i < 5; i++)
	println(int[i]) // res: 0, 1, 2, 3, 4

for(var i = 0; i < 5; i++)
	println(str[i]) // res: 0, 1, 2, 3, 4

for(var i = 0; i < 5; i++)
	println(bool[i]) // res: true, false, true, false, true

for(var i = 0; i < 5; i++)
	println(a[i])    // res: 0, 1, 2, 3, 4

class A =

	var int: Int

	Def new(i: Int) = int = i

	Def toString(): String = "" + int
