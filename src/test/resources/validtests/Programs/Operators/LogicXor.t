var b: Bool = true;
var c: Char = 'a';
var i: Int = 2;
var l: Long = 2l;

// Bool
println(b ^ b);  // res: false
println(b ^ false);  // res: true
println(false ^ false);  // res: false

// Char
println(c ^ c);  // res: 0

// Int
println(i ^ i);  // res: 0
println(i ^ l);  // res: 0
println(i ^ c);  // res: 99

// Long
println(l ^ l);  // res: 0
println(l ^ c);  // res: 99

class A =
	Def toString(): String = return "A";

