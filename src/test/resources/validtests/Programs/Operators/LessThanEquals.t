var c: Char = 'a';
var i: Int = 1;
var l: Long = 2l;
var f: Float = 3f;
var d: Double = 4.0;

// Int
println(i <= i);  // res: true
println(i <= l);  // res: true
println(i <= f);  // res: true
println(i <= d);  // res: true
println(i <= c);  // res: true

// Long
println(l <= i);  // res: false
println(l <= l);  // res: true
println(l <= f);  // res: true
println(l <= d);  // res: true
println(l <= c);  // res: true

// Float
println(f <= i);  // res: false
println(f <= l);  // res: false
println(f <= f);  // res: true
println(f <= d);  // res: true
println(f <= c);  // res: true

// Double
println(d <= i);  // res: false
println(d <= l);  // res: false
println(d <= f);  // res: false
println(d <= d);  // res: true
println(d <= c);  // res: true

// Char
println(c <= i);  // res: false
println(c <= l);  // res: false
println(c <= f);  // res: false
println(c <= d);  // res: false
println(c <= c);  // res: true

class A =
	Def toString(): String = return "A";
