var c: Char = 'b';
var i: Int = 2;
var l: Long = 2l;
var f: Float = 2f;
var d: Double = 2.0;

// Char
println(c % c);  // res: 0

// Int
println(i % i);  // res: 0
println(i % l);  // res: 0
println(i % f);  // res: 0.0
println(i % d);  // res: 0.0
println(i % c);  // res: 2

// Long
println(l % l);  // res: 0
println(l % f);  // res: 0.0
println(l % d);  // res: 0.0
println(l % c);  // res: 2

// Float
println(f % f);  // res: 0.0
println(f % d);  // res: 0.0
println(f % c);  // res: 2.0

// Double
println(d % d);  // res: 0.0
println(d % c);  // res: 2.0

class A =
	Def toString(): String = return "A";
