var a1: A = new A()
var a2: A = new A()
var a3: A = new A()

println(A.i) // res: 15
println(A.j) // res: 16
println(A.k) // res: 5

class A =

	Var static a: A = new A()
	Var static i: Int = 15
	Var static j: Int = i + 1
	Var static k: Int = test()

	def static test(): Int =
		println("Init")  // res: Init
		return 5