var c: A? = new A()
if(c.nullable != null)
	c.nullable.Test()
c.nullable.Test() // res: F2000

if(c.nullable != null && c.nullable.nullable != null)
	c.nullable.nullable.Test()

if(c.nullable)
	c.nullable.Test()

if(c.nullable && c.nullable.nullable)
	c.nullable.nullable.Test()

// TODO: This should probably work
// if(c?.nullable?.nullable != null)
//     c.nullable.nullable.Test()
//
// if(c?.nullable?.nullable)
//     c.nullable.nullable.Test()

c.nullable.nullable.Test() // res: F2000, F2000

if(c.nullable && c.nullable.nullable)
	c = GetA()
	c.Test() // res: F2000
	c.nullable.Test() // res: F2000, F2000
	c.nullable.nullable.Test() // res: F2000, F2000, F2000

var a = GetA()


if(A.staticVar)
	A.staticVar.Test()
	B.staticVar.Test() // res: F2000

if(A.staticVar != null)
	A.staticVar.Test()

A.staticVar.Test() // res: F2000


/*------------------------------------------------------------------------*/

Def GetA(): A? = null
Def GetB(): B? = null

class A =

	Val static staticVar: A? = true ? new A() : null
	Val nullable: B? = true ? new B() : null

	Def Test() = println("Test")

class B =

	Val static staticVar: A? = true ? new A() : null
	Val nullable: C? = true ? new C() : null

	Def Test() = println("Test")

class C =

	var X: Int?
	var Y: Int?
	var Z: Int?

	Def Test() = println("Test")

	Def Test2() =
		X = Y = Z = 1
		println(X + Y + Z)
