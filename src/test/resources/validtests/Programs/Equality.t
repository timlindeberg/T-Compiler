var num: Int
var a: A
var b: A
var c: A
var s1: String
var s2: String
var i1: Int[]
var i2: Int[]

num = 1

a = new A()
b = new B()
c = new A()
s1 = "Hej"
s2 = "Hej"

i1 = new Int[2]
i1[0] = 0
i1[1] = 1
i2 = new Int[2]
i2[0] = 0
i2[1] = 1

// primitives
println(num == 1)      // res: true
println(1 == 2)        // res: false
println(true == true)  // res: true
println(false == true) // res: false

// strings
println("Hej" == "Hej")// res: true
println(s1 == s2)      // res: true
s1 = s1 + "hej"
s2 = s1 + "hej"
println(s1 == s2)      // res: false

// objects
println(a == b)        // res: false
println(a == c)        // res: false
a = b
println(a == b)        // res: true


// arrays
println(i1 == i2)      // res: false
i1 = i2
println(i1 == i2)      // res: true

class A
class B : A
