var c: Char = 'a';
var i: Int = 1;
var l: Long = 2l;
var f: Float = 3f;
var d: Double = 4.0;

// Int
println(i >= i);  // res: true
println(i >= l);  // res: false
println(i >= f);  // res: false
println(i >= d);  // res: false
println(i >= c);  // res: false

// Long
println(l >= i);  // res: true
println(l >= l);  // res: true
println(l >= f);  // res: false
println(l >= d);  // res: false
println(l >= c);  // res: false

// Float
println(f >= i);  // res: true
println(f >= l);  // res: true
println(f >= f);  // res: true
println(f >= d);  // res: false
println(f >= c);  // res: false

// Double
println(d >= i);  // res: true
println(d >= l);  // res: true
println(d >= f);  // res: true
println(d >= d);  // res: true
println(d >= c);  // res: false

// Char
println(c >= i);  // res: true
println(c >= l);  // res: true
println(c >= f);  // res: true
println(c >= d);  // res: true
println(c >= c);  // res: true

class A =
	Def toString(): String = return "A";
